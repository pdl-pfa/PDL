package odm_finance.finance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentData {
    @NotBlank(message = "Le numéro de transaction ne peut pas être vide")
    private String transactionId;
    
    @NotBlank(message = "Le numéro de facture ne peut pas être vide")
    private String invoiceNumber;
    
    @NotNull(message = "Le montant ne peut pas être null")
    @Positive(message = "Le montant doit être positif")
    private Double amount;
    
    @NotBlank(message = "La méthode de paiement ne peut pas être vide")
    private String paymentMethod;
    
    @NotNull(message = "La date de paiement ne peut pas être null")
    private LocalDateTime paymentDate;
    
    @NotBlank(message = "Le nom du client ne peut pas être vide")
    private String clientName;
    
    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email ne peut pas être vide")
    private String clientEmail;
    
    private String status = "COMPLETED";
}