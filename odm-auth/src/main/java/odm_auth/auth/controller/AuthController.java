package odm_auth.auth.controller;

import jakarta.validation.Valid;
import odm_auth.auth.dto.AuthRequest;
import odm_auth.auth.dto.AuthResponse;
import odm_auth.auth.entity.User;
import odm_auth.auth.entity.VerificationToken;
import odm_auth.auth.repository.UserRepository;
import odm_auth.auth.repository.VerificationTokenRepository;
import odm_auth.auth.service.EmailService;
import odm_auth.auth.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final EmailService emailService;
    private final VerificationTokenRepository tokenRepository;


    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService, EmailService emailService, VerificationTokenRepository tokenRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already taken");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        // Create token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);

        String link = "http://localhost:8080/auth/verify?token=" + token;
        emailService.sendVerificationEmail(user, link);

        return ResponseEntity.ok("Registration successful. Check your email to verify your account.");
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody @Valid AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String jwt = jwtService.generateToken(user);


        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String token) {
        return tokenRepository.findByToken(token)
                .map(verificationToken -> {
                    if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.badRequest().body("Token expired.");
                    }

                    User user = verificationToken.getUser();
                    user.setEnabled(true);
                    userRepository.save(user);
                    tokenRepository.delete(verificationToken);

                    return ResponseEntity.ok("Account verified successfully.");
                })
                .orElse(ResponseEntity.badRequest().body("Invalid token."));
    }

}
