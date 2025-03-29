package cauCapstone.openCanvas.oauth2.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// RestTemplate는 HTTP API 요청을 보내고 받기 위한 도구이다.
// 연결 끊기에 사용된다.
// 빈으로 등록해 어플리케이션 전역으로 @Autowired를 이용해 객체를 생성가능하다.
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }
}