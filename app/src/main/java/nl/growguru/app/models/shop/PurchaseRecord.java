package nl.growguru.app.models.shop;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.views.GrowGuruViews;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@JsonView(GrowGuruViews.PurchaseRecordFilter.class)
public class PurchaseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private GrowGuru buyer;

    @ManyToOne
    private Product product;

    @CreationTimestamp
    @Setter(value = AccessLevel.NONE)
    private LocalDateTime purchased;

    private Price price;

    public PurchaseRecord(GrowGuru buyer, Product product, Price price) {
        this.buyer = buyer;
        this.product = product;
        this.price = price;
    }
}
