package nl.growguru.app.config;

import com.stripe.exception.SignatureVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import nl.growguru.app.exceptions.BadRequest;
import nl.growguru.app.exceptions.ConflictException;
import nl.growguru.app.exceptions.ForbiddenResourceAccessed;
import nl.growguru.app.exceptions.PreConditionFailed;
import nl.growguru.app.exceptions.ResourceNotFound;
import nl.growguru.app.exceptions.TooBrokeException;
import nl.growguru.app.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        logger.warn(getExceptionMessage(request, BAD_REQUEST, e.getMessage()), e);

        return new ResponseEntity<>(
                e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .toList(),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(
            DataIntegrityViolationException e,
            HttpServletRequest request
    ) {
        var message = getExceptionMessage(request, CONFLICT, e.getMostSpecificCause().getMessage());

        logger.warn(message, e);

        return new ResponseEntity<>(
                message.contains("Duplicate entry") ? "'" + extractDuplicateValue(e.getMostSpecificCause().getMessage())
                        + "' is already taken. Please use a different value." : message,
                CONFLICT
        );
    }

    private String extractDuplicateValue(String message) {
        String[] parts = message.split("'");
        if (parts.length > 1) {
            return parts[1]; // Assuming the value is between the first pair of single quotes
        }
        return "unknown"; // Fallback value
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handleConflictException(Exception e, HttpServletRequest request) {
        return buildErrorResponse(request, CONFLICT, e);
    }

    @ExceptionHandler(PreConditionFailed.class)
    public ResponseEntity<String> handlePreconditionError(Exception e, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.PRECONDITION_FAILED, e);
    }

    @ExceptionHandler(ForbiddenResourceAccessed.class)
    public ResponseEntity<String> handleForbiddenResourceAccess(Exception e, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.FORBIDDEN, e);
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<String> handleNotFound(Exception e, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler({
            UnauthorizedException.class,
            HttpClientErrorException.Unauthorized.class
    })
    public ResponseEntity<String> handleUnauthorizedAcces(Exception e, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.UNAUTHORIZED, e);
    }

    @ExceptionHandler({
            HttpClientErrorException.BadRequest.class,
            SignatureVerificationException.class,
            BadRequest.class,
            URISyntaxException.class
    })
    public ResponseEntity<String> handleBadRequest(Exception e, HttpServletRequest request) {
        return buildErrorResponse(request, BAD_REQUEST, e);
    }

    @ExceptionHandler(TooBrokeException.class)
    public ResponseEntity<String> handleTooBrokeRequest(Exception e, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.I_AM_A_TEAPOT, e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleInternalServerError(Exception e, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    private ResponseEntity<String> buildErrorResponse(
            HttpServletRequest request,
            HttpStatus status,
            Throwable e
    ) {
        var message = getExceptionMessage(request, status, e.getMessage());

        if (status.is5xxServerError()) logger.error(message, e);
        else logger.warn(message, e);

        return ResponseEntity.status(status).body(e.getMessage());
    }

    private String getRequestURL(HttpServletRequest request) {
        return request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    }

    private String getExceptionMessage(
            HttpServletRequest request,
            HttpStatus status,
            String exceptionMessage
    ) {
        return String.format("Request '%s' failed with status %s: '%s'",
                getRequestURL(request),
                status,
                exceptionMessage
        );
    }

}
