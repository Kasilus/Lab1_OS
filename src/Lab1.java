import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Lab1 {

    public static void main(String[] args) {

        OS os = new OS();
        List<PhysicalPage> physicalPages = os.getPhysicalPages();
        // Static number of processes. Maybe, latter it will be dynamic number with process killing and init. ahah-ahahah
        Integer numberOfProcesses = 3;
        // TODO: replace with smth more comfortable
        List<Runnable> processes = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numberOfProcesses; i++) {
            Integer needMemoryForProcessAtStart = random.nextInt(OS.VARIABLES.PHYSICAL_PAGE_AMOUNT * 5 / 4);
            processes.add(new Process(i, needMemoryForProcessAtStart));
        }

    }
}
