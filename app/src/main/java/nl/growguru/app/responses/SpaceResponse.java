package nl.growguru.app.responses;

import java.util.Collection;
import java.util.UUID;
import lombok.Data;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.models.plantspaces.Plant;
import nl.growguru.app.models.plantspaces.Space;

@Data
public class SpaceResponse {

    private UUID id;
    private String name;
    private Collection<String> memberNames;
    private Collection<String> adminNames;
    private Collection<Plant> plants;

    public SpaceResponse(Space space) {
        this.id = space.getId();
        this.name = space.getName();
        this.memberNames = space.getMembers().stream().map(GrowGuru::getUsername).toList();
        this.adminNames = space.getAdmins().stream().map(GrowGuru::getUsername).toList();
        this.plants = space.getPlants();
    }
}
