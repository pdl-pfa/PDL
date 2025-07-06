package odm_finance.finance.controller;


import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import odm_finance.finance.model.*;
import odm_finance.finance.service.PaymentNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public  class StripePaymentController {

    private final PaymentNotificationService.StripePaymentService stripePaymentService;

    /**
     * Créer un PaymentIntent pour initier un paiement
     */
    @PostMapping("/create-payment-intent")
    public ResponseEntity<ApiResponse> createPaymentIntent(@Valid @RequestBody PaymentIntentRequest request) {
        try {
            PaymentIntentResponse response = stripePaymentService.createPaymentIntent(request);

            return ResponseEntity.ok(ApiResponse.success(
                    "PaymentIntent créé avec succès",
                    response
            ));

        } catch (StripeException e) {
            log.error("Erreur Stripe lors de la création du PaymentIntent: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Erreur lors de la création du paiement: " + e.getUserMessage())
            );
        } catch (Exception e) {
            log.error("Erreur générale lors de la création du PaymentIntent: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Erreur interne du serveur")
            );
        }
    }

    /**
     * Confirmer un paiement réussi et envoyer l'email
     */
    @PostMapping("/confirm-payment")
    public ResponseEntity<ApiResponse> confirmPayment(@Valid @RequestBody PaymentSuccessData successData) {
        try {
            PaymentData paymentData = stripePaymentService.confirmPayment(successData);

            return ResponseEntity.ok(ApiResponse.success(
                    "Paiement confirmé et email envoyé avec succès",
                    paymentData
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("État de paiement invalide: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Erreur lors de la confirmation du paiement: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Erreur lors de la confirmation du paiement")
            );
        }
    }

    /**
     * Récupérer les détails d'un PaymentIntent
     */
    @GetMapping("/payment-intent/{id}")
    public ResponseEntity<ApiResponse> getPaymentIntent(@PathVariable String id) {
        try {
            PaymentIntent paymentIntent = stripePaymentService.getPaymentIntent(id);

            return ResponseEntity.ok(ApiResponse.success(
                    "PaymentIntent récupéré avec succès",
                    Map.of(
                            "id", paymentIntent.getId(),
                            "status", paymentIntent.getStatus(),
                            "amount", paymentIntent.getAmount(),
                            "currency", paymentIntent.getCurrency()
                    )
            ));

        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("PaymentIntent non trouvé: " + e.getUserMessage())
            );
        }
    }

    /**
     * Endpoint pour les cartes de test Stripe
     */
    @GetMapping("/test-cards")
    public ResponseEntity<ApiResponse> getTestCards() {
        Map<String, Object> testCards = Map.of(
                "visa_success", "4242424242424242",
                "visa_declined", "4000000000000002",
                "mastercard", "5555555555554444",
                "amex", "378282246310005",
                "visa_3d_secure", "4000000000003220",
                "instructions", "Utilisez n'importe quelle date d'expiration future et n'importe quel CVC à 3 chiffres"
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Cartes de test Stripe",
                testCards
        ));
    }
}
