package odm_auth.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;


public record AuthRequest (
        @NotNull(message = "email is required")
        @Email(message = "email is not valid")
        String email,
        @NotNull(message = "password is required")
        String password){
}