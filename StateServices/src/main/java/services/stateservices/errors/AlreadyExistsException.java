package services.stateservices.errors;

public class AlreadyExistsException extends Exception {
    public AlreadyExistsException(String message) {
        super(message);
    }
    
    public AlreadyExistsException() {
        super("Already exists!");
    }
}