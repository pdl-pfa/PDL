package odm_auth.auth.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import odm_auth.auth.entity.User;
import odm_auth.auth.repository.UserRepository;
import odm_auth.auth.service.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

//controller to do some tests
@RestController
@RequestMapping("/test")
public class TestController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public TestController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @GetMapping
    public User test(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }
    @GetMapping("/token-info")
    public Map<String, Object> getTokenInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        Claims claims = jwtService.extractAllClaims(token);

        return Map.of(
                "email", claims.get("email"),
                "issuedAt", claims.getIssuedAt(),
                "expiresAt", claims.getExpiration(),
                "role", claims.get("role"),
                "name",claims.get("name"),
                "lastName", claims.get("lastName"),
                "address" , claims.get("address"),
                "phone", claims.get("phone")
        );
    }
}
