package cauCapstone.openCanvas.oauth2.oauth2.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import cauCapstone.openCanvas.oauth2.oauth2.exception.OAuth2AuthenticationProcessingException;

// mode가 "unlink"일 때 provider가 구글인지 네이버인지 등을 찾고, 엑세스 토큰으로 연동을 해제한다.
@RequiredArgsConstructor
@Component
public class OAuth2UserUnlinkManager {
	private final GoogleOAuth2UserUnlink googleOAuth2UserUnlink;
	
    public void unlink(OAuth2Provider provider, String accessToken) {
        if (OAuth2Provider.GOOGLE.equals(provider)) {
            googleOAuth2UserUnlink.unlink(accessToken);
        } else {
            throw new OAuth2AuthenticationProcessingException(
                    "Unlink with " + provider.getRegistrationId() + " is not supported");
        }
    }
}
