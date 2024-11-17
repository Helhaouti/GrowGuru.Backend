package nl.growguru.app.repositories;

import java.util.UUID;
import nl.growguru.app.models.plantspaces.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceRepository extends JpaRepository<Space, UUID> {
}
