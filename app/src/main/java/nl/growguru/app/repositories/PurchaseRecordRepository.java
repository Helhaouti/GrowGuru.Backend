package nl.growguru.app.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.models.shop.Product;
import nl.growguru.app.models.shop.PurchaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, UUID> {
    @Query("SELECT p FROM PurchaseRecord p WHERE p.buyer = ?1")
    List<PurchaseRecord> findAllByGrowGuru(GrowGuru growGuru);

    @Query("SELECT p.product FROM PurchaseRecord p WHERE p.buyer = ?1")
    List<Product> findAllProductsByGrowGuru(GrowGuru growGuru);

    @Query("SELECT p FROM PurchaseRecord p WHERE p.product.id = ?1")
    List<PurchaseRecord> findAllByProduct(UUID productId);

    @Query("SELECT p FROM PurchaseRecord p WHERE p.purchased = ?1")
    List<PurchaseRecord> findAllByDate(LocalDateTime date);
}
