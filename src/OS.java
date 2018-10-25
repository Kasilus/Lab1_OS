import java.util.List;
import java.util.concurrent.TimeUnit;

public class OS {

    static class VARIABLES {
        public final static Integer PHYSICAL_PAGE_AMOUNT = 256;
        public final static TimeUnit TIME_UNIT_FOR_PROCESS = TimeUnit.MILLISECONDS;
        public final static Integer NEED_MEMORY_FOR_PROCESS_MIN = 10;
        public final static Integer TIME_FOR_PROCESS_MIN = 100;
        public final static Integer TIME_FOR_PROCESS_MAX = 5000;
    }

    PhysicalPageManager physicalPageManager;

    public OS() {
        physicalPageManager = new PhysicalPageManager();
    }

    public List<PhysicalPage> getPhysicalPages() {
        return physicalPageManager.getPhysicalPages();
    }

    public PhysicalPage getFreePhysicalPage() {
        return physicalPageManager.getFreePhysicalPage();
    }

}
