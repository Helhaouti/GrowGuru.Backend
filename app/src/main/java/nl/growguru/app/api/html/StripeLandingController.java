package nl.growguru.app.api.html;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class StripeLandingController {

    public static final String SUCCESSFUL_STRIPE_PAYMENT_PAGE = "/payment/successful";

    @GetMapping(SUCCESSFUL_STRIPE_PAYMENT_PAGE)
    public String success() {
        return "site-payment-succes";
    }

}