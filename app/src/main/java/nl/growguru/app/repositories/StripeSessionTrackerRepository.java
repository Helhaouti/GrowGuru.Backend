package nl.growguru.app.repositories;

import java.util.UUID;
import nl.growguru.app.models.shop.StripeSessionTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StripeSessionTrackerRepository extends JpaRepository<StripeSessionTracker, UUID> {

    @Query("select s from StripeSessionTracker s where s.sessionId = ?1")
    StripeSessionTracker findBySessionId(String sessionId);

    @Transactional
    @Modifying
    @Query("delete from StripeSessionTracker s where s.growGuru.id = ?1")
    void deleteSessionsByGrowGuruId(UUID growGuruId);
}
