package cauCapstone.openCanvas.openai.imagegenerator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ImageGeneratorService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateImage(String prompt) {
        String url = "https://api.openai.com/v1/images/generations";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "dall-e-3");
        body.put("prompt", prompt);
        body.put("n", 1);
        body.put("size", "1024x1024");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<Map<String, String>> data = (List<Map<String, String>>) response.getBody().get("data");
            return data.get(0).get("url");
        } else {
            throw new RuntimeException("OpenAI 이미지 생성 실패: " + response.getStatusCode());
        }
    }
}