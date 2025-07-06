package odm_finance.finance.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import odm_finance.finance.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationService {

    private final EmailService emailService;
    private final InvoiceService invoiceService;

    /**
     * Envoie une notification de paiement avec le PDF de facture
     *
     * @param paymentData Données du paiement
     * @param invoiceData Données de la facture
     * @throws Exception Si une erreur survient lors de l'envoi
     */
    public void sendPaymentConfirmation(PaymentData paymentData, InvoiceData invoiceData) throws Exception {
        try {
            // Générer le PDF de la facture
            byte[] pdfBytes = invoiceService.generateInvoicePdf(invoiceData);

            // Préparer les variables pour le template email
            Map<String, Object> emailVariables = createEmailVariables(paymentData, invoiceData);

            // Créer le template email
            EmailTemplate emailTemplate = new EmailTemplate();
            emailTemplate.setTo(paymentData.getClientEmail());
            emailTemplate.setSubject("Confirmation de paiement - Facture " + paymentData.getInvoiceNumber());
            emailTemplate.setTemplateName("payment-confirmation");
            emailTemplate.setVariables(emailVariables);
            emailTemplate.setAttachment(pdfBytes);
            emailTemplate.setAttachmentName("facture-" + paymentData.getInvoiceNumber() + ".pdf");
            emailTemplate.setAttachmentType("application/pdf");

            // Envoyer l'email
            emailService.sendHtmlEmail(emailTemplate);

            log.info("Notification de paiement envoyée pour la facture {} à {}",
                    paymentData.getInvoiceNumber(), paymentData.getClientEmail());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification de paiement pour la facture {}",
                    paymentData.getInvoiceNumber(), e);
            throw new Exception("Erreur lors de l'envoi de la notification de paiement", e);
        }
    }

    /**
     * Envoie une notification de paiement échoué
     *
     * @param paymentData Données du paiement
     * @throws Exception Si une erreur survient lors de l'envoi
     */
    public void sendPaymentFailureNotification(PaymentData paymentData) throws Exception {
        try {
            Map<String, Object> emailVariables = new HashMap<>();
            emailVariables.put("clientName", paymentData.getClientName());
            emailVariables.put("invoiceNumber", paymentData.getInvoiceNumber());
            emailVariables.put("amount", paymentData.getAmount());
            emailVariables.put("transactionId", paymentData.getTransactionId());
            emailVariables.put("paymentDate", paymentData.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            emailService.sendSimpleEmail(
                    paymentData.getClientEmail(),
                    "Échec de paiement - Facture " + paymentData.getInvoiceNumber(),
                    "payment-failure",
                    emailVariables
            );

            log.info("Notification d'échec de paiement envoyée pour la facture {} à {}",
                    paymentData.getInvoiceNumber(), paymentData.getClientEmail());

        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification d'échec pour la facture {}",
                    paymentData.getInvoiceNumber(), e);
            throw new Exception("Erreur lors de l'envoi de la notification d'échec", e);
        }
    }

    /**
     * Crée les variables pour le template email
     */
    private Map<String, Object> createEmailVariables(PaymentData paymentData, InvoiceData invoiceData) {
        Map<String, Object> variables = new HashMap<>();

        // Données du client
        variables.put("clientName", paymentData.getClientName());

        // Données du paiement
        variables.put("transactionId", paymentData.getTransactionId());
        variables.put("paymentMethod", paymentData.getPaymentMethod());
        variables.put("paymentDate", paymentData.getPaymentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        variables.put("amount", paymentData.getAmount());
        variables.put("status", paymentData.getStatus());

        // Données de la facture
        variables.put("invoiceNumber", paymentData.getInvoiceNumber());
        variables.put("invoiceDate", invoiceData.getIssueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        variables.put("dueDate", invoiceData.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        variables.put("total", invoiceData.getTotal());

        // Données de l'entreprise
        variables.put("companyName", "ODM Finance");
        variables.put("supportEmail", "support@odm-finance.com");
        variables.put("websiteUrl", "https://www.odm-finance.com");

        return variables;
    }

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public static class StripePaymentService {

        private final PaymentNotificationService paymentNotificationService;

        @Value("${app.frontend.url}")
        private String frontendUrl;

        /**
         * Crée un PaymentIntent Stripe
         */
        public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) throws StripeException {
            try {
                // Générer le numéro de facture
                String invoiceNumber = generateInvoiceNumber();

                // Calculer le montant total
                long totalAmount = calculateTotalAmount(request.getProducts());

                // Créer les métadonnées
                Map<String, String> metadata = createMetadata(request, invoiceNumber);

                // Paramètres du PaymentIntent
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(totalAmount)
                    .setCurrency(request.getCurrency())
                    .setDescription(request.getDescription() != null ?
                        request.getDescription() : "Paiement facture " + invoiceNumber)
                    .setReceiptEmail(request.getReceiptEmail() != null ?
                        request.getReceiptEmail() : request.getClientEmail())
                    .putAllMetadata(metadata)
                    .build();

                // Créer le PaymentIntent
                PaymentIntent paymentIntent = PaymentIntent.create(params);

                log.info("PaymentIntent créé avec succès: {}", paymentIntent.getId());

                return new PaymentIntentResponse(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    paymentIntent.getAmount(),
                    paymentIntent.getCurrency(),
                    invoiceNumber
                );

            } catch (StripeException e) {
                log.error("Erreur lors de la création du PaymentIntent: {}", e.getMessage(), e);
                throw e;
            }
        }

        /**
         * Confirme un paiement et envoie l'email de confirmation
         */
        public PaymentData confirmPayment(PaymentSuccessData successData) throws Exception {
            try {
                // Récupérer le PaymentIntent depuis Stripe
                PaymentIntent paymentIntent = PaymentIntent.retrieve(
                    successData.getPaymentIntentId()
                );

                if (!"succeeded".equals(paymentIntent.getStatus())) {
                    throw new IllegalStateException("Le paiement n'a pas été confirmé côté Stripe");
                }

                // Extraire les données des métadonnées
                Map<String, String> metadata = paymentIntent.getMetadata();
                String clientName = metadata.get("client_name");
                String clientEmail = metadata.get("client_email");
                String invoiceNumber = metadata.get("invoice_number");

                // Créer les données de paiement
                PaymentData paymentData = new PaymentData(
                    paymentIntent.getId(),
                    invoiceNumber,
                    paymentIntent.getAmount() / 100.0, // Convertir centimes en euros
                    getPaymentMethodType(paymentIntent),
                    LocalDateTime.now(),
                    clientName,
                    clientEmail,
                    "COMPLETED"
                );

                // Créer les données de facture depuis les métadonnées
                InvoiceData invoiceData = createInvoiceDataFromMetadata(metadata, paymentData);

                // Envoyer l'email de confirmation
                paymentNotificationService.sendPaymentConfirmation(paymentData, invoiceData);

                log.info("Paiement confirmé et email envoyé pour: {}", paymentIntent.getId());

                return paymentData;

            } catch (StripeException e) {
                log.error("Erreur lors de la confirmation du paiement: {}", e.getMessage(), e);
                throw new Exception("Erreur lors de la récupération du paiement Stripe", e);
            }
        }

        /**
         * Récupère les détails d'un PaymentIntent
         */
        public PaymentIntent getPaymentIntent(String paymentIntentId) throws StripeException {
            return PaymentIntent.retrieve(paymentIntentId);
        }

        private long calculateTotalAmount(List<ProduitAchat> products) {
            double subtotal = products.stream()
                .mapToDouble(ProduitAchat::getTotal)
                .sum();

            double taxRate = 0.20; // TVA 20%
            double total = subtotal * (1 + taxRate);

            // Convertir en centimes pour Stripe
            return Math.round(total * 100);
        }

        private Map<String, String> createMetadata(PaymentIntentRequest request, String invoiceNumber) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("client_name", request.getClientName());
            metadata.put("client_email", request.getClientEmail());
            metadata.put("invoice_number", invoiceNumber);
            metadata.put("products_count", String.valueOf(request.getProducts().size()));

            // Ajouter les détails des produits (limité par Stripe à 500 caractères par valeur)
            StringBuilder productsJson = new StringBuilder();
            for (int i = 0; i < request.getProducts().size() && i < 5; i++) {
                ProduitAchat product = request.getProducts().get(i);
                if (i > 0) productsJson.append(",");
                productsJson.append(String.format("%s:%d:%.2f",
                    product.getNom().replaceAll("[,:]", ""),
                    product.getQuantite(),
                    product.getPrix()));
            }
            metadata.put("products", productsJson.toString());

            return metadata;
        }

        private String getPaymentMethodType(PaymentIntent paymentIntent) {
            if (paymentIntent.getPaymentMethodObject() != null) {
                PaymentMethod pm = paymentIntent.getPaymentMethodObject();
                return switch (pm.getType()) {
                    case "card" -> "Carte bancaire";
                    case "sepa_debit" -> "Prélèvement SEPA";
                    case "paypal" -> "PayPal";
                    default -> "Autre";
                };
            }
            return "Non spécifié";
        }

        private InvoiceData createInvoiceDataFromMetadata(Map<String, String> metadata, PaymentData paymentData) {
            // Reconstruire les produits depuis les métadonnées
            List<InvoiceData.InvoiceItem> items = parseProductsFromMetadata(metadata.get("products"));

            double subtotal = paymentData.getAmount() / 1.20; // Retirer la TVA
            double taxAmount = paymentData.getAmount() - subtotal;

            InvoiceData.Client client = new InvoiceData.Client(
                metadata.get("client_name"),
                "Adresse non spécifiée", // Vous pouvez stocker cela dans les métadonnées si nécessaire
                metadata.get("client_email"),
                "Téléphone non spécifié"
            );

            return new InvoiceData(
                metadata.get("invoice_number"),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                client,
                items,
                subtotal,
                taxAmount,
                paymentData.getAmount()
            );
        }

        private List<InvoiceData.InvoiceItem> parseProductsFromMetadata(String productsString) {
            // Parse simple des produits depuis les métadonnées
            // Format: "nom1:qty1:prix1,nom2:qty2:prix2"
            if (productsString == null || productsString.isEmpty()) {
                return List.of(new InvoiceData.InvoiceItem("Produit", 1, 0.0, 0.0));
            }

            return List.of(productsString.split(","))
                .stream()
                .map(productStr -> {
                    String[] parts = productStr.split(":");
                    if (parts.length >= 3) {
                        String nom = parts[0];
                        int qty = Integer.parseInt(parts[1]);
                        double prix = Double.parseDouble(parts[2]);
                        return new InvoiceData.InvoiceItem(nom, qty, prix, qty * prix);
                    }
                    return new InvoiceData.InvoiceItem("Produit", 1, 0.0, 0.0);
                })
                .collect(Collectors.toList());
        }

        private String generateInvoiceNumber() {
            String year = String.valueOf(LocalDate.now().getYear());
            String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return "INV-" + year + "-" + uuid;
        }
    }
}