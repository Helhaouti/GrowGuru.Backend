package nl.growguru.app.exceptions;

public class PreConditionFailed extends RuntimeException {

    public PreConditionFailed(String message) {
        super(message);
    }

}