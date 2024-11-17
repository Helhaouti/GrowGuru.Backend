package nl.growguru.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.models.plantspaces.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GrowGuruRepository extends JpaRepository<GrowGuru, UUID> {

    @Query("SELECT g FROM GrowGuru g WHERE g.username = ?1")
    Optional<GrowGuru> findByUsername(String username);

    @Query("SELECT g FROM GrowGuru g WHERE g.email = ?1")
    Optional<GrowGuru> findByEmail(String email);

    @Query("SELECT g FROM GrowGuru g JOIN g.spaces s WHERE s = :space")
    List<GrowGuru> findAllMembersInSpace(@Param("space") Space space);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN TRUE ELSE FALSE END FROM GrowGuru g WHERE g.verificationCode = ?1")
    boolean existsByVerificationCode(String code);

    @Query("SELECT g FROM GrowGuru g WHERE g.verificationCode = ?1")
    Optional<GrowGuru> findUserByVerificationCode(String code);
}
