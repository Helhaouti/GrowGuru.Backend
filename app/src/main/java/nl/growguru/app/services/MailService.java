package nl.growguru.app.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nl.growguru.app.exceptions.EmailIncompleteException;
import nl.growguru.app.models.email.Attachment;
import nl.growguru.app.models.email.Email;
import nl.growguru.app.models.shop.PurchaseRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    @Value("${email.sender}")
    private String defaultSender;

    @SneakyThrows
    public void sendPurchaseReceipt(PurchaseRecord purchaseRecord) {
        var context = new Context(Locale.ENGLISH, Map.of(
                "username", purchaseRecord.getBuyer().getUsername(),
                "productName", purchaseRecord.getProduct().getName(),
                "price", String.format("%s %.2f",
                        purchaseRecord.getPrice().getCurrency(), purchaseRecord.getPrice().getAmount()),
                "purchased", purchaseRecord.getPurchased()
        ));
        var email = new Email.EmailDto() {{
            setSender(defaultSender);
            setReceiver(purchaseRecord.getBuyer().getEmail());
            setSubject("Purchase Confirmation");
            setContent(templateEngine.process("mail-payment-receipt", context));
        }}.toMail();

        sendEmail(email);
    }

    @SneakyThrows
    public void sendVerificationMail(String email, String name, String verificationCode) {
        var baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        var context = new Context();
        context.setVariables(Map.of("email", email, "name", name));
        context.setVariable("url", String.format("%s/api/v1/auth/verification/%s", baseUrl, verificationCode));
        var body = templateEngine.process("verification", context);
        Email.EmailDto emailDto = new Email.EmailDto();
        emailDto.setSender(defaultSender);
        emailDto.setReceiver(email);
        emailDto.setSubject("Verify your account");
        emailDto.setContent(body);
        var emailObj = emailDto.toMail();

        sendEmail(emailObj);
    }

    private void sendEmail(@NonNull Email email) throws MessagingException, EmailIncompleteException {
        if (!email.isComplete()) throw new EmailIncompleteException("Email is missing a field");

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, !email.getAttachments().isEmpty());

        helper.setFrom(email.getSender());
        helper.setTo(email.getReceiver());
        helper.setSubject(email.getSubject());
        helper.setText(email.getContent(), true);

        for (Attachment attachment : email.getAttachments()) {
            if (!attachment.isComplete()) throw new EmailIncompleteException("Attachment in email is incomplete!");
            helper.addAttachment(attachment.getName(), attachment.getFile());
        }

        CompletableFuture.runAsync(() -> javaMailSender.send(message));
    }

}