package nl.growguru.app.api.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.models.shop.PurchaseRecord;
import nl.growguru.app.services.PurchaseRecordService;
import nl.growguru.app.utils.SecurityContextUtil;
import nl.growguru.app.views.GrowGuruViews;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/purchaserecord")

@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
public class PurchaseRecordController {
    private final PurchaseRecordService purchaseRecordService;

    @JsonView(GrowGuruViews.PurchaseRecordFilter.class)
    @GetMapping("/all")
    public List<PurchaseRecord> getAllRecords(){
        return purchaseRecordService.findAll();
    }

    @JsonView(GrowGuruViews.PurchaseRecordFilter.class)
    @GetMapping("/allFromUser")
    public List<PurchaseRecord> getAllPurchaseRecordsPerUser()
    {
        return purchaseRecordService.findAllByGrowGuru(SecurityContextUtil.getCurrentGrowGuru());
    }

    @JsonView(GrowGuruViews.PurchaseRecordFilter.class)
    @GetMapping("/id/{id}")
    public PurchaseRecord findById(@PathVariable UUID id) {
        return purchaseRecordService.findById(id);
    }

    @JsonView(GrowGuruViews.PurchaseRecordFilter.class)
    @GetMapping("/date/{date}")
    public List<PurchaseRecord> findByDate(@PathVariable LocalDateTime date) {
        return purchaseRecordService.findByDate(date);
    }

    @JsonView(GrowGuruViews.PurchaseRecordFilter.class)
    @GetMapping("/product/{id}")
    public List<PurchaseRecord> findByProduct(@PathVariable UUID id) {
        return purchaseRecordService.findAllByProduct(id);
    }
}
