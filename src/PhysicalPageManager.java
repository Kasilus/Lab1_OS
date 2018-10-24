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

    public PhysicalPage getFreePhysicalPage() {
        for (int i = 0; i < physicalPages.size(); i++) {
            if (physicalPages.get(i).isFree) {
                return physicalPages.get(i);
            }
        }

        // no physical pages more
        return null;
    }
}
