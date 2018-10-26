import java.util.*;
import java.util.concurrent.TimeUnit;

public class Process {

    // static variables
    private static Random random = new Random();
    static class VARIABLES {
        public final static TimeUnit TIME_UNIT_FOR_PROCESS_SLEEP = TimeUnit.MILLISECONDS;
        public final static Integer TIME_FOR_PROCESS_SLEEP_MIN = 1;
        public final static Integer TIME_FOR_PROCESS_SLEEP_MAX = 20;
        public final static Integer TIME_FOR_REFERENCED_PAGE_DELETE = 200;
        public final static Integer t = 800;
    }

    private Integer pid;
    // virtual pages for process
    private List<VirtualPageMappingToPhysicalPageRecord> records;
    private List<Integer> workingSet;

    private OS os;

    public Process(Integer pid, Integer needMemoryAtStart, OS os) {
        this.os = os;
        this.pid = pid;
        initRecords(needMemoryAtStart);
        initWorkingSet();
    }

    private void initRecords(Integer needMemoryAtStart) {
        System.out.println(this + "Start init records. Need records to process start: " + needMemoryAtStart);
        this.records = new ArrayList<>();
        for (int i = 0; i < needMemoryAtStart; i++) {
            VirtualPageMappingToPhysicalPageRecord record = new VirtualPageMappingToPhysicalPageRecord(i);
            records.add(record);
        }
        System.out.println(this + "Finish init records");
    }

    private void initWorkingSet() {
        System.out.println(this + "Start init working set");
        this.workingSet = new ArrayList<>();
        // TODO: At first, working set is a 5 times smaller than "need memory at start". Change later
        Integer workingSetSize = records.size() / 5;
        System.out.println(this + "Working set size = " + workingSetSize);
        for (int i = 0; i < workingSetSize; i++) {
            PhysicalPage freePhysicalPage = os.getFreePhysicalPage();
            records.get(i).setPhysicalPage(freePhysicalPage);
            records.get(i).setPrecedenceBit(true);
            freePhysicalPage.setFree(false);
            workingSet.add(i);
        }
        System.out.println(this + "Finish init working set");
    }

    // should use pages just from working set!!! (pages, which are in memory now)
    public void run(Integer timeToExecute) {
        System.out.println(this + "Awake. Time to execute: " + timeToExecute);

        // when we call page from working set it equals 4 ms, when not = 30 ms
        Integer timePassedFromProcessCall = 0;
        Integer timerCounter = 0;

        // reset all pages lastTimeAccessed
        for (VirtualPageMappingToPhysicalPageRecord record : records) {
            record.setLastAccessedTime(0);
        }

        while (timeToExecute >  timePassedFromProcessCall) {

            if (timePassedFromProcessCall / VARIABLES.TIME_FOR_REFERENCED_PAGE_DELETE > timerCounter) {
                //clear all references in working set
                for (VirtualPageMappingToPhysicalPageRecord record : records) {
                    if (record.getPrecedenceBit()) {
                        record.setReferencedBit(false);
                        record.setReadBit(false);
                        record.setModificationBit(false);
                    }
                }
                timerCounter++;
            }

            Integer pageCalledAddress = null;
            try {
                if (random.nextDouble() > 0.1) {
                    System.out.println(this + "Start getting page address from working set");
                    pageCalledAddress = getRandomPageAddressFromWorkingSet();
                    System.out.println(this + "Finish getting page address from working set. Page address: " + pageCalledAddress);
                    timePassedFromProcessCall += 4;
                } else {
                    System.out.println(this + "Start getting page address not from working set");
                    pageCalledAddress = getRandomPageAddressNotFromWorkingSet();
                    System.out.println(this + "Finish getting page address not from working set. Page address: " + pageCalledAddress);
                    // TODO: you can try to allocate physical memory if it is exists and avoid exception below throw
                    throw new PageAbsenceException("Virtual page with address " + pageCalledAddress + " is not in working set");
                }
            } catch (PageAbsenceException e) {
                replaceAbsentPageWithOldFromWorkingSet(timePassedFromProcessCall, pageCalledAddress, e);
                timePassedFromProcessCall += 30;
            }

            System.out.println(this + "Read or modify chosen record");
            referenceToPageByAddres(pageCalledAddress);

            // process sleeping
            try {
                Integer timeForSleep = VARIABLES.TIME_FOR_PROCESS_SLEEP_MIN + random.nextInt(VARIABLES.TIME_FOR_PROCESS_SLEEP_MAX - VARIABLES.TIME_FOR_PROCESS_SLEEP_MIN);
                System.out.println(this + "Starting to sleep for " + timeForSleep);
                VARIABLES.TIME_UNIT_FOR_PROCESS_SLEEP.sleep(timeForSleep);
            } catch (InterruptedException e) {
                System.out.println(this + "Interrupted while sleeping");
            }
        }

        System.out.println(this + "Sleep...");
    }

    private Integer replaceAbsentPageWithOldFromWorkingSet(Integer timePassed, Integer pageCalledAddress, PageAbsenceException e) {
        System.out.println(this + e.getMessage());
        Integer maxPageAgeToRemove = null;
        VirtualPageMappingToPhysicalPageRecord maxPageAgeToRemoveRecord = null;
        VirtualPageMappingToPhysicalPageRecord recordWithNoModification = null;
        Boolean isPageReplaced = false;
        for (VirtualPageMappingToPhysicalPageRecord record : records) {
            if (record.getPrecedenceBit()) {
                if (record.getReferencedBit()) {
                    System.out.println(this + "Record had reference during this system tact. Set current virtual time as last accessed");
                    System.out.println("New last accessed time =" + timePassed + "\n" + record);
                    record.setLastAccessedTime(timePassed);
                    if (recordWithNoModification == null && !record.getModificationBit()) {
                        recordWithNoModification = record;
                    }
                } else {
                    Integer pageAge = (timePassed - record.getLastAccessedTime());
                    if (pageAge > VARIABLES.t && !isPageReplaced) {
                        System.out.println(this + "Record is old. t=" + VARIABLES.t + ", pageAge=" + pageAge + ". Replace it with new one.");
                        replacePhysicalPageReferenceFromOldToNewPage(record, records.get(pageCalledAddress));
                        isPageReplaced = true;
                    } else if (pageAge <= VARIABLES.t && !isPageReplaced) {
                        System.out.println(this + "Record isn't old enough. t=" + VARIABLES.t + ", pageAge=" + pageAge + ".");
                        if (maxPageAgeToRemove == null || pageAge > maxPageAgeToRemove) {
                            maxPageAgeToRemove = pageAge;
                            maxPageAgeToRemoveRecord = record;
                        }
                    }
                }
            }
        }
        if (!isPageReplaced) {
            System.out.println(this + "No page has been replaced yet.");
            replacePageAfterAllRecordsScan(pageCalledAddress, maxPageAgeToRemoveRecord, recordWithNoModification);
        }
        return timePassed;
    }

    private void replacePageAfterAllRecordsScan(Integer pageCalledAddress, VirtualPageMappingToPhysicalPageRecord maxPageAgeToRemoveRecord, VirtualPageMappingToPhysicalPageRecord recordWithNoModification) {
        if (maxPageAgeToRemoveRecord != null) {
            System.out.println(this + "The oldest page found. Replace it.");
            replacePhysicalPageReferenceFromOldToNewPage(maxPageAgeToRemoveRecord, records.get(pageCalledAddress));
        } else {
            System.out.println(this + "That was reference to all pages in working set. Replace random with no modification priority.");
            if (recordWithNoModification != null) {
                System.out.println(this + "Random record with no modification found. Replace it. " + recordWithNoModification);
                replacePhysicalPageReferenceFromOldToNewPage(recordWithNoModification, records.get(pageCalledAddress));
            } else {
                Integer pageToRemoveIndex = random.nextInt(workingSet.size());
                System.out.println(this + "All pages were modified. Random record to replace is:" + records.get(pageToRemoveIndex));
                replacePhysicalPageReferenceFromOldToNewPage(records.get(pageToRemoveIndex), records.get(pageCalledAddress));
            }
        }
    }

    private void referenceToPageByAddres(Integer pageCalledAddress) {
        VirtualPageMappingToPhysicalPageRecord record = records.get(pageCalledAddress);
        record.setReferencedBit(true);
        System.out.println(this + "Reference to record. Record address: " + pageCalledAddress);
        if (random.nextDouble() > 0.5) {
            System.out.println(this + "Read");
            record.setReadBit(true);
        } else {
            System.out.println(this + "Modify");
            record.setModificationBit(true);
        }
    }

    private void replacePhysicalPageReferenceFromOldToNewPage(
            VirtualPageMappingToPhysicalPageRecord oldPageFromWorkingSet,
            VirtualPageMappingToPhysicalPageRecord newPageForWorkingSet
    ) {
        System.out.println(this + "Start page replacing.\nOld page: " + oldPageFromWorkingSet + "\nNew page: " + newPageForWorkingSet);
        oldPageFromWorkingSet.setPrecedenceBit(false);
        newPageForWorkingSet.setPrecedenceBit(true);
        PhysicalPage physicalPage = oldPageFromWorkingSet.getPhysicalPage();
        oldPageFromWorkingSet.setPhysicalPage(null);
        newPageForWorkingSet.setPhysicalPage(physicalPage);
        workingSet.remove(oldPageFromWorkingSet.getId());
        workingSet.add(newPageForWorkingSet.getId());
        System.out.println(this + "Finish page replacing\nOld page: " + oldPageFromWorkingSet + "\nNew page: " + newPageForWorkingSet);
    }

    private Integer getRandomPageAddressFromWorkingSet() throws PageAbsenceException {
        if (workingSet.size() > 0) {
            return workingSet.get(random.nextInt(workingSet.size()));
        } else {
            throw new PageAbsenceException("There are no pages in working set yet");
        }
    }

    private Integer getRandomPageAddressNotFromWorkingSet() {
        Integer pageCalledAddress;
        do {
            pageCalledAddress = random.nextInt(records.size());
        } while (workingSet.contains(pageCalledAddress));
        return pageCalledAddress;
    }

    @Override
    public String toString() {
        return "Process-" + pid + ": ";
    }
}
