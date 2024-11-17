package nl.growguru.app.api.rest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.models.plantspaces.Space;
import nl.growguru.app.services.GrowGuruService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for GrowGuruController.
 * This class provides various test cases to ensure the functionality and reliability of the GrowGuruController class.
 * The test cases cover a range of scenarios, including standard operations, edge cases, and error handling.
 */
class GrowGuruControllerTest {

    @Mock
    private GrowGuruService growGuruService;
    @InjectMocks
    private GrowGuruController growGuruController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void Test_1_getAllGrowGurusInSpace() {
        // Arrange
        Space mockSpace = new Space();
        List<GrowGuru> expectedGrowGurus = List.of(new GrowGuru()); // Use mock data
        when(growGuruService.findAllInSpace(mockSpace)).thenReturn(expectedGrowGurus);

        // Act
        List<GrowGuru> result = growGuruController.getAllGrowGurusInSpace(mockSpace);

        // Assert
        assertNotNull(result);
        assertEquals(expectedGrowGurus.size(), result.size());
        verify(growGuruService).findAllInSpace(mockSpace);
    }

    @Test //Right-BICEP, Correct
    void Test_1_getAllGrowGurusInSpace_WhenNoGurus_ReturnsEmptyList() {
        // Arrange
        Space mockSpace = new Space();
        List<GrowGuru> expectedEmptyList = Collections.emptyList();
        when(growGuruService.findAllInSpace(mockSpace)).thenReturn(expectedEmptyList);

        // Act
        List<GrowGuru> result = growGuruController.getAllGrowGurusInSpace(mockSpace);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(growGuruService).findAllInSpace(mockSpace);
    }

    @Test //Right-BICEP, Correct
    void Test_1_getAllGrowGurusInSpace_WhenMultipleGurus_ReturnsCorrectList() {
        // Arrange
        Space mockSpace = new Space();
        List<GrowGuru> expectedGurus = List.of(new GrowGuru(), new GrowGuru());
        when(growGuruService.findAllInSpace(mockSpace)).thenReturn(expectedGurus);

        // Act
        List<GrowGuru> result = growGuruController.getAllGrowGurusInSpace(mockSpace);

        // Assert
        assertNotNull(result);
        assertEquals(expectedGurus.size(), result.size());
        verify(growGuruService).findAllInSpace(mockSpace);
    }

    @Test //FIRST, Correct
    void Test_1_getAllGrowGurusInSpace_WhenSpaceIsNull_NothingShouldBeFoundInList() {
        // Arrange
        Space nullSpace = null;
        List<GrowGuru> expectedGurus = List.of();

        // Act & Assert
        assertEquals(expectedGurus, growGuruController.getAllGrowGurusInSpace(nullSpace));
    }


    @Test
    void Test_2_findByIdTest() {
        // Arrange
        UUID id = UUID.randomUUID();
        GrowGuru expectedGrowGuru = new GrowGuru();
        expectedGrowGuru.setId(id);
        when(growGuruService.findById(id)).thenReturn(expectedGrowGuru);

        // Act
        GrowGuru result = growGuruController.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(expectedGrowGuru, result);
        verify(growGuruService).findById(id);
    }

    @Test //Right-BICEP, Correct
    void Test_2_findById_WhenIdDoesNotExist_ReturnsNull() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(growGuruService.findById(nonExistingId)).thenReturn(null);

        // Act
        GrowGuru result = growGuruController.findById(nonExistingId);

