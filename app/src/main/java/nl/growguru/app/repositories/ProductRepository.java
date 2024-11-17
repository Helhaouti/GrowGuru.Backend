package nl.growguru.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.growguru.app.models.shop.Category;
import nl.growguru.app.models.shop.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("SELECT p FROM Product p WHERE p.name = ?1")
    Optional<Product> findByName(String name);

    @Query("SELECT p FROM Product p WHERE p.category = ?1")
    List<Product> findAllWithSameCategory(Category category);

    @Query("SELECT p FROM Product p WHERE p.standard = true")
    List<Product> findAllDefaultProducts();

    @Query("SELECT p FROM Product p WHERE p.id NOT IN :ownedProducts")
    List<Product> findAllExcludingOwned(List<UUID> ownedProducts);
}
