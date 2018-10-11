import java.util.ArrayList;
import java.util.List;

public class OS {

    class VARIABLES {
        public final static int PHYSICAL_PAGE_AMOUNT = 256;
    }

    PhysicalPageManager physicalPageManager;

    public OS() {
        physicalPageManager = new PhysicalPageManager();
    }

    public List<PhysicalPage> getPhysicalPages() {
        return physicalPageManager.getPhysicalPages();
    }


}
