import java.util.ArrayList;
import java.util.List;

public class PhysicalPageManager {

    List<PhysicalPage> physicalPages = null;

    public PhysicalPageManager() {
        initPhysicalPages();
    }

    public List<PhysicalPage> getPhysicalPages() {
        if (physicalPages == null) {
            initPhysicalPages();
        }

        return physicalPages;
    }

    private void initPhysicalPages() {
        physicalPages = new ArrayList<>();
        for (int i = 0; i < OS.VARIABLES.PHYSICAL_PAGE_AMOUNT; i++) {
            physicalPages.add(new PhysicalPage());
        }
    }

}
