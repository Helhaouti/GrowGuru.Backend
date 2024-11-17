package nl.growguru.app.services;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.exceptions.ConflictException;
import nl.growguru.app.exceptions.ResourceNotFound;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.models.shop.Price;
import nl.growguru.app.models.shop.Product;
import nl.growguru.app.models.shop.PurchaseRecord;
import nl.growguru.app.repositories.GrowGuruRepository;
import nl.growguru.app.repositories.PurchaseRecordRepository;
import org.springframework.stereotype.Service;

import static nl.growguru.app.models.shop.Currency.LEAFS;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseRecordService {

    private final PurchaseRecordRepository purchaseRecordRepository;
    private final GrowGuruRepository growGuruRepository;
    private final MailService mailService;

    public List<PurchaseRecord> findAll() {
        return purchaseRecordRepository.findAll();
    }

    public PurchaseRecord findById(UUID id) {
        return purchaseRecordRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFound(PurchaseRecord.class.getSimpleName(), "purchase record", id.toString()));
    }

    public List<PurchaseRecord> findByDate(LocalDateTime date) {
        return purchaseRecordRepository.findAllByDate(date);
    }

    public List<PurchaseRecord> findAllByGrowGuru(GrowGuru growGuru) {
        return purchaseRecordRepository.findAllByGrowGuru(growGuru);
    }

    public List<PurchaseRecord> findAllByProduct(UUID id) {
        return purchaseRecordRepository.findAllByProduct(id);
    }

    public void processProductPurchase(
            Product product,
            GrowGuru growGuru,
            Price purchasePrice
    ) {
        switch (product.getCategory()) {
            case CURRENCY -> growGuru.setCurrency(growGuru.getCurrency() + product.getLeafs());
            default -> {
                // Check if the GrowGuru already owns the product
                List<Product> currentProducts = purchaseRecordRepository.findAllProductsByGrowGuru(growGuru);
                if (currentProducts.contains(product))
                    throw new ConflictException("GrowGuru already owns product: " + product.getName());
            }
        }

        // Add purchaseRecord
        PurchaseRecord purchaseRecord = purchaseRecordRepository.saveAndFlush(new PurchaseRecord(growGuru, product, purchasePrice));

        // Save purchaseRecord to GrowGuru
        growGuru.getPurchases().add(purchaseRecord);
        growGuruRepository.save(growGuru);

        if (purchaseRecord.getPrice().getCurrency() != LEAFS) mailService.sendPurchaseReceipt(purchaseRecord);
    }

}
