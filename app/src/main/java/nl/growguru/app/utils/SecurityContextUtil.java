package nl.growguru.app.utils;

import java.util.Objects;
import nl.growguru.app.models.auth.GrowGuru;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextUtil {

    public static Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static GrowGuru getCurrentGrowGuru() {
        return getCurrentGrowGuru(true);
    }

    public static GrowGuru getCurrentGrowGuru(boolean nullable) {
        var currGrowGuru = getAuth() == null ? null : (GrowGuru) getAuth().getPrincipal();

        return nullable ? currGrowGuru : Objects.requireNonNull(currGrowGuru);
    }

    private SecurityContextUtil() {
    }

}
