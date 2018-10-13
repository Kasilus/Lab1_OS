import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Process implements Runnable {

    static class VARIABLES {
        public final static TimeUnit TIME_UNIT_FOR_PROCESS_SLEEP = TimeUnit.MILLISECONDS;
        public final static Integer TIME_FOR_PROCESS_SLEEP_MIN = 1;
        public final static Integer TIME_FOR_PROCESS_SLEEP_MAX = 5;
    }

    private List<VirtualPageMappingToPhysicalPageRecord> records;
    private Integer needMemoryAtStart;
    private static Random random;
    private List<Integer> workingSet;

    // pass PID and set to thread
    public Process(Integer pid, Integer needMemoryAtStart) {
        Thread.currentThread().setName("Process-" + pid);
        this.needMemoryAtStart = needMemoryAtStart;
        initRecords();
        this.workingSet = new ArrayList<>();
    }

    private void initRecords() {
        this.records = new ArrayList<>();
        for (int i = 0; i < this.needMemoryAtStart; i++) {
            VirtualPageMappingToPhysicalPageRecord record = new VirtualPageMappingToPhysicalPageRecord();
            records.add(record);
        }
    }

    @Override
    public void run() {

        // process sleeping
        try {
            Integer timeForSleep = VARIABLES.TIME_FOR_PROCESS_SLEEP_MIN + random.nextInt(VARIABLES.TIME_FOR_PROCESS_SLEEP_MAX - VARIABLES.TIME_FOR_PROCESS_SLEEP_MIN);
            VARIABLES.TIME_UNIT_FOR_PROCESS_SLEEP.sleep(timeForSleep);
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " was interrupted while sleeping");
        }

        Integer pageCalledAddress;
        // call some virtual page
        try {
            if (random.nextDouble() > 0.1) {
                pageCalledAddress = getRandomPageAddressFromWorkingSet();
            } else {
                pageCalledAddress = getRandomPageAddressNotFromWorkingSet();
                throw new PageAbsenceException("Virtual page with address " + pageCalledAddress + " is not in working set");
            }
        } catch (PageAbsenceException e) {
            //TODO: algorithm with page swapping, if there is no place in working set
            //prodgap for page initialization
            pageCalledAddress = 0;
        }

        // read or write page
        VirtualPageMappingToPhysicalPageRecord record = records.get(pageCalledAddress);
        if (random.nextDouble() > 0.5) {
            record.setReadBit(true);
        } else {
            record.setModificationBit(true);
        }

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
