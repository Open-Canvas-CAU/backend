package cauCapstone.openCanvas.oauth2.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import cauCapstone.openCanvas.oauth2.oauth2.exception.OAuth2AuthenticationProcessingException;
import cauCapstone.openCanvas.oauth2.oauth2.user.OAuth2UserInfo;
import cauCapstone.openCanvas.oauth2.oauth2.user.OAuth2UserInfoFactory;

// oAuth2UserRequest에 담긴 엑세스 토큰을 가지고서 사용자의 정보를 받고, OAuth2User의 구현객체를 oAuth2User에 담고, 
// processOauth2User 메소드를 실행해서 사용자의 정보를 커스터마이징 해서(필요한 정보만 담음)OAuth2User 클래스로 리턴한다.
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {

    	// 사용자의 정보를 oAauth2UserRequest에서 받아서 oAuth2User에 담는다.
    	// OAuth2User는 인터페이스고 super.loadUser 메소드는 구현객체를 리턴한다.
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
        	// 사용자의 정보를 processOauth2User 메소드를 통해 커스터마이징 해서 리턴한다.
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
        	// OAuth2AuthenticationFailureHandler가 작동하려면 AuthenticationException이 던져져야 해서 모든 예외를 AuthenticationException으로 던진다.
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

    	// userRequest 객체에서 registrationId와 accessToken을 뽑는다.
        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId();

        String accessToken = userRequest.getAccessToken().getTokenValue();

        // OAuth2User의 Dto 클래스라고 보면 된다.
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,
                accessToken,
                oAuth2User.getAttributes());

        // 이메일 유효성 검사
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        return new OAuth2UserPrincipal(oAuth2UserInfo);
    }
}
