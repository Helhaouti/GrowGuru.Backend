package nl.growguru.app.models.plantspaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import nl.growguru.app.views.GrowGuruViews;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class PlantCustomization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private UUID potBaseSkinId;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private UUID potTrimSkinId;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private UUID accessoryId;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private UUID tableDecorationId;

}