        // Assert
        assertNull(result);
        verify(growGuruService).findById(nonExistingId);
    }

    @Test //FIRST, Correct
    void Test_2_findById_WhenServiceThrowsException_ControllerThrowsException() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(growGuruService.findById(id)).thenThrow(new RuntimeException("Service exception"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> growGuruController.findById(id));
        verify(growGuruService).findById(id);
    }

    @Test //Right-BICEP, Correct
    void Test_2_findById_WithEdgeCaseId_ReturnsGrowGuru() {
        // Arrange
        UUID edgeCaseId = new UUID(0L, 0L); // Example of an edge case UUID
        GrowGuru expectedGrowGuru = new GrowGuru();
        expectedGrowGuru.setId(edgeCaseId);
        when(growGuruService.findById(edgeCaseId)).thenReturn(expectedGrowGuru);

        // Act
        GrowGuru result = growGuruController.findById(edgeCaseId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedGrowGuru, result);
        verify(growGuruService).findById(edgeCaseId);
    }


    @Test
    void Test_3_findByEmailTest() {
        // Arrange
        String email = "test@example.com";
        GrowGuru expectedGrowGuru = new GrowGuru();
        expectedGrowGuru.setEmail(email);
        when(growGuruService.findByEmail(email)).thenReturn(expectedGrowGuru);

        // Act
        GrowGuru result = growGuruController.findByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(expectedGrowGuru, result);
        verify(growGuruService).findByEmail(email);
    }

    @Test //Right-BICEP, Correct
    void Test_3_findByEmail_WhenEmailDoesNotExist_ReturnsNull() {
        // Arrange
        String nonExistingEmail = "nonexistent@example.com";
        when(growGuruService.findByEmail(nonExistingEmail)).thenReturn(null);

        // Act
        GrowGuru result = growGuruController.findByEmail(nonExistingEmail);

        // Assert
        assertNull(result);
        verify(growGuruService).findByEmail(nonExistingEmail);
    }

    @Test //FIRST, Correct
    void Test_3_findByEmail_WhenEmailIsNull_NoGrowGuruIsFound() {
        // Arrange
        String nullEmail = null;

        // Act & Assert
        assertNull(growGuruController.findByEmail(nullEmail));
    }

    @Test //FIRST, Correct
    void Test_3_findByEmail_WhenEmailIsEmpty_NoGrowGuruIsFound() {
        // Arrange
        String emptyEmail = "";

        // Act & Assert
        assertNull(growGuruController.findByEmail(emptyEmail));
    }

    @Test
    void Test_4_findByUsernameTest() {
        // Arrange
        String username = "testUser";
        GrowGuru expectedGrowGuru = new GrowGuru();
        expectedGrowGuru.setUsername(username);
        when(growGuruService.findByUserName(username)).thenReturn(expectedGrowGuru);

        // Act
        GrowGuru result = growGuruController.findByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(expectedGrowGuru, result);
        verify(growGuruService).findByUserName(username);
    }

    @Test //Right-BICEP, Correct
    void Test_4_findByUsername_WhenUsernameDoesNotExist_NoGrowGuruIsFound() {
        // Arrange
        String nonExistingUsername = "nonexistentUser";
        when(growGuruService.findByUserName(nonExistingUsername)).thenReturn(null);

        // Act
        GrowGuru result = growGuruController.findByUsername(nonExistingUsername);

        // Assert
        assertNull(result);
        verify(growGuruService).findByUserName(nonExistingUsername);
    }

    @Test //FIRST, Correct
    void Test_4_findByUsername_WhenUsernameIsNull_NoGrowGuruIsFound() {
        // Arrange
        String nullUsername = null;

        // Act & Assert
        assertNull(growGuruController.findByUsername(nullUsername));
    }

    @Test
    void Test_4_findByUsername_WhenUsernameIsEmpty_NoGrowGuruIsFound() {
        // Arrange
        String emptyUsername = "";

        // Act & Assert
        assertNull(growGuruController.findByUsername(emptyUsername));
    }

    @Test
    void Test_4_findByUsername_WithSpecialCharactersInUsername_ReturnsGrowGuru() {
        // Arrange
        String specialUsername = "user@name#123";
        GrowGuru expectedGrowGuru = new GrowGuru();
        expectedGrowGuru.setUsername(specialUsername);
        when(growGuruService.findByUserName(specialUsername)).thenReturn(expectedGrowGuru);

        // Act
        GrowGuru result = growGuruController.findByUsername(specialUsername);

        // Assert
        assertNotNull(result);
        assertEquals(expectedGrowGuru, result);
        verify(growGuruService).findByUserName(specialUsername);
    }
}