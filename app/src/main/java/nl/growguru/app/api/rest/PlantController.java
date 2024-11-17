package nl.growguru.app.api.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.models.plantspaces.Plant;
import nl.growguru.app.services.PlantService;
import nl.growguru.app.services.SpaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/plant")

@RequiredArgsConstructor

@SecurityRequirement(name = "Authorization")
public class PlantController {
    private final PlantService plantService;
    private final SpaceService spaceService;

    @GetMapping("/{id}")
    public ResponseEntity<Plant> getPlantDetails(@PathVariable UUID id) {
        var plant = plantService.getPlantDetails(id);
        return new ResponseEntity<>(plant, (plant == null) ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    @PutMapping("/add/{spaceId}")
    public ResponseEntity<Plant> addPlants(@RequestBody @Valid Plant.PlantDto plantDto, @PathVariable UUID spaceId) {
        return new ResponseEntity<>(plantService.addPlant(plantDto, spaceId), HttpStatus.CREATED);
    }

    @PutMapping("/kill/{id}")
    public ResponseEntity<Plant> moveToGraveyard(@PathVariable UUID id) {
        return new ResponseEntity<>(plantService.moveToGraveyard(id), HttpStatus.OK);
    }

    @GetMapping("/all-from-space/{spaceId}")
    public ResponseEntity<Collection<Plant>> getAllPlantsFromSpace(@PathVariable UUID spaceId) {
        return new ResponseEntity<>(spaceService.getSelectedSpace(spaceId).getPlants(), HttpStatus.OK);
    }

    @PutMapping("/water/{plantId}")
    public ResponseEntity<String> waterPlant(@PathVariable UUID plantId) {
        return new ResponseEntity<>(plantService.waterPlant(plantId), HttpStatus.OK);
    }

    @PutMapping("/update-skins/{plantId}")
    public ResponseEntity<Plant> updatePlantSkins(@PathVariable UUID plantId, @RequestBody Plant.UpdatePlantSkinsDto updatePlantSkinsDto) {
        return new ResponseEntity<>(plantService.updatePlantSkins(plantId, updatePlantSkinsDto), HttpStatus.OK);
    }
}
