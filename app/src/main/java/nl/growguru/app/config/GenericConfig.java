package nl.growguru.app.config;

import java.time.ZoneId;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

public class GenericConfig {

    public static final String EMAIL_REGEX
            = "[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+" /*
                User part of the email address
                This part matches the beginning of an email address, which consists of alphanumeric characters
                (a-z, A-Z, 0-9) and special characters (!#$%&'*+/=?^_`{|}~-). The plus sign (+) indicates that
                one or more of these characters will be matched.
             */
            + "(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*" /*
                Dot separated parts in user section
                This part allows for dot-separated components in the user part of the email.
                It's enclosed in a non-capturing group (?:) and can repeat zero or more times (*).
                It ensures that each dot-separated part has the same character composition as the
                initial user part.
            */
            + "@" /*
                Separator for user and domain parts
                This is the '@' character that separates the user part from the domain part in an email address.
             */
            + "(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+" /*
                Domain name
                This part matches the domain name of the email. It starts with an alphanumeric character
                ([a-zA-Z0-9]), followed by zero or more alphanumeric characters or hyphens ([a-zA-Z0-9-]*), and
                ends with another alphanumeric character.
                The question mark (?) makes the middle part optional to allow for single-character domains.
                The escaped dot (\\.) represents a literal dot in the domain.
                The plus sign (+) indicates that one or more domains separated by dots will be matched.
             */
            + "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?" /*
                Top-level domain
                This final part matches the top-level domain (TLD) of the email. It follows the same pattern as
                the domain name, starting and ending with an alphanumeric character, with zero or more
                alphanumeric characters or hyphens in between.
                The TLD is required to be at least one character long.
            */;

    public static final String PASSWORD_REGEX
            = "(?=.*[A-Z])"    // At least one uppercase letter
            + "(?=.*[a-z])"  // At least one lowercase letter
            + "(?=.*\\d)"    // At least one digit
            + ".{8,}";       // At least 8 characters in total


    @Getter
    @Value("${application.frontend.hostname}")
    private String frontendHostname;

    @Getter
    @Value("${application.backend.hostname}")
    private String backendHostname;

    @Value("${application.timezone.tzdb_id}")
    private String tzdbId;


    public ZoneId getDefaultZoneId() {
        return ZoneId.of(tzdbId);
    }

}
