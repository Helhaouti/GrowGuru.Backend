package nl.growguru.app.utils;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class EnvUtil {

    public static String getBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
    }

    private EnvUtil() {
    }

}
