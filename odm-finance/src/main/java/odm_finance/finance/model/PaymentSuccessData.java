package odm_finance.finance.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PaymentSuccessData {
    @NotBlank(message = "L'ID du PaymentIntent ne peut pas être vide")
    private String paymentIntentId;

    private String paymentMethodId;
    private String receiptUrl;
}