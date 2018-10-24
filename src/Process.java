import java.util.*;
import java.util.concurrent.TimeUnit;

public class Process {

    static class VARIABLES {
        public final static TimeUnit TIME_UNIT_FOR_PROCESS_SLEEP = TimeUnit.MILLISECONDS;
        public final static Integer TIME_FOR_PROCESS_SLEEP_MIN = 1;
        public final static Integer TIME_FOR_PROCESS_SLEEP_MAX = 5;
    }

    private OS os;
    private List<VirtualPageMappingToPhysicalPageRecord> records;
    private Integer needMemoryAtStart;
    private static Random random;
    // virtual pages, which have links on physical pages at the moment
    private List<Integer> workingSet;
    // time for the last process references
    private Integer t;
    private LinkedList<Integer> lastSystemTactsTime = new LinkedList<>();

    // pass PID and set to thread
    public Process(Integer pid, Integer needMemoryAtStart, OS os) {
        Thread.currentThread().setName("Process-" + pid);
        this.needMemoryAtStart = needMemoryAtStart;
        initRecords();
        initWorkingSet();
    }

    private void initRecords() {
        this.records = new ArrayList<>();
        for (int i = 0; i < this.needMemoryAtStart; i++) {
            VirtualPageMappingToPhysicalPageRecord record = new VirtualPageMappingToPhysicalPageRecord(i);
            records.add(record);
        }
    }

    private void initWorkingSet() {
        this.workingSet = new ArrayList<>();
        // At first, working set is a 5 times smaller than "need memory at start"
        // TODO: change later
        Integer workingSetSize = records.size() / 5;
        for (int i = 0; i < workingSetSize; i++) {
            PhysicalPage freePhysicalPage = os.getFreePhysicalPage();
            records.get(i).setPhysicalPage(freePhysicalPage);
            records.get(i).setPrecedenceBit(true);
            freePhysicalPage.setFree(false);
            workingSet.add(i);
        }
    }

    public void run(Integer timeToExecute) {

        Long timeOfProcessAwake = System.currentTimeMillis();

        while (timeToExecute < System.currentTimeMillis() - timeOfProcessAwake) {

            Integer pageCalledAddress = null;
            // call some virtual page
            try {
                if (random.nextDouble() > 0.1) {
                    pageCalledAddress = getRandomPageAddressFromWorkingSet();
                } else {
                    pageCalledAddress = getRandomPageAddressNotFromWorkingSet();
                    throw new PageAbsenceException("Virtual page with address " + pageCalledAddress + " is not in working set");
                }
            } catch (PageAbsenceException e) {
                Integer counter = 0;
                Integer maxPageAgeToRemove = null;
                VirtualPageMappingToPhysicalPageRecord maxPageAgeToRemoveRecord = null;
                VirtualPageMappingToPhysicalPageRecord recordWithNoModification = null;
                for (VirtualPageMappingToPhysicalPageRecord record : records) {
                    if (record.getReferencedBit()) {

                        // maybe, change this for the whole virtual time range
                        record.setLastAccessedTime(record.getLastAccessedTime() + (int)(System.currentTimeMillis() - timeOfProcessAwake));
                        if (recordWithNoModification == null && !record.getModificationBit()) {
                            recordWithNoModification = record;
                        }
                    } else if (!record.getReferencedBit()){
                        Integer pageAge = Math.toIntExact((System.currentTimeMillis() - timeOfProcessAwake) - record.getLastAccessedTime());
                        if (pageAge > t) {
                            replacePhysicalPageReferenceFromOldToNewPage(record, records.get(pageCalledAddress));
                            break;
                        } else if (pageAge <= t) {
                            if (maxPageAgeToRemove == null || pageAge > maxPageAgeToRemove) {
                                maxPageAgeToRemove = pageAge;
                                maxPageAgeToRemoveRecord = record;
                            }
                        }
                    }
                    counter++;
                }

                // if all table was scanned, but no pages removed
                if (counter == records.size()) {
                    if (maxPageAgeToRemove != null) {
                        replacePhysicalPageReferenceFromOldToNewPage(maxPageAgeToRemoveRecord, records.get(pageCalledAddress));
                    } else {
                        // delete random page with M = 0
                        if (recordWithNoModification != null) {
                            replacePhysicalPageReferenceFromOldToNewPage(recordWithNoModification, records.get(pageCalledAddress));
                        } else {
                            Integer pageToRemoveIndex = random.nextInt(workingSet.size());
                            replacePhysicalPageReferenceFromOldToNewPage(records.get(pageToRemoveIndex), records.get(pageCalledAddress));
                        }
                    }
                }

            }

            // read or write page
            VirtualPageMappingToPhysicalPageRecord record = records.get(pageCalledAddress);
            if (random.nextDouble() > 0.5) {
                record.setReadBit(true);
            } else {
                record.setModificationBit(true);
            }

            // process sleeping
            try {
                Integer timeForSleep = VARIABLES.TIME_FOR_PROCESS_SLEEP_MIN + random.nextInt(VARIABLES.TIME_FOR_PROCESS_SLEEP_MAX - VARIABLES.TIME_FOR_PROCESS_SLEEP_MIN);
                VARIABLES.TIME_UNIT_FOR_PROCESS_SLEEP.sleep(timeForSleep);
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " was interrupted while sleeping");
            }
        }

        // update process time of last calls
        t -= lastSystemTactsTime.poll();
        lastSystemTactsTime.addLast(timeToExecute);
        t+= timeToExecute;
    }

    private void replacePhysicalPageReferenceFromOldToNewPage (
            VirtualPageMappingToPhysicalPageRecord oldPageFromWorkingSet,
             VirtualPageMappingToPhysicalPageRecord newPageForWorkingSet
    ) {
        oldPageFromWorkingSet.setPrecedenceBit(false);
        newPageForWorkingSet.setPrecedenceBit(true);
        PhysicalPage physicalPage = oldPageFromWorkingSet.getPhysicalPage();
        oldPageFromWorkingSet.setPhysicalPage(null);
        newPageForWorkingSet.setPhysicalPage(physicalPage);
        workingSet.remove(oldPageFromWorkingSet.getId());
        workingSet.add(newPageForWorkingSet.getId());
    }

    private Integer getRandomPageAddressFromWorkingSet() throws PageAbsenceException {
        if (workingSet.size() > 0) {
            return workingSet.get(random.nextInt(workingSet.size()));
        } else {
            throw new PageAbsenceException("There is no pages in working set yet");
        }
    }

    private Integer getRandomPageAddressNotFromWorkingSet() {
        Integer pageCalledAddress;
        do {
            pageCalledAddress = random.nextInt(records.size());
        } while (workingSet.contains(pageCalledAddress));
        return pageCalledAddress;
    }
}
