package cauCapstone.openCanvas.oauth2.oauth2.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// OAuth2UserInfoFactory 객체가 OAuth2UserInfo 객체를 리턴할 때, 
// 이 enum의 registrationId와 엑세스 토큰의 registrationId를 비교해서 어디서 로그인한건지 찾는다.
@Getter // getRegistrationId를 말한다.
@RequiredArgsConstructor // @RequiredArgsConstructor는 registrationId를 파라미터로 하는 생성자를 갖는다.
public enum OAuth2Provider {
    GOOGLE("google"), // GOOGLE은 OAuth2Provider 클래스(enum)이고, "google"은 regisstrationId 필드이다. 
    FACEBOOK("facebook"),
    GITHUB("github"),
    NAVER("naver"),
    KAKAO("kakao");

    private final String registrationId;
}