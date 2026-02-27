package yh_project.openapi.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiClient {

    @Value("${env.ai_api_key}")
    private String apiKey;

    @Value("${env.ai_gemini_model}")
    private String model;

    private final WebClient.Builder webClientBuilder;

    public GeminiDto.Response generateContent(GeminiDto.Request request) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        return webClientBuilder.build()
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiDto.Response.class)
                .block();
    }
}

