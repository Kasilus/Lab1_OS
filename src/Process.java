import java.util.*;
import java.util.concurrent.TimeUnit;

public class Process {

    static class VARIABLES {
        public final static TimeUnit TIME_UNIT_FOR_PROCESS_SLEEP = TimeUnit.MILLISECONDS;
        public final static Integer TIME_FOR_PROCESS_SLEEP_MIN = 10;
        public final static Integer TIME_FOR_PROCESS_SLEEP_MAX = 200;
        public final static Integer MIN_NEEDED_PROCESS_REFERENCES_COUNT = 5;
    }

    private Integer pid;
    private OS os;
    private List<VirtualPageMappingToPhysicalPageRecord> records;
    private Integer needMemoryAtStart;
    private static Random random = new Random();
    // virtual pages, which have links on physical pages at the moment
    private List<Integer> workingSet;
    // do it as constant
    private Integer t = 500;
    private LinkedList<Integer> lastSystemTactsTime = new LinkedList<>();

    // pass PID and set to thread
    public Process(Integer pid, Integer needMemoryAtStart, OS os) {
        this.os = os;
        this.pid = pid;
        this.needMemoryAtStart = needMemoryAtStart;
        initRecords();
        initWorkingSet();
    }

    private void initRecords() {
        System.out.println(this + "Start init records. Need records to process start: " + needMemoryAtStart);
        this.records = new ArrayList<>();
        for (int i = 0; i < this.needMemoryAtStart; i++) {
            VirtualPageMappingToPhysicalPageRecord record = new VirtualPageMappingToPhysicalPageRecord(i);
            records.add(record);
//            System.out.println(record);
        }
        System.out.println(this + "Finish init records");
    }

    private void initWorkingSet() {
        System.out.println(this + "Start init working set");
        this.workingSet = new ArrayList<>();
        // At first, working set is a 5 times smaller than "need memory at start"
        // TODO: change later
        Integer workingSetSize = records.size() / 5;
        System.out.println(this + "Working set size = " + workingSetSize);
        for (int i = 0; i < workingSetSize; i++) {
            PhysicalPage freePhysicalPage = os.getFreePhysicalPage();
            records.get(i).setPhysicalPage(freePhysicalPage);
            records.get(i).setPrecedenceBit(true);
            freePhysicalPage.setFree(false);
            workingSet.add(i);
//            System.out.println("Record" + i + " goes to working set");
        }
        System.out.println(this + "Finish init working set");
    }

