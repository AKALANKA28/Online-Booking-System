package com.ticketing.apigateway.filter;

import com.ticketing.apigateway.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (isPublic(request)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtService.parse(token);
            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Id", String.valueOf(claims.get("userId")))
                    .header("X-User-Email", String.valueOf(claims.get("email")))
                    .header("X-User-Role", String.valueOf(claims.get("role")))
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (JwtException ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublic(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if (path.startsWith("/auth/") || path.startsWith("/actuator/") || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs") || path.startsWith("/webjars")) {
            return true;
        }

        if (HttpMethod.POST.equals(method) && path.equals("/api/users/register")) {
            return true;
        }

        return HttpMethod.GET.equals(method)
                && (path.startsWith("/api/events") || path.startsWith("/api/seats/events"));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
