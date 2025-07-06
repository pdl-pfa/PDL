package odm_finance.finance.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import odm_finance.finance.model.InvoiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;

@Service
public class InvoiceService {

    private final TemplateEngine templateEngine;

    @Autowired
    public InvoiceService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Génère un document PDF à partir des données de facturation fournies.
     *
     * @param invoiceData Les données nécessaires pour générer la facture
     * @return Un tableau d'octets contenant le document PDF
     * @throws IllegalArgumentException Si les données d'entrée sont invalides
     * @throws RuntimeException Si une erreur survient lors de la génération du PDF
     */
    public byte[] generateInvoicePdf(InvoiceData invoiceData) {
        // Validation des données d'entrée
        if (invoiceData == null) {
            throw new IllegalArgumentException("Les données de facture ne peuvent pas être nulles");
        }
        if (invoiceData.getClient() == null) {
            throw new IllegalArgumentException("Les données client ne peuvent pas être nulles");
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Préparer le contexte Thymeleaf avec les données
            Context context = new Context();
            context.setVariable("invoice", invoiceData);
            context.setVariable("client", invoiceData.getClient());

            // Traiter le template avec Thymeleaf
            String processedHtml = templateEngine.process("invoice-template", context);

            // Générer le PDF
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(processedHtml, "/");  // Utiliser une base URI valide
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        }
    }
}