package odm_finance.finance.model;

import lombok.Data;
import jakarta.validation.constraints.*;

import java.util.List;

@Data
public class PaymentIntentRequest {
    @NotNull(message = "Le montant ne peut pas être null")
    @Positive(message = "Le montant doit être positif")
    private Long amount; // Montant en centimes (ex: 1000 = 10.00€)

    @NotBlank(message = "La devise ne peut pas être vide")
    @Pattern(regexp = "^[a-z]{3}$", message = "La devise doit être un code ISO à 3 lettres")
    private String currency = "eur";

    @NotBlank(message = "Le nom du client ne peut pas être vide")
    private String clientName;

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email ne peut pas être vide")
    private String clientEmail;

    @NotEmpty(message = "La liste des produits ne peut pas être vide")
    private List<ProduitAchat> products;

    private String description;
    private String receiptEmail;
}