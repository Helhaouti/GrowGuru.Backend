package nl.growguru.app.api.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nl.growguru.app.services.StripeService;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(WebhookController.HOOK_API_BASE)
@RequiredArgsConstructor
public class WebhookController {

    public static final String HOOK_API_BASE = "/api/v1/hook";
    private static final Logger log = Logger.getLogger(WebhookController.class);
    private final ObjectMapper mapper;
    private final StripeService stripeService;

    @Value("${payment.secrets.webhook-secret}")
    private String endpointSecret;

    @SneakyThrows
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

        if ("checkout.session.completed".equals(event.getType())) {
            var eventData = mapper.readValue(
                    event.getData().toJson(),
                    new TypeReference<Map<String, Map<String, Object>>>() {
                    }
            ).get("object");
            stripeService.processSessionCompletion((String) eventData.get("id"));
        }

        log.info(
                "Event received: " + event.getType()
                        + "\nid: " + event.getId()
        );

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}