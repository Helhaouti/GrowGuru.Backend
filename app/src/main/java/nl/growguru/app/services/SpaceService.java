package nl.growguru.app.services;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.exceptions.ConflictException;
import nl.growguru.app.exceptions.ResourceNotFound;
import nl.growguru.app.models.plantspaces.Space;
import nl.growguru.app.repositories.GrowGuruRepository;
import nl.growguru.app.repositories.SpaceRepository;
import nl.growguru.app.responses.SpaceResponse;
import org.springframework.stereotype.Service;

import static nl.growguru.app.utils.SecurityContextUtil.getCurrentGrowGuru;

/**
 * TODO: Implement the maintenance of the Space representation invariant: admins ⊆ members, and their privelages.
 */
@Transactional
@Service
@RequiredArgsConstructor
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final GrowGuruRepository growGuruRepository;
    private static final String SPACE_NOT_FOUND = "Couldn't find space";
    private static final String USER_NOT_FOUND = "Couldn't find user";
    private static final String DUPLICATE_MEMBER_MESSAGE = "Member is already part of the space";

    public Space getSelectedSpace(UUID spaceId) {
        return getCurrentGrowGuru().getAllSpaces().stream()
                .filter(a -> a.getId().equals(spaceId))
                .findAny()
                .orElseThrow(() -> new ResourceNotFound(SPACE_NOT_FOUND));
    }

    public List<SpaceResponse> getUserSpaces() {
        return getCurrentGrowGuru().getAllSpaces().stream().map(SpaceResponse::new).toList();
    }

    public Space createSpace(Space.CreateSpaceDto req) {
        req.getAdminUsernames().add(getCurrentGrowGuru().getUsername());

        var space = spaceRepository.save(req.toSpace(growGuruRepository::findByUsername));

        space.getMembers().forEach(guru -> guru.getSpaces().add(space));
        space.getAdmins().forEach(admin -> admin.getAdminSpaces().add(space));

        return space;
    }

    public Space addMemberToSpace(UUID spaceId, String username) {
        var space = getSelectedSpace(spaceId);

        growGuruRepository
                .findByUsername(username)
                .ifPresentOrElse(
                        g -> {
                            if (!space.getMembers().add(g) || space.getAdmins().contains(g))
                                throw new ConflictException(DUPLICATE_MEMBER_MESSAGE);
                            else g.getSpaces().add(space);
                        },
                        () -> {
                            throw new ResourceNotFound(USER_NOT_FOUND);
                        }
                );

        return spaceRepository.save(space);
    }

    public void deleteSpace(UUID spaceId) {
        var space = getSelectedSpace(spaceId);

        space.getMembers().forEach(guru -> guru.getSpaces().remove(space));
        space.getAdmins().forEach(admin -> admin.getAdminSpaces().remove(space));

        spaceRepository.delete(space);
    }

    public void removeMember(UUID spaceId, String username) {
        var space = getSelectedSpace(spaceId);

        var spaceMember = space.getAllMembers().stream()
                .filter(member -> member.getUsername().equals(username))
                .findAny().orElseThrow(() -> new ResourceNotFound(USER_NOT_FOUND));

        boolean removedFromMembers = space.getMembers().remove(spaceMember);
        boolean removedFromAdmins = space.getAdmins().remove(spaceMember);

        if (removedFromMembers) spaceMember.getSpaces().remove(space);
        if (removedFromAdmins) spaceMember.getAdminSpaces().remove(space);

        if (removedFromMembers || removedFromAdmins) {
            spaceRepository.save(space);
            growGuruRepository.save(spaceMember);
        }

        if (space.getMembers().isEmpty() && space.getAdmins().isEmpty()) spaceRepository.delete(space);
    }

    public Space updateSpace(UUID spaceId, Space.UpdateSpaceDto updateSpaceDto) {
        Space space = getSelectedSpace(spaceId);

        space.setName(updateSpaceDto.getName());

        spaceRepository.save(space);

        return space;
    }

}
