package odm_finance.finance.model;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProduitAchat {
    private String nom;
    private double prix;
    private int quantite;

    public double getTotal() {
        return prix * quantite;
    }
}