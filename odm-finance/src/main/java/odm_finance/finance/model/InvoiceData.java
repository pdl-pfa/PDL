package odm_finance.finance.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceData {
    private String number;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private Client client;
    private List<InvoiceItem> items;
    private double subtotal;
    private double taxAmount;
    private double total;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Client {
        private String name;
        private String address;
        private String email;
        private String phone;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItem {
        private String description;
        private int quantity;
        private double unitPrice;
        private double total;
    }
}