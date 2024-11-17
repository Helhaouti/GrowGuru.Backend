package nl.growguru.app.exceptions;

public class ResourceNotFound extends RuntimeException {

    public ResourceNotFound(String message) {
        super(message);
    }

    public ResourceNotFound(String type, String field, String query) {
        super("No %s with the %s: %s found.".formatted(type, field, query));
    }

}