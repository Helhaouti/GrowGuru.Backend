package nl.growguru.app.models.shop;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.growguru.app.views.GrowGuruViews;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonView(GrowGuruViews.PurchaseRecordFilter.class)
    private UUID id;

    @CreationTimestamp
    @Setter(value = AccessLevel.NONE)
    private LocalDate created;

    @UpdateTimestamp
    private LocalDate updated;

    @Column(unique = true)
    private String name;

    /**
     * Represents the amount of leafs purchased, defined if product category == CURRENCY, else null.
     */
    private Long leafs;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Embedded
    private Price price;

    /**
     * Stores a product image in binary form. This field is mutually exclusive with the 'color' field;
     * it is null if 'color' is defined, and vice versa.
     */
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] image;

    /**
     * Stores the color of the product as a string. This field is mutually exclusive with the 'image' field;
     * it is null if 'image' is defined, and vice versa.
     */
    private String color;

    /**
     * Defines whether the products should be assigned to a new user by default.
     */
    private boolean standard;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;

        return getId() != null ? getId().equals(product.getId()) : product.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

}