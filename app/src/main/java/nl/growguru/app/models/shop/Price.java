package nl.growguru.app.models.shop;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Price {

    private double amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

}