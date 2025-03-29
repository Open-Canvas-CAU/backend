package cauCapstone.openCanvas.oauth2.oauth2.user;

import java.util.Map;

// Oauth2UserInfo를 위한 인터페이스이다.
public interface OAuth2UserInfo {

    OAuth2Provider getProvider();

    String getAccessToken();

    Map<String, Object> getAttributes();

    String getId();

    String getEmail();

    String getName();

    String getFirstName();

    String getLastName();

    String getNickname();

    String getProfileImageUrl();
    
}
