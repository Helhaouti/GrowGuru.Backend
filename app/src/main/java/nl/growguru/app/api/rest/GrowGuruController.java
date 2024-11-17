package nl.growguru.app.api.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.models.plantspaces.Space;
import nl.growguru.app.services.GrowGuruService;
import nl.growguru.app.utils.JWTUtil;
import nl.growguru.app.utils.SecurityContextUtil;
import nl.growguru.app.views.GrowGuruViews;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for handling GrowGuru user-related operations.
 * Provides endpoints for various user management tasks like retrieving user details, updating user information, and deleting a user.
 */
@RestController
@RequestMapping("/api/v1/user")

@Valid

@RequiredArgsConstructor

@SecurityRequirement(name = "Authorization")
public class GrowGuruController {

    private final GrowGuruService growGuruService;
    private final JWTUtil jwtUtil;

    /**
     * Easy method to return current user.
     *
     * @return current GrowGuru user
     */
    @GetMapping
    public GrowGuru getGrowGuru() {
        return SecurityContextUtil.getCurrentGrowGuru();
    }

    /**
     * Retrieves a list of GrowGuru users belonging to a specific space.
     *
     * @param space The space for which to find all users.
     * @return List of GrowGuru users in the given space.
     */
    @GetMapping("/all/{space}")
    public List<GrowGuru> getAllGrowGurusInSpace(@PathVariable Space space) {
        return growGuruService.findAllInSpace(space);
    }

    /**
     * Finds a GrowGuru user by their unique id.
     *
     * @param id The UUID of the user to find.
     * @return The found GrowGuru user, or null if not found.
     */
    @GetMapping("/{id}")
    @JsonView(GrowGuruViews.UserViewFilter.class)
    public GrowGuru findById(@PathVariable UUID id) {
        return growGuruService.findById(id);
    }

    /**
     * Finds a GrowGuru user by their email address.
     *
     * @param email The email address of the user to find.
     * @return The found GrowGuru user, or null if not found.
     */
    @GetMapping("/email/{email}")
    @JsonView(GrowGuruViews.UserViewFilter.class)
    public GrowGuru findByEmail(@PathVariable @NotBlank(message = "is blank.") String email) {
        return growGuruService.findByEmail(email);
    }

    /**
     * Finds a GrowGuru user by their username.
     *
     * @param username The username of the user to find.
     * @return The found GrowGuru user, or null if not found.
     */
    @GetMapping("/username/{username}")
    @JsonView(GrowGuruViews.UserViewFilter.class)
    public GrowGuru findByUsername(@PathVariable() @NotBlank(message = "is blank.") String username) {
        return growGuruService.findByUserName(username);
    }

    /**
     * Updates the parameters of the current GrowGuru user.
     *
     * @param updateDto the potential new parameters of the user (any are optional).
     * @return The updated GrowGuru user.
     */
    @Valid
    @SneakyThrows
    @PutMapping
    public ResponseEntity<Map<String, String>> updateGrowGuruParam(@RequestBody GrowGuru.UpdateDto updateDto) {
        var updatedGrowGuru = growGuruService.update(updateDto);

        return new ResponseEntity<>(
                jwtUtil.generateTokensFor(updatedGrowGuru),
                HttpStatus.ACCEPTED
        );
    }

    /**
     * Deletes the current GrowGuru user.
     */
    @PostMapping("/delete")
    public void deleteGrowGuru() {
        growGuruService.remove(SecurityContextUtil.getCurrentGrowGuru());
    }
}
