package nl.growguru.app.models.email;

import java.util.ArrayList;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Email {

    private String sender;
    private String receiver;
    private String subject;
    private String content;
    @Builder.Default
    private Collection<Attachment> attachments = new ArrayList<>();

    public boolean isComplete() {
        return (sender != null && !sender.isEmpty()) &&
                (receiver != null && !receiver.isEmpty()) &&
                (subject != null && !subject.isEmpty()) &&
                (content != null && !content.isEmpty()) &&
                (attachments != null);
    }

    @Data
    public static class EmailDto {

        private String sender;
        private String receiver;
        private String subject;
        private String content;

        public Email toMail() {
            return builder()
                    .sender(getSender())
                    .receiver(getReceiver())
                    .subject(getSubject())
                    .content(getContent())
                    .build();
        }
    }
}

