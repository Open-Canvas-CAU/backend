package cauCapstone.openCanvas.oauth2.oauth2.exception;

import org.springframework.security.core.AuthenticationException;

// 에러 메세지 던지려고 작성한 코드(OAuth2UserInfoFactory에서 씀)
public class OAuth2AuthenticationProcessingException extends AuthenticationException {
    public OAuth2AuthenticationProcessingException(String msg) {
        super(msg);
    }
}
