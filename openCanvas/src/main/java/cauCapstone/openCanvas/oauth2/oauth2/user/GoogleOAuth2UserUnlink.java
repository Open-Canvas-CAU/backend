package cauCapstone.openCanvas.oauth2.oauth2.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class GoogleOAuth2UserUnlink implements OAuth2UserUnlink {
	
	//이 URL은 OAuth2 인증을 취소할 때 쓰는 토큰 해제 api url이다.
    private static final String URL = "https://oauth2.googleapis.com/revoke";
    private final RestTemplate restTemplate;
    

    // params에 엑세스 토큰을 담아서 restTemplate로 토큰 해제 post 요청을 한다.
    @Override
    public void unlink(String accessToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("token", accessToken);
        restTemplate.postForObject(URL, params, String.class);
    }
}
