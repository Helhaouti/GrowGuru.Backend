package nl.growguru.app.models.shop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.growguru.app.models.auth.GrowGuru;

@Entity

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StripeSessionTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String sessionId;

    @Column(columnDefinition = "TEXT")
    private String paymentLink;

    @ManyToOne
    private Product product;

    @ManyToOne
    private GrowGuru growGuru;

}