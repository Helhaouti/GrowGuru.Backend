package nl.growguru.app.repositories;

import java.util.UUID;
import nl.growguru.app.models.plantspaces.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantRepository extends JpaRepository<Plant, UUID> {
}
