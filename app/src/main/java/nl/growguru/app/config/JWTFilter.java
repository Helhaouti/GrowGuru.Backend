package nl.growguru.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.exceptions.ConflictException;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.services.GrowGuruService;
import nl.growguru.app.utils.JWTUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import static nl.growguru.app.utils.JWTUtil.JWT_TOKEN_PREFACE;
import static org.springframework.util.StringUtils.hasLength;

/**
 * A filter to capture JWT tokens embedded in HTTP Authorizations header from incoming requests.
 * If a JWT is present it validates it and authenticates the user.
 */
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final GrowGuruService growGuruService;
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {
        var authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);

        // Check if a JWT is not present in the header, and pass to the next filter.
        if (!hasLength(authHeader) || !authHeader.startsWith(JWT_TOKEN_PREFACE)) {
            chain.doFilter(req, res);
            return;
        }

        var token = authHeader.replace(JWT_TOKEN_PREFACE, "").trim();

        // Check if the provided JWT token is invalid, and pass to the next filter.
        if (!jwtUtil.validate(token)) {
            chain.doFilter(req, res);
            return;
        }

        // Assign the user to the spring security context, and pass to the next filter.
        GrowGuru growGuru = growGuruService.findByUserName(jwtUtil.getUsernameOf(token));

        if (growGuru == null) {
            chain.doFilter(req, res);
            return;
        }

        if (!growGuru.isEnabled()){
            throw new ConflictException("Your account is disabled. Check your email for the activation link.");
        }

        var authToken = new UsernamePasswordAuthenticationToken(
                growGuru,
                null,
                Objects.requireNonNullElse(growGuru.getAuthorities(), List.of())
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        chain.doFilter(req, res);
    }
}