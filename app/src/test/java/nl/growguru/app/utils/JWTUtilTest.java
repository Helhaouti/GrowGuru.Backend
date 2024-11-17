package nl.growguru.app.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.SneakyThrows;
import nl.growguru.app.models.auth.GrowGuru;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static nl.growguru.app.utils.JWTUtil.JWT_CLAIM_KEY_EMAIL;
import static nl.growguru.app.utils.JWTUtil.JWT_CLAIM_KEY_USERNAME;
import static nl.growguru.app.utils.JWTUtil.JWT_CLAIM_KEY_USER_ID;
import static nl.growguru.app.utils.JWTUtil.JWT_TOKEN_PREFACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.profiles.active=test"
)
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(value = PER_METHOD)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class JWTUtilTest {

    @Value("${jwt.issuer:MyOrganisation}")
    private String issuer;
    @Value("${jwt.pass-phrase}")
    private String passPhrase;

    @Autowired
    private JWTUtil jwtUtil;

    @Test
    void whenGenerateTokenForValidUser_thenTokenIsWellFormed() {
        var token = generateTokenFor(GrowGuru.builder().username("testUser").build(), 10000L);
        assertNotNull(token, "Generated token should not be null");
        assertTrue(token.startsWith(JWT_TOKEN_PREFACE), "Token should start with Bearer prefix");
    }

    @Test
    void whenValidateValidToken_thenReturnsTrue() {
        var token = generateTokenFor(GrowGuru.builder().username("testUser").build(), 10000L);

        assertTrue(
                jwtUtil.validate(token.replace(JWT_TOKEN_PREFACE, "").trim()),
                "Valid token should be validated successfully"
        );
    }

    @Test
    void whenValidateExpiredToken_thenReturnsFalse() {
        var token = generateTokenFor(GrowGuru.builder().username("testUser").build(), -10000L);
        assertFalse(
                jwtUtil.validate(token.replace(JWT_TOKEN_PREFACE, "").trim()),
                "Expired token should not be validated"
        );
    }

    @Test
    void whenValidateMalformedToken_thenReturnsFalse() {
        var token = "malformedToken.dsfdsfewr3qfewrfw.wevervp[p]qf.dslk";
        assertFalse(
                jwtUtil.validate(token.replace(JWT_TOKEN_PREFACE, "").trim()),
                "Malformed token should not be validated"
        );
    }

    @Test
    void whenGetUsernameOfValidToken_thenReturnsCorrectUsername() {
        var token = generateTokenFor(GrowGuru.builder().username("testUser").build(), 1000L);

        assertEquals(
                "testUser",
                jwtUtil.getUsernameOf(token.replace(JWT_TOKEN_PREFACE, "").trim()),
                "Extracted username should match the one in the token"
        );
    }

    @Test
    void whenGenerateTokenForNullUser_thenThrowsException() {
        assertThrows(
                Exception.class,
                () -> jwtUtil.generateTokensFor(null),
                "Generating token for null user should throw exception"
        );
    }

    @SneakyThrows
    String generateTokenFor(GrowGuru growGuru, long duration) {
        return JWT_TOKEN_PREFACE + Jwts.builder()
                .claim(JWT_CLAIM_KEY_USER_ID, growGuru.getId())
                .claim(JWT_CLAIM_KEY_USERNAME, growGuru.getUsername())
                .claim(JWT_CLAIM_KEY_EMAIL, growGuru.getEmail())

                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + duration))

                .signWith(JWTUtil.getKey(passPhrase), SignatureAlgorithm.HS256)
                .compact();
    }

}