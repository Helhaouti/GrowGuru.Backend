package nl.growguru.app.api.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.exceptions.BadRequest;
import nl.growguru.app.exceptions.UnauthorizedException;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.services.GrowGuruService;
import nl.growguru.app.utils.JWTUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The central place for the authentication of outside-users. Upon a successful login or registration it also
 * provides a JWToken, for future requests.
 */
@RestController
@RequestMapping(AuthenticateController.AUTH_API_BASE)

@RequiredArgsConstructor
public class AuthenticateController {

    public static final String AUTH_API_BASE = "/api/v1/auth";
    private final AuthenticationManager authManager;
    private final GrowGuruService growGuruService;
    private final JWTUtil jwtUtil;

    /**
     * Checks whether the provided loginDto are valid, and authenticates the user, by returning a JWT.
     *
     * @param loginDto An object containing username and password attributes.
     * @return The user that belongs to the given loginDto and a JWT for use in authentication.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid GrowGuru.LoginDto loginDto) {
        Authentication authenticate;

        try {
            authenticate = authManager.authenticate(loginDto.toAuthToken());
        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException("Email and/or password is not valid.");
        } catch (DisabledException ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        var growGuru = (GrowGuru) authenticate.getPrincipal();

        return new ResponseEntity<>(
                jwtUtil.generateTokensFor(growGuru),
                HttpStatus.ACCEPTED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @RequestBody
            @Valid @NotBlank(message = "refresh token not provided in body")
            String refreshToken
    ) {
        if (!jwtUtil.validate(refreshToken) || !jwtUtil.isRefreshToken(refreshToken))
            throw new BadRequest("The provided token is invalid");

        var growGuru = growGuruService.findByUserName(jwtUtil.getUsernameOf(refreshToken));

        return new ResponseEntity<>(
                jwtUtil.generateTokensFor(growGuru),
                HttpStatus.ACCEPTED
        );
    }

    /**
     * Creates a user with given parameters, when all parameters are provided and valid, in the provided object.
     *
     * @param req An object containing the attributes of the to be created User.
     * @return The created User object and a JWT token.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody @Valid GrowGuru.RegisterDto req) {
        var growGuru = growGuruService.create(req);

        return new ResponseEntity<>(
                jwtUtil.generateTokensFor(growGuru),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/verification/{code}")
    public String verify(@PathVariable String code) {
        return growGuruService.verify(code);
    }

}