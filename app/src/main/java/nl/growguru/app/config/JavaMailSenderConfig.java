package nl.growguru.app.config;

import java.util.Map;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class JavaMailSenderConfig {

    @Value("${email.username}")
    private String userName;
    @Value("${email.password}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername(userName);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.putAll(Map.of(
                "mail.transport.protocol", "smtp",
                "mail.smtp.auth", "true",
                "mail.smtp.starttls.enable", "true",
                "mail.debug", "true",
                "mail.smtp.ssl.trust", "smtp.gmail.com"
        ));

        return mailSender;
    }

}
