package odm_gateway.gateway.filter;

import io.jsonwebtoken.Claims;
import odm_gateway.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {


    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                // Vérifie la présence du header Authorization
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return this.onError(exchange, "Missing Authorization Header");
                }

                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    jwtUtil.validateToken(authHeader);
                    // Récupère les claims du token
                    Claims claims = jwtUtil.extractAllClaims(authHeader);

                    // Extrait les valeurs des claims
                    String email = claims.get("email", String.class);
                    String phone = claims.get("phone", String.class);
                    String name = claims.get("name", String.class);
                    String lastName = claims.get("lastName", String.class);
                    String address = claims.get("address", String.class);

                    // Modifie la requête en ajoutant les headers de user Connecte
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("userEmail", email)
                            .header("userPhone", phone)
                            .header("userName", name)
                            .header("userLastName", lastName)
                            .header("userAddress", address)
                            .build();

                    exchange = exchange.mutate().request(modifiedRequest).build();
                } catch (Exception e) {
                    return this.onError(exchange, "Invalid Token: Unauthorized Access");
                }
            }
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String errorBody = String.format("{\"error\": \"%s\"}", err);
        byte[] bytes = errorBody.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }


    public static class Config {

    }
}

