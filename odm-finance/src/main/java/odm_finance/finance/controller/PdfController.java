package odm_finance.finance.controller;

import lombok.RequiredArgsConstructor;
import odm_finance.finance.model.ApiResponse;
import odm_finance.finance.model.InvoiceData;
import odm_finance.finance.model.ProduitAchat;
import odm_finance.finance.service.InvoiceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final InvoiceService invoiceService;

    /**
     * Génère un PDF et retourne une réponse JSON avec le PDF encodé en base64
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse> generatePdf(@RequestBody List<ProduitAchat> produitsAchat) {
        try {
            // Validation des données d'entrée
            if (produitsAchat == null || produitsAchat.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("La liste des produits ne peut pas être vide")
                );
            }

            // Créer les données de facture
            InvoiceData invoiceData = createInvoiceData(produitsAchat);
            
            // Générer le PDF
            byte[] pdfBytes = invoiceService.generateInvoicePdf(invoiceData);
            
            // Encoder le PDF en base64
            String base64Pdf = java.util.Base64.getEncoder().encodeToString(pdfBytes);
            
            return ResponseEntity.ok(ApiResponse.success("PDF généré avec succès", base64Pdf));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erreur lors de la génération du PDF: " + e.getMessage())
            );
        }
    }
    
    /**
     * Génère un PDF et retourne directement le fichier pour téléchargement
     */
    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadPdf(@RequestBody List<ProduitAchat> produitsAchat) {
        try {
            // Validation des données d'entrée
            if (produitsAchat == null || produitsAchat.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Créer les données de facture
            InvoiceData invoiceData = createInvoiceData(produitsAchat);
            
            // Générer le PDF
            byte[] pdfBytes = invoiceService.generateInvoicePdf(invoiceData);
            
            // Configurer les en-têtes pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "facture-" + invoiceData.getNumber() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint pour prévisualiser le PDF dans le navigateur
     */
    @PostMapping("/preview")
    public ResponseEntity<byte[]> previewPdf(@RequestBody List<ProduitAchat> produitsAchat) {
        try {
            if (produitsAchat == null || produitsAchat.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            InvoiceData invoiceData = createInvoiceData(produitsAchat);
            byte[] pdfBytes = invoiceService.generateInvoicePdf(invoiceData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", 
                "facture-" + invoiceData.getNumber() + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Crée les données de facture à partir de la liste des produits
     */
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
        
        // Créer les données client (remplacer par les vraies données)
        InvoiceData.Client client = new InvoiceData.Client(
            "Client Exemple",
            "123 Rue Exemple, 75000 Paris, France",
            "client@exemple.com",
            "01 23 45 67 89"
        );
        
        // Créer la facture
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
    
    /**
     * Génère un numéro de facture unique
     */
    private String generateInvoiceNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "INV-" + year + "-" + uuid;
    }
}