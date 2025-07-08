package com.JSR.PharmaFlow.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat-gpt")
@CrossOrigin("*")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithGPT(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        logger.info("Processing chat request with prompt: {}", prompt);
        return executeWithRetry(prompt, 3);
    }

    private ResponseEntity<Map<String, String>> executeWithRetry(String prompt, int retriesLeft) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            String content = ((Map<String, String>) choices.get(0).get("message")).get("content");

            return ResponseEntity.ok(Map.of("response", content));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && retriesLeft > 0) {
                try {
                    long waitTime = (long) Math.pow(2, 3 - retriesLeft) * 1000;
                    logger.warn("Rate limited, retrying in {} ms ({} retries left)", waitTime, retriesLeft);
                    Thread.sleep(waitTime);
                    return executeWithRetry(prompt, retriesLeft - 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("Request interrupted during retry", ie);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "Request interrupted"));
                }
            }
            logger.error("OpenAI API error: {}", e.getStatusText());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", "OpenAI API error: " + e.getStatusText()));
        } catch (Exception e) {
            logger.error("Internal server error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}