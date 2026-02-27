package yh_project.openapi.ai;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiUsageRepository aiUsageRepository;
    private final StringRedisTemplate redisTemplate;
    private final Gson gson;
    private final GeminiClient geminiClient;
    private final AiConfig aiConfig;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        String userId = request.getUserId();

        // 1. Check Global Cost Limit
        Double totalUsage = aiUsageRepository.getTotalUsageCost();
        if (totalUsage != null && totalUsage >= aiConfig.getCostLimitKrw()) {
            throw new AiQuotaExceededException("Global API usage limit exceeded.");
        }

        // 2. Check User Request Limit
        AiUsage userUsage = aiUsageRepository.findById(userId)
                .orElse(AiUsage.builder().userId(userId).requestCount(0).totalCost(0.0).build());

        if (userUsage.getRequestCount() >= aiConfig.getReqLimitPerUser()) {
             throw new AiQuotaExceededException("User request limit exceeded.");
        }

        // 3. Retrieve History
        String historyKey = "chat:history:" + userId;
        List<String> historyJson = redisTemplate.opsForList().range(historyKey, 0, -1);

        List<GeminiDto.Request.Content> contents = new ArrayList<>();
        if (historyJson != null) {
            for (String json : historyJson) {
                try {
                    contents.add(gson.fromJson(json, GeminiDto.Request.Content.class));
                } catch (Exception e) {
                    log.error("Failed to parse history", e);
                }
            }
        }

        GeminiDto.Request.Part userPart = GeminiDto.Request.Part.builder().text(request.getMessage()).build();
        GeminiDto.Request.Content userContent = GeminiDto.Request.Content.builder()
                .role("user")
                .parts(Collections.singletonList(userPart))
                .build();
        contents.add(userContent);

        // Prepare Request (System instruction)
        GeminiDto.Request.Content systemInstruction = GeminiDto.Request.Content.builder()
                .parts(Collections.singletonList(GeminiDto.Request.Part.builder().text(aiConfig.getSystemPrompt()).build()))
                .build();

        GeminiDto.Request geminiRequest = GeminiDto.Request.builder()
                .contents(contents)
                .systemInstruction(systemInstruction)
                .build();

        // 4. Call API
        GeminiDto.Response response = geminiClient.generateContent(geminiRequest);

        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new RuntimeException("No response from AI");
        }

        String aiText = response.getCandidates().get(0).getContent().getParts().get(0).getText();

        // 5. Update History
        GeminiDto.Request.Content modelContent = GeminiDto.Request.Content.builder()
                .role("model")
                .parts(Collections.singletonList(GeminiDto.Request.Part.builder().text(aiText).build()))
                .build();

        try {
            redisTemplate.opsForList().rightPush(historyKey, gson.toJson(userContent));
            redisTemplate.opsForList().rightPush(historyKey, gson.toJson(modelContent));
            redisTemplate.expire(historyKey, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Failed to save history", e);
        }

        // 6. Update Usage
        userUsage.setRequestCount(userUsage.getRequestCount() + 1);

        // Cost calculation
        double cost = 0.0;
        if (response.getUsageMetadata() != null) {
            int promptTokens = response.getUsageMetadata().getPromptTokenCount();
            int candidateTokens = response.getUsageMetadata().getCandidatesTokenCount();

            // Approximate pricing (Flash)
            // Input: $0.35 / 1M => 0.00000035 * 1400 KRW = 0.00049 KRW/token
            // Output: $1.05 / 1M => 0.00000105 * 1400 KRW = 0.00147 KRW/token

            double inputCost = promptTokens * 0.00049;
            double outputCost = candidateTokens * 0.00147;
            cost = inputCost + outputCost;
        }

        userUsage.setTotalCost(userUsage.getTotalCost() + cost);
        aiUsageRepository.save(userUsage);

        return new ChatResponse(aiText);
    }
}

