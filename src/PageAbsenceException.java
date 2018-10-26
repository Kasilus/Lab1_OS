public class PageAbsenceException extends Exception {

    public PageAbsenceException(String message) {
        super("Page absence. " + message);
    }
}
