package nl.growguru.app.models.plantspaces;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.views.GrowGuruViews;
import org.springframework.data.util.Predicates;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;

@Entity

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        scope = Space.class,
        property = "id"
)
public class Space {

    @Id
    @JsonView(GrowGuruViews.SpaceFilter.class)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonView(GrowGuruViews.SpaceFilter.class)
    private String name;

    @Builder.Default
    @JsonView(GrowGuruViews.SpaceFilter.class)
    @ManyToMany(mappedBy = "spaces", fetch = FetchType.EAGER, cascade = {PERSIST, MERGE})
    private Set<GrowGuru> members = new HashSet<>();

    /**
     * Admins need to be members of the space.
     * First member, creator of space, is automatically an admin of the space.
     * Representation invariant: admins ⊆ members
     */
    @Builder.Default
    @JsonView(GrowGuruViews.SpaceFilter.class)
    @ManyToMany(mappedBy = "adminSpaces", fetch = FetchType.EAGER, cascade = {PERSIST, MERGE})
    private Set<GrowGuru> admins = new HashSet<>();

    @Builder.Default
    @JsonView({GrowGuruViews.SpaceFilter.class, GrowGuruViews.UserViewFilter.class})
    @OneToMany(fetch = FetchType.EAGER, cascade = ALL)
    private Set<Plant> plants = new HashSet<>();

    public Set<GrowGuru> getAllMembers() {
        return Stream.concat(members.stream(), admins.stream()).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Space space)) return false;

        return getId() != null ? getId().equals(space.getId()) : space.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Data
    @NoArgsConstructor
    public static class CreateSpaceDto {
        @NotBlank(message = "is not provided.")
        private String name;

        private Set<String> memberUsernames = new HashSet<>();
        private Set<String> adminUsernames = new HashSet<>();

        public CreateSpaceDto(String name){
            this.name = name;
        }

        public Space toSpace(Function<String, Optional<GrowGuru>> growGuruMapper) {
            memberUsernames.removeAll(adminUsernames);

            return builder()
                    .name(name)
                    .members(
                            memberUsernames.stream()
                                    .map(uuid -> growGuruMapper.apply(uuid).orElse(null))
                                    .filter(Predicates.negate(Objects::isNull))
                                    .collect(Collectors.toSet())
                    )
                    .admins(
                            adminUsernames.stream()
                                    .map(uuid -> growGuruMapper.apply(uuid).orElse(null))
                                    .filter(Predicates.negate(Objects::isNull))
                                    .collect(Collectors.toSet())
                    )
                    .build();
        }
    }

    @Data
    public static class UpdateSpaceDto {
        private String name;
    }

}