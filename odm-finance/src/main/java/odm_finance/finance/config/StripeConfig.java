package odm_finance.finance.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StripeConfig {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        // Nettoyer la clé API (supprimer les espaces)
        if (stripeApiKey != null) {
            stripeApiKey = stripeApiKey.trim();
        }

        // Valider la clé API
        if (stripeApiKey == null || stripeApiKey.isEmpty()) {
            throw new IllegalStateException("La clé API Stripe est manquante dans la configuration");
        }

        if (!stripeApiKey.startsWith("sk_test_") && !stripeApiKey.startsWith("sk_live_")) {
            throw new IllegalStateException("Format de clé API Stripe invalide. Elle doit commencer par sk_test_ ou sk_live_");
        }

        // Configurer Stripe
        Stripe.apiKey = stripeApiKey;

//        log.info("Configuration Stripe initialisée avec succès (Mode: {})",
//                stripeApiKey.startsWith("sk_test_") ? "TEST" : "LIVE");
    }
}