import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Lab1 {

    public static void main(String[] args) {

        OS os = new OS();
        List<PhysicalPage> physicalPages = os.getPhysicalPages();
        // Static number of processes. Maybe, latter it will be dynamic number with process killing and init. ahah-ahahah
        Integer numberOfProcesses = 3;
        // TODO: replace with smth more comfortable
        List<Process> processes = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numberOfProcesses; i++) {
            Integer needMemoryForProcessAtStart = random.nextInt(OS.VARIABLES.PHYSICAL_PAGE_AMOUNT * 5 / 4);
            processes.add(new Process(i, needMemoryForProcessAtStart, os));
        }

        // run processes
        Process currentProcess;
        while (true) {
            // allocate some time for random process
            Integer currentProcessId = random.nextInt(numberOfProcesses);
            currentProcess = processes.get(currentProcessId);
            Integer timeForProcessExecution = OS.VARIABLES.TIME_FOR_PROCESS_MIN + random.nextInt(OS.VARIABLES.TIME_FOR_PROCESS_MAX - OS.VARIABLES.TIME_FOR_PROCESS_MIN);
            currentProcess.run(timeForProcessExecution);
        }

    }
}
