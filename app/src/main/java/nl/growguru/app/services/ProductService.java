package nl.growguru.app.services;

import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.exceptions.ResourceNotFound;
import nl.growguru.app.exceptions.TooBrokeException;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.models.shop.Category;
import nl.growguru.app.models.shop.Price;
import nl.growguru.app.models.shop.Product;
import nl.growguru.app.models.shop.PurchaseRecord;
import nl.growguru.app.repositories.ProductRepository;
import nl.growguru.app.repositories.PurchaseRecordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static nl.growguru.app.models.shop.Category.CURRENCY;
import static nl.growguru.app.models.shop.Currency.LEAFS;
import static nl.growguru.app.utils.SecurityContextUtil.getCurrentGrowGuru;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final PurchaseRecordRepository purchaseRecordRepository;
    private final StripeService stripeService;
    private final PurchaseRecordService purchaseService;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findAllExcludingOwned() {
        List<PurchaseRecord> purchases = getCurrentGrowGuru().getPurchases();

        List<UUID> ownedProducts = purchases
                .stream()
                .map(PurchaseRecord::getProduct)
                .filter(p -> p.getCategory() != CURRENCY)
                .map(Product::getId)
                .toList();

        if (ownedProducts.isEmpty())
            throw new ResourceNotFound(Product.class.getSimpleName(), "product", "n/a");
        else
            return productRepository.findAllExcludingOwned(ownedProducts)
                    .stream()
                    .sorted(Comparator.comparingDouble(
                            p -> p.getCategory() != CURRENCY ? 0 : p.getPrice().getAmount()))
                    .toList();
    }

    public List<Product> findAllOwnedByUser(GrowGuru growGuru) {
        return purchaseRecordRepository.findAllProductsByGrowGuru(growGuru);
    }

    public Product findById(UUID id) {
        return productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFound(Product.class.getSimpleName(), "product", id.toString()));
    }

    public Product findByName(String name) {
        return productRepository
                .findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Product with name - %s, not found", name)
                ));
    }

    public List<Product> findAllWithSameCategory(Category category) {
        return productRepository.findAllWithSameCategory(category);
    }

    public ResponseEntity<Void> buyProduct(UUID id) {
        return buyProduct(id, getCurrentGrowGuru());
    }

    public ResponseEntity<Void> buyProduct(UUID id, GrowGuru growGuru) {
        Product product = this.findById(id);

        return switch (product.getCategory()) {
            case CURRENCY -> product.getPrice().getCurrency() != LEAFS || !product.isStandard()
                    ? stripeService.createCheckoutSession(product, growGuru)
                    : buyProductWithLeafs(product, growGuru);
            default -> buyProductWithLeafs(product, growGuru);
        };
    }

    private ResponseEntity<Void> buyProductWithLeafs(Product product, GrowGuru growGuru) {
        double priceAmount = product.getPrice().getAmount();

        if (!product.isStandard()) {
            long currentCurrency = growGuru.getCurrency();

            if (currentCurrency < product.getPrice().getAmount())
                throw new TooBrokeException("Can not afford new product.");
            else {
                currentCurrency -= (long) product.getPrice().getAmount();
                growGuru.setCurrency(currentCurrency);
            }
        } else
            priceAmount = 0;

        purchaseService.processProductPurchase(product, growGuru, new Price(priceAmount, LEAFS));

        if (product.getCategory() == Category.CURRENCY)
            growGuru.setCurrency(growGuru.getCurrency() + product.getLeafs());

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public void remove(UUID id) {
        this.productRepository.deleteById(id);
    }
}