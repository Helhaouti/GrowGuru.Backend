package nl.growguru.app.services;

import java.util.UUID;
import nl.growguru.app.api.rest.SpaceController;
import nl.growguru.app.models.plantspaces.Space;
import nl.growguru.app.repositories.GrowGuruRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {
    @Mock
    private SpaceService spaceService;
    @Mock
    private GrowGuruRepository growGuruRepository;
    @InjectMocks
    private SpaceController spaceController;

    @Test
    @DisplayName("Find space from the current user")
    void getSelectedSpace() {
        Space space = new Space();
        UUID uuid = UUID.randomUUID();
        space.setId(uuid);

        when(spaceService.getSelectedSpace(uuid)).thenReturn(space);

        Space result = spaceController.getUserSpace(uuid);

        Assertions.assertEquals(space, result);
    }

    @Test
    @DisplayName("Create Space for user")
    void createSpaceTest() {
        Space.CreateSpaceDto space1 = new Space.CreateSpaceDto("space1");

        when(spaceService.createSpace(space1)).thenReturn(space1.toSpace(growGuruRepository::findByUsername));

        Space result = spaceController.addSpace(space1);

        Assertions.assertEquals(space1.getName(), result.getName());
    }

    @Test
    @DisplayName("Delete space from user by space id")
    void deleteSpace() {
        Space.CreateSpaceDto space1 = new Space.CreateSpaceDto("space1");

        when(spaceService.createSpace(space1)).thenReturn(space1.toSpace(growGuruRepository::findByUsername));

        Space result = spaceController.addSpace(space1);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(space1.getName(), result.getName());

        spaceController.deleteSpace(space1.toSpace(growGuruRepository::findByUsername).getId());
        Space space2 = spaceController.getUserSpace(space1.toSpace(growGuruRepository::findByUsername).getId());

        Assertions.assertNull(space2);
    }
}