package com.ticketing.apigateway.service;

import com.ticketing.apigateway.dto.UserValidationRequest;
import com.ticketing.apigateway.dto.UserValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserAuthClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.user.base-url}")
    private String userServiceBaseUrl;

    @Value("${app.security.internal-api-key}")
    private String internalApiKey;

    public Mono<UserValidationResponse> validateCredentials(String username, String password) {
        return webClientBuilder.build()
                .post()
                .uri(userServiceBaseUrl + "/internal/users/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Internal-Token", internalApiKey)
                .bodyValue(new UserValidationRequest(username, password))
                .retrieve()
                .bodyToMono(UserValidationResponse.class);
    }
}
