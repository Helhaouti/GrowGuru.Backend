package nl.growguru.app.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import nl.growguru.app.models.auth.GrowGuru;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * A utility to perform operations relating to JWT tokens.
 */
@Component
public class JWTUtil {

    public static final long JWT_ACCESS_DURATION_MS = 1000L * 60 * 60 * 2;  // 2 hours
    public static final long JWT_REFRESH_DURATION_MS = 1000L * 60 * 60 * 24 * 30 * 6;  // 6 months on average

    public static final String JWT_TOKEN_PREFACE = "Bearer ";

    public static final String JWT_CLAIM_KEY_USER_ID = "user-id";
    public static final String JWT_CLAIM_KEY_USERNAME = "username";
    public static final String JWT_CLAIM_KEY_EMAIL = "email";

    private static final Logger log = LoggerFactory.getLogger(JWTUtil.class);

    @Value("${jwt.issuer:MyOrganisation}")
    private String issuer;
    @Value("${jwt.pass-phrase}")
    private String passPhrase;

    public Map<String, String> generateTokensFor(GrowGuru growGuru) {
        return Map.of(
                "accessToken", generateAccessTokenFor(growGuru),
                "refreshToken", generateRefreshTokenFor(growGuru)
        );
    }

    @SneakyThrows
    private String generateAccessTokenFor(GrowGuru growGuru) {
        return Jwts.builder()
                .claim(JWT_CLAIM_KEY_USER_ID, growGuru.getId())
                .claim(JWT_CLAIM_KEY_USERNAME, growGuru.getUsername())
                .claim(JWT_CLAIM_KEY_EMAIL, growGuru.getEmail())

                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_ACCESS_DURATION_MS))

                .signWith(JWTUtil.getKey(passPhrase), SignatureAlgorithm.HS256)
                .compact();
    }

    @SneakyThrows
    private String generateRefreshTokenFor(GrowGuru growGuru) {
        return Jwts.builder()
                .claim(JWT_CLAIM_KEY_USER_ID, growGuru.getId())
                .claim(JWT_CLAIM_KEY_USERNAME, growGuru.getUsername())
                .claim(JWT_CLAIM_KEY_EMAIL, growGuru.getEmail())
                .claim("type", "refresh")

                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_REFRESH_DURATION_MS))

                .signWith(JWTUtil.getKey(passPhrase), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Checks whether the provided jwt token is valid.
     *
     * @param token The token to be validated.
     * @return Whether the token is valid.
     */
    public boolean validate(String token) {
        try {
            parseToken(token, passPhrase);

            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature - {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token - {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token - {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token - {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty - {}", e.getMessage());
        }

        return false;
    }

    /**
     * Extracts the username from the payload of the JWToken.
     *
     * @param token The token containing the username.
     * @return the username of the authenticated user.
     */
    public String getUsernameOf(String token) {
        return (String) parseToken(token, passPhrase)
                .getBody()
                .get(JWT_CLAIM_KEY_USERNAME);
    }

    public boolean isRefreshToken(String token) {
        return Objects
                .requireNonNullElse(
                        parseToken(token, passPhrase).getBody().get("type"),
                        ""
                ).equals("refresh");
    }

    /**
     * Generates a key based on secret and unique string.
     *
     * @param passPhrase A string that is secret and unique
     * @return A key that can be used to en-/decrypt a JWT
     */
    public static Key getKey(String passPhrase) {
        byte[] hmacKey = passPhrase.getBytes(StandardCharsets.UTF_16);
        return new SecretKeySpec(hmacKey, SignatureAlgorithm.HS512.getJcaName());
    }

    /**
     * Parses the body of a JWT in string format, with the provided passphrase, to a JWS object.
     *
     * @param token      A JWT in string format.
     * @param passPhrase A string that is secret and unique
     * @return The body of the JWT in a JWS object.
     */
    private static Jws<Claims> parseToken(String token, String passPhrase) {
        return Jwts.parserBuilder()
                .setSigningKey(JWTUtil.getKey(passPhrase))
                .build()
                .parseClaimsJws(token);
    }

    /**
     * Creates a password encoder.
     *
     * @return The created password encoder.
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        final int SALT_LENGTH = 16;
        final int HASH_LENGTH = 32;
        final int PARALLELISM = 1;
        final int MEMORY = 4096;
        final int ITERATIONS = 3;

        return new Argon2PasswordEncoder(SALT_LENGTH, HASH_LENGTH, PARALLELISM, MEMORY, ITERATIONS);
    }
}