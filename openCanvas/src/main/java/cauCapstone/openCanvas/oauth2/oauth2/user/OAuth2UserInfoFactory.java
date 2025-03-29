package cauCapstone.openCanvas.oauth2.oauth2.user;

import java.util.Map;

import cauCapstone.openCanvas.oauth2.oauth2.exception.OAuth2AuthenticationProcessingException;

// 엑세스 토큰으로 유저정보를 받아오고나서 구글 유저인지, 네이버 유저인지 등등 어디서 로그인했는지 파악해서 해당하는 Oauth2UserInfo 객체를 만들어서 리턴한다.
// TODO: 구글밖에 안만들었지만, 확장가능하다.
// TODO: 엑세스 토큰으로 유저정보를 받은건지, 서비스 엑세스 토큰으로 유저정보를 받은건지 확인하기.
public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId,
            String accessToken,
            Map<String, Object> attributes) {
    	if (OAuth2Provider.GOOGLE.getRegistrationId().equals(registrationId)) {
    		return new GoogleOAuth2UserInfo(accessToken, attributes);
    	} else {
    		throw new OAuth2AuthenticationProcessingException("Login with " + registrationId + " is not supported");
    	}
    }
}
