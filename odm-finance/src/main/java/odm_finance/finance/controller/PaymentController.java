package odm_finance.finance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import odm_finance.finance.model.ApiResponse;
import odm_finance.finance.model.InvoiceData;
import odm_finance.finance.model.PaymentData;
import odm_finance.finance.model.ProduitAchat;
import odm_finance.finance.service.PaymentNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentNotificationService paymentNotificationService;

    /**
     * Traite un paiement et envoie la confirmation par email
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            // Valider les données
            if (request.getProduitsAchat() == null || request.getProduitsAchat().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("La liste des produits ne peut pas être vide")
                );
            }

            // Créer les données de facture
            InvoiceData invoiceData = createInvoiceData(request.getProduitsAchat());

            // Créer les données de paiement
            PaymentData paymentData = createPaymentData(request, invoiceData);

            // Envoyer la notification de paiement
            paymentNotificationService.sendPaymentConfirmation(paymentData, invoiceData);

            return ResponseEntity.ok(ApiResponse.success(
                    "Paiement traité avec succès. Email de confirmation envoyé.",
                    paymentData.getTransactionId()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Erreur lors du traitement du paiement: " + e.getMessage())
            );
        }
    }

    /**
     * Notifie un échec de paiement
     */
    @PostMapping("/failure")
    public ResponseEntity<ApiResponse> notifyPaymentFailure(@Valid @RequestBody PaymentData paymentData) {
        try {
            paymentNotificationService.sendPaymentFailureNotification(paymentData);

            return ResponseEntity.ok(ApiResponse.success(
                    "Notification d'échec envoyée avec succès"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    ApiResponse.error("Erreur lors de l'envoi de la notification: " + e.getMessage())
            );
        }
    }

    private InvoiceData createInvoiceData(List<ProduitAchat> produitsAchat) {
        // Calculer les totaux
        double subtotal = produitsAchat.stream()
                .mapToDouble(ProduitAchat::getTotal)
                .sum();

        double taxRate = 0.20; // TVA à 20%
        double taxAmount = subtotal * taxRate;
        double total = subtotal + taxAmount;

        // Convertir les produits en items de facture
        List<InvoiceData.InvoiceItem> items = produitsAchat.stream()
                .map(produit -> new InvoiceData.InvoiceItem(
                        produit.getNom(),
                        produit.getQuantite(),
                        produit.getPrix(),
                        produit.getTotal()
                ))
                .collect(Collectors.toList());

        // Créer les données client
        InvoiceData.Client client = new InvoiceData.Client(
                "Client Exemple",
                "123 Rue Exemple, 75000 Paris, France",
                "client@exemple.com",
                "01 23 45 67 89"
        );

        return new InvoiceData(
                generateInvoiceNumber(),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                client,
                items,
                subtotal,
                taxAmount,
                total
        );
    }

    private PaymentData createPaymentData(PaymentRequest request, InvoiceData invoiceData) {
        return new PaymentData(
                generateTransactionId(),
                invoiceData.getNumber(),
                invoiceData.getTotal(),
                request.getPaymentMethod(),
                LocalDateTime.now(),
                request.getClientName(),
                request.getClientEmail(),
                "COMPLETED"
        );
    }

    private String generateInvoiceNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "INV-" + year + "-" + uuid;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    // Classe interne pour la requête de paiement
    @lombok.Data
    public static class PaymentRequest {
        @jakarta.validation.constraints.NotBlank
        private String clientName;

        @jakarta.validation.constraints.Email
        @jakarta.validation.constraints.NotBlank
        private String clientEmail;

        @jakarta.validation.constraints.NotBlank
        private String paymentMethod;

        @jakarta.validation.Valid
        @jakarta.validation.constraints.NotEmpty
        private List<ProduitAchat> produitsAchat;
    }
}