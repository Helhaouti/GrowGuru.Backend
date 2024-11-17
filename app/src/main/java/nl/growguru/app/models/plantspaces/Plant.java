package nl.growguru.app.models.plantspaces;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.growguru.app.views.GrowGuruViews;
import org.hibernate.annotations.CreationTimestamp;


@Entity

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Plant {

    @JsonView(GrowGuruViews.SpaceFilter.class)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private String name;

    @Enumerated(EnumType.STRING)
    @JsonView(GrowGuruViews.SpaceFilter.class)
    private Type type;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private PlantState plantState;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private int streaks;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private LocalDateTime deadAt;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private LocalDateTime lastWateringDate;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private LocalDateTime wateringDate;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private byte wateringInterval;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customization_id")
    private PlantCustomization plantCustomization;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plant plant)) return false;

        return getId() != null ? getId().equals(plant.getId()) : plant.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Data
    public static class PlantDto {
        @NotBlank(message = "is not provided")
        private String name;
        @NotNull(message = "is not provided")
        private Type type;
        @Min(message = "is too short", value = 0)
        private byte wateringInterval;
        @NotNull(message = "is not provided")
        private PlantCustomization plantCustomization;

        public Plant toPlant() {
            return builder()
                    .name(getName())
                    .type(getType())
                    .plantState(PlantState.ALIVE)
                    .wateringDate(LocalDateTime.now().plus(Duration.ofDays(getWateringInterval())))
                    .wateringInterval(getWateringInterval())
                    .plantCustomization(getPlantCustomization())
                    .streaks(0)
                    .createdAt(LocalDateTime.now())
                    .deadAt(null)
                    .build();
        }
    }

    @Data
    public static class UpdatePlantSkinsDto {
        private PlantCustomization plantCustomization;
    }
}