    // should use pages just from working set!!! (pages, which are in memory now)
    public void run(Integer timeToExecute) {
        System.out.println(this + "Awake. Time to execute: " + timeToExecute);
        Long timeOfProcessAwake = System.currentTimeMillis();

        while (timeToExecute > System.currentTimeMillis() - timeOfProcessAwake) {

            Integer pageCalledAddress = null;
            // call some virtual page
            try {
                if (random.nextDouble() > 0.1) {
                    System.out.println(this + "Start getting page address from working set");
                    pageCalledAddress = getRandomPageAddressFromWorkingSet();
                    System.out.println(this + "Finish getting page address from working set. Page address: " + pageCalledAddress);
                } else {
                    System.out.println(this + "Start getting page address not from working set");
                    pageCalledAddress = getRandomPageAddressNotFromWorkingSet();
                    System.out.println(this + "Finish getting page address not from working set. Page address: " + pageCalledAddress);
                    // TODO: you can try to allocate physical memory if it is exists
                    throw new PageAbsenceException("Virtual page with address " + pageCalledAddress + " is not in working set");
                }
            } catch (PageAbsenceException e) {
                System.out.println(this + e.getMessage());
                Integer counter = 0;
                Integer maxPageAgeToRemove = null;
                VirtualPageMappingToPhysicalPageRecord maxPageAgeToRemoveRecord = null;
                VirtualPageMappingToPhysicalPageRecord recordWithNoModification = null;
                Boolean isPageReplaced = false;
                for (VirtualPageMappingToPhysicalPageRecord record : records) {
                    if (record.getPrecedenceBit()) {
                        if (record.getReferencedBit()) {
                            System.out.println(this + "Record had reference during this system tact. Set current virtual time as last accessed");
                            // maybe, change this for the whole virtual time range
                            Integer newLastAccessedTime = (int) (System.currentTimeMillis() - timeOfProcessAwake);
                            System.out.println("New last accessed time =" + newLastAccessedTime);
                            System.out.println(record);
                            record.setLastAccessedTime(newLastAccessedTime);
                            if (recordWithNoModification == null && !record.getModificationBit()) {
                                recordWithNoModification = record;
                            }
                        } else if (!record.getReferencedBit()) {
                            Integer pageAge = (int) ((System.currentTimeMillis() - timeOfProcessAwake) - record.getLastAccessedTime());
                            if (pageAge > t && !isPageReplaced) {
                                System.out.println(this + "Record is old. t=" + t + ", pageAge=" + pageAge + ". Replace it with new one.");
                                replacePhysicalPageReferenceFromOldToNewPage(record, records.get(pageCalledAddress));
                                isPageReplaced = true;
                            } else if (pageAge <= t) {
                                System.out.println(this + "Record isn't old enough. t=" + t + ", pageAge=" + pageAge + ".");
                                if (maxPageAgeToRemove == null || pageAge > maxPageAgeToRemove) {
                                    maxPageAgeToRemove = pageAge;
                                    maxPageAgeToRemoveRecord = record;
                                }
//                                record.setLastAccessedTime(record.getLastAccessedTime() + timeToExecute);
                            }
                        }
                        counter++;
                    }
                }

                // if all table was scanned, but no pages removed
                if (!isPageReplaced) {
                    System.out.println(this + "No page was replaced. Bad, very bad...");
                    if (maxPageAgeToRemove != null) {
                        System.out.println(this + "Yeah, we have the oldest page. Replace it.");
                        replacePhysicalPageReferenceFromOldToNewPage(maxPageAgeToRemoveRecord, records.get(pageCalledAddress));
                    } else {
                        // delete random page with M = 0
                        System.out.println(this + "Dammit... The worst case. That was reference to all pages in working set. Let's replace random with no modification priority.");
                        if (recordWithNoModification != null) {
                            System.out.println(this + "Random record with no modification found. Replace it. " + recordWithNoModification);
                            replacePhysicalPageReferenceFromOldToNewPage(recordWithNoModification, records.get(pageCalledAddress));
                        } else {
                            Integer pageToRemoveIndex = random.nextInt(workingSet.size());
                            System.out.println(this + "Shit. All were modified. Randooom record to replace is:" + records.get(pageToRemoveIndex));
                            replacePhysicalPageReferenceFromOldToNewPage(records.get(pageToRemoveIndex), records.get(pageCalledAddress));
                        }
                    }
                }

            }

            // read or write page
            System.out.println(this + "Read or modify chosen record");
            VirtualPageMappingToPhysicalPageRecord record = records.get(pageCalledAddress);
            record.setReferencedBit(true);
            if (random.nextDouble() > 0.5) {
                System.out.println(this + "Read");
                record.setReadBit(true);
            } else {
                System.out.println(this + "Modify");
                record.setModificationBit(true);
            }

//            // update process time of last calls
//            System.out.println(this + "Time for last tacts before.");
//            lastSystemTactsTime.forEach(System.out::println);
//            if (lastSystemTactsTime.size() > 5) {
//                t -= lastSystemTactsTime.poll();
//            }
//            lastSystemTactsTime.addLast(timeToExecute);
//            System.out.println(this + "Time for last tacts after.");
//            lastSystemTactsTime.forEach(System.out::println);
//            t+= timeToExecute;

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

    private void replacePhysicalPageReferenceFromOldToNewPage (
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
