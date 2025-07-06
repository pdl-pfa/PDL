package odm_finance.finance.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe représentant une réponse API standardisée.
 * Utilisée pour encapsuler le résultat des opérations et fournir des détails sur le statut.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    /**
     * Indique si l'opération a réussi ou échoué.
     */
    private boolean success;
    
    /**
     * Message décrivant le résultat de l'opération.
     */
    private String message;
    
    /**
     * Données retournées par l'opération.
     * Pour les PDFs, contient une représentation base64 du document.
     */
    private Object data;
    
    /**
     * Constructeur pour une réponse de succès avec données.
     * 
     * @param message Message de succès
     * @param data Données à retourner
     */
    public ApiResponse(String message, Object data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }
    
    /**
     * Constructeur pour une réponse de succès sans données.
     * 
     * @param message Message de succès
     */
    public ApiResponse(String message) {
        this.success = true;
        this.message = message;
        this.data = null;
    }
    
    /**
     * Crée une réponse d'erreur.
     * 
     * @param errorMessage Message d'erreur
     * @return Instance ApiResponse configurée comme erreur
     */
    public static ApiResponse error(String errorMessage) {
        return new ApiResponse(false, errorMessage, null);
    }
    
    /**
     * Crée une réponse de succès avec données.
     * 
     * @param message Message de succès
     * @param data Données à retourner
     * @return Instance ApiResponse configurée comme succès
     */
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data);
    }
    
    /**
     * Crée une réponse de succès sans données.
     * 
     * @param message Message de succès
     * @return Instance ApiResponse configurée comme succès
     */
    public static ApiResponse success(String message) {
        return new ApiResponse(true, message, null);
    }
}