package nl.growguru.app.exceptions;

public class ForbiddenResourceAccessed extends RuntimeException {

    public ForbiddenResourceAccessed(String message) {
        super(message);
    }

}