package nl.growguru.app.api.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.models.plantspaces.Space;
import nl.growguru.app.responses.SpaceResponse;
import nl.growguru.app.services.SpaceService;
import nl.growguru.app.views.GrowGuruViews;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/space")

@RequiredArgsConstructor

@SecurityRequirement(name = "Authorization")
public class SpaceController {

    private final SpaceService spaceService;

    @GetMapping
    public List<SpaceResponse> getUserSpaces() {
        return spaceService.getUserSpaces();
    }

    @JsonView(GrowGuruViews.SpaceFilter.class)
    @GetMapping("/{spaceId}")
    public Space getUserSpace(@PathVariable UUID spaceId) {
        return spaceService.getSelectedSpace(spaceId);
    }

    @JsonView(GrowGuruViews.SpaceFilter.class)
    @PutMapping
    public Space addSpace(@RequestBody @Valid Space.CreateSpaceDto createSpaceDto) {
        return spaceService.createSpace(createSpaceDto);
    }

    @JsonView(GrowGuruViews.SpaceFilter.class)
    @PutMapping("/{spaceId}")
    public Space updateSpace(
            @PathVariable UUID spaceId,
            @RequestBody Space.UpdateSpaceDto updateSpaceDto
    ) {
        return spaceService.updateSpace(spaceId, updateSpaceDto);
    }

    @DeleteMapping("/{spaceId}")
    public void deleteSpace(@PathVariable UUID spaceId) {
        spaceService.deleteSpace(spaceId);
    }

    @JsonView(GrowGuruViews.SpaceMemberFilter.class)
    @GetMapping("/{spaceId}/members")
    public Set<GrowGuru> getMembers(@PathVariable UUID spaceId) {
        return spaceService.getSelectedSpace(spaceId).getAllMembers();
    }

    @JsonView(GrowGuruViews.SpaceFilter.class)
    @PutMapping("/{spaceId}/members/{username}")
    public Space addMember(@PathVariable UUID spaceId, @PathVariable String username) {
        return spaceService.addMemberToSpace(spaceId, username);
    }

    @DeleteMapping("/{spaceId}/members/{username}")
    public void removeMember(@PathVariable UUID spaceId, @PathVariable String username) {
        spaceService.removeMember(spaceId, username);
    }

}
