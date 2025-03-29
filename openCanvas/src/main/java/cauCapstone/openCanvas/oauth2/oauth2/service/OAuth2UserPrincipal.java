package cauCapstone.openCanvas.oauth2.oauth2.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import cauCapstone.openCanvas.oauth2.oauth2.user.OAuth2UserInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

// OAuth2User 인터페이스의 구현객체이다.
// OAuth2UserInfo의 유저정보와 계정이 유효하다는걸 알리는 정보가 있다.
public class OAuth2UserPrincipal implements OAuth2User, UserDetails{
	
    private final OAuth2UserInfo userInfo;

    public OAuth2UserPrincipal(OAuth2UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return userInfo.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return userInfo.getAttributes();
    }

    //TODO: 현재 이부분이 빈 리스트를 반환하게 되있다.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return userInfo.getEmail();
    }

    public OAuth2UserInfo getUserInfo() {
        return userInfo;
    }
}
