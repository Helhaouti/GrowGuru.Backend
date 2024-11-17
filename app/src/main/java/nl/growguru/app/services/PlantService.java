package nl.growguru.app.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.exceptions.ResourceNotFound;
import nl.growguru.app.models.plantspaces.Plant;
import nl.growguru.app.models.plantspaces.PlantState;
import nl.growguru.app.models.plantspaces.Space;
import nl.growguru.app.repositories.PlantRepository;
import nl.growguru.app.repositories.SpaceRepository;
import org.springframework.stereotype.Service;

import static nl.growguru.app.utils.SecurityContextUtil.getCurrentGrowGuru;

@Service
@RequiredArgsConstructor
public class PlantService {

    private final PlantRepository plantRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceService spaceService;
    private static final String PLANT_NOT_FOUND = "Couldn't find plant";
    private static final String PLANT_HAS_BEEN_WATERED = "Plant is watered";
    private static final int STREAK_INCREMENT = 1;
    private static final int HOURS_A_DAY = 24;
    private static final int MINIMUM_WATERING_INTERVAL = 10;
    private static final double MARGIN_PERCENTAGE = 0.10;

    public Plant getPlantDetails(UUID plantId) {
        return plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFound(PLANT_NOT_FOUND));
    }

    public Plant addPlant(Plant.PlantDto plantDto, UUID spaceId) {
        Plant newPlant = plantDto.toPlant();

        Space selectedSpace = spaceService.getSelectedSpace(spaceId);

        selectedSpace.getPlants().add(newPlant);

        spaceRepository.save(selectedSpace);

        return newPlant;
    }

    public Plant moveToGraveyard(UUID id) {
        return plantRepository.findById(id)
                .map(plant -> {
                    if (plant.getDeadAt() == null) {
                        plant.setDeadAt(LocalDateTime.now());
                        plant.setPlantState(PlantState.DEAD);
                    }
                    return plantRepository.save(plant);
                }).orElseThrow(() -> new ResourceNotFound(PLANT_NOT_FOUND));
    }

    public String waterPlant(UUID plantId) {
        //Validate selected plant
        Plant currentPlant = getCurrentGrowGuru().getAllSpaces().stream()
                .flatMap(s -> s.getPlants().stream())
                .filter(p -> p.getId().equals(plantId))
                .findAny()
                .orElseThrow(() -> new ResourceNotFound(PLANT_NOT_FOUND));

        //Streaks: Calculate margin
        int wateringMarginHours = HOURS_A_DAY;
        byte wateringIntervalDuration = currentPlant.getWateringInterval();
        //Streaks: Check if watering interval more than 10 days
        if (wateringIntervalDuration > MINIMUM_WATERING_INTERVAL) {
            //If yes then update the value with new calculation
            wateringMarginHours = (int) Math.round((MARGIN_PERCENTAGE * wateringIntervalDuration) * HOURS_A_DAY);
        }

        //Streaks: Check if the current date is outside the margin of the watering date,
        if (isOutsideMargins(currentPlant.getWateringDate(), wateringMarginHours)) {
            //If true then reset streak
            currentPlant.setStreaks(0);
        } else {
            //If false then add one streak
            currentPlant.setStreaks(currentPlant.getStreaks() + STREAK_INCREMENT);
        }

        //Update last watering date with the current time
        currentPlant.setLastWateringDate(LocalDateTime.now());

        //Update new watering plant by adding the days of the interval
        Duration wateringInterval = Duration.ofDays(currentPlant.getWateringInterval());
        LocalDateTime updatedWateringDate = LocalDateTime.now().plus(wateringInterval);
        currentPlant.setWateringDate(updatedWateringDate);

        plantRepository.save(currentPlant);

        return PLANT_HAS_BEEN_WATERED;
    }

    private boolean isOutsideMargins(LocalDateTime wateringDate, int marginHours) {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime positiveMargin = wateringDate.plus(Duration.ofHours(marginHours));
        LocalDateTime negativeMargin = wateringDate.minus(Duration.ofHours(marginHours));

        return currentDate.isAfter(positiveMargin) || currentDate.isBefore(negativeMargin);
    }

    public Plant updatePlantSkins(UUID plantId, Plant.UpdatePlantSkinsDto updatePlantSkinsDto) {
        Plant plant = getPlantDetails(plantId);

        plant.setPlantCustomization(updatePlantSkinsDto.getPlantCustomization());

        plantRepository.save(plant);

        return plant;
    }

}
