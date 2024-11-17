package nl.growguru.app.services;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData;
import com.stripe.param.checkout.SessionCreateParams.LineItem.PriceData.ProductData;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nl.growguru.app.models.auth.GrowGuru;
import nl.growguru.app.models.shop.Product;
import nl.growguru.app.models.shop.StripeSessionTracker;
import nl.growguru.app.repositories.StripeSessionTrackerRepository;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static com.stripe.param.checkout.SessionCreateParams.Mode.PAYMENT;
import static com.stripe.param.checkout.SessionCreateParams.PaymentMethodType.BANCONTACT;
import static com.stripe.param.checkout.SessionCreateParams.PaymentMethodType.CARD;
import static com.stripe.param.checkout.SessionCreateParams.PaymentMethodType.EPS;
import static com.stripe.param.checkout.SessionCreateParams.PaymentMethodType.GIROPAY;
import static com.stripe.param.checkout.SessionCreateParams.PaymentMethodType.IDEAL;
import static com.stripe.param.checkout.SessionCreateParams.PaymentMethodType.KLARNA;
import static nl.growguru.app.api.html.StripeLandingController.SUCCESSFUL_STRIPE_PAYMENT_PAGE;
import static nl.growguru.app.utils.EnvUtil.getBaseUrl;
import static org.springframework.http.HttpStatus.CREATED;

@Service
@Transactional
@RequiredArgsConstructor
public class StripeService {

    private final Logger log = Logger.getLogger(StripeService.class);

    private final StripeSessionTrackerRepository trackerRepository;
    private final PurchaseRecordService purchaseService;

    @Value("${payment.secrets.api-key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    @SneakyThrows
    public ResponseEntity<Void> createCheckoutSession(Product product, GrowGuru growGuru) {
        var priceData = PriceData.builder()
                .setUnitAmount(Math.round(product.getPrice().getAmount() * 100))
                .setCurrency(product.getPrice().getCurrency().toLowerCaseString())
                .setProductData(ProductData.builder().setName(product.getName()).build())
                .build();

        var createParams = SessionCreateParams.builder()
                .setSuccessUrl(getBaseUrl() + SUCCESSFUL_STRIPE_PAYMENT_PAGE)
                .setMode(PAYMENT)
                .addAllPaymentMethodType(List.of(
                        CARD, KLARNA, EPS, BANCONTACT, IDEAL, GIROPAY
                ))
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(priceData)
                        .build())
                .build();

        var session = Session.create(createParams);

        trackerRepository.save(
                StripeSessionTracker.builder()
                        .sessionId(session.getId())
                        .paymentLink(session.getUrl())
                        .growGuru(growGuru)
                        .product(product)
                        .build()
        );

        log.info(String.format(
                "Stripe session created: %nSession id: %s %nGrowGuru: id: %s, username: %s %nProduct: %s %f %s",
                session.getId(),
                growGuru.getId(),
                growGuru.getUsername(),
                product.getName(),
                product.getPrice().getAmount(),
                product.getPrice().getCurrency()
        ));

        return ResponseEntity
                .status(CREATED)
                .headers(new HttpHeaders() {{
                    setLocation(new URI(session.getUrl()));
                }})
                .build();
    }

    public void processSessionCompletion(String sessionId) {
        var tracker = trackerRepository.findBySessionId(sessionId);

        if (tracker == null) log.warn("Received untracked completed session with id: " + sessionId);
        else purchaseService.processProductPurchase(
                tracker.getProduct(),
                tracker.getGrowGuru(),
                tracker.getProduct().getPrice()
        );
    }

}