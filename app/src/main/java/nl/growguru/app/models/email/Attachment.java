package nl.growguru.app.models.email;

import java.io.File;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Attachment {

    private String name;
    private File file;

    public boolean isComplete() {
        return (name != null && !name.isEmpty()) &&
                (file != null && file.exists() && file.isFile() && file.canRead());
    }

}
