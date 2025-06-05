package cauCapstone.openCanvas.openai.novelsummary.service;

import cauCapstone.openCanvas.openai.imagegenerator.prompt.ImageGeneratorPromptBuilder;
import cauCapstone.openCanvas.openai.novelsummary.prompt.NovelSummaryPromptBuilder;
import cauCapstone.openCanvas.openai.novelsummary.util.TextUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NovelSummaryService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final int MAX_BYTE_LENGTH = 2700;
    private final TextUtil textUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NovelSummaryService(TextUtil textUtil) {
        this.textUtil = textUtil;
    }

    public String summarizeIfExceeds(String title, String[] genres, String content) {
        if (textUtil.getUtf8BytesLength(content) <= MAX_BYTE_LENGTH) {
            return content;
        }

        String prompt = NovelSummaryPromptBuilder.buildPrompt(title, genres, content);
        String summarized = requestSummary(prompt);

        if (textUtil.getUtf8BytesLength(summarized) > MAX_BYTE_LENGTH) {
            summarized = textUtil.truncateToMaxBytes(summarized, MAX_BYTE_LENGTH);
        }

        return summarized;
    }

    private String requestSummary(String prompt) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("choices").get(0).path("message").path("content").asText();
            } catch (Exception e) {
                throw new RuntimeException("요약 응답 파싱 실패", e);
            }
        } else {
            throw new RuntimeException("요약 요청 실패: " + response.getStatusCode());
        }
    }
}
