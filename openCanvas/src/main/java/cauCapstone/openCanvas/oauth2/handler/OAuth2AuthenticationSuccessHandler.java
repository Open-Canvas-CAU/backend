package cauCapstone.openCanvas.oauth2.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import cauCapstone.openCanvas.oauth2.oauth2.service.OAuth2UserPrincipal;
import cauCapstone.openCanvas.oauth2.oauth2.user.OAuth2Provider;

import java.io.IOException;
import java.util.Optional;

import static cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.MODE_PARAM_COOKIE_NAME;
import static cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

// 로그인 상황과 회원탈퇴 상황이 있다.
// 상속한 SimpleUrlAuthenticationSuccessHandler는 로그인 성공 시 리다이렉트 하는 로직을 제공한다.
// TODO: 로그아웃 상황을 구현해야한다. 유저정보는 db에서 지우지 않고, 리프레시 토큰은 db에서 삭제한다.
// 리프레시토큰은 로그인 상태에서의 생명주기를 가진다.
@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	//HttpCookie..는 쿠키관련이고, ..UnlinkManager은 연동 해제(회원 탈퇴) 하는데 사용된다.
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final OAuth2UserUnlinkManager oAuth2UserUnlinkManager;

    // determineTargetUrl 메소드로 로그인을 하는건지 회원탈퇴를 하는건지 판단한 뒤 targetUrl로 리다이렉트 한다.
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String targetUrl;

        targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);	// 임시정보를 정리한다.
        getRedirectStrategy().sendRedirect(request, response, targetUrl);	// 리다이렉트를 한다.
    }

    // login 모드인지, unlink 모드인지 판단하고 targetUrl을 리턴한다.
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {

    	// 쿠키에서 redirectUri와 mode를 꺼낸다.
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());

        String mode = CookieUtils.getCookie(request, MODE_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse("");

        // OAuth2UserPrincipal을 가져온다.
        OAuth2UserPrincipal principal = getOAuth2UserPrincipal(authentication);

        if (principal == null) {
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("error", "Login failed")
                    .build().toUriString();
        }

        if ("login".equalsIgnoreCase(mode)) {
            // TODO: principal에 담긴 정보를 db에 저장해야한다. 이 때 User 정보 관련된 엔티티를 따로 만들어야하고, 
        	//OAuth2와 관련된 User 엔티티, 웹어플리케이션 관련 User 엔티티를 만들어야할 수도 있다.
            // TODO: 서비스 자체 서버에서 액세스 토큰, 리프레시 토큰 발급을 해야한다.
            // TODO: 리프레시 토큰을 DB에 저장 해야한다.
            log.info("email={}, name={}, nickname={}, accessToken={}", principal.getUserInfo().getEmail(),
                    principal.getUserInfo().getName(),
                    principal.getUserInfo().getNickname(),
                    principal.getUserInfo().getAccessToken()
            );
            
            // 임시용 테스트 엑세스 토큰과 리프레시 토큰
            String accessToken = "test_access_token";
            String refreshToken = "test_refresh_token";

            // targetUrl에 엑세스 토큰과 리프레시 토큰의 쿼리 패러미터를 붙여서 전송한다.
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("access_token", accessToken)
                    .queryParam("refresh_token", refreshToken)
                    .build().toUriString();

        } else if ("unlink".equalsIgnoreCase(mode)) {

            String accessToken = principal.getUserInfo().getAccessToken();
            OAuth2Provider provider = principal.getUserInfo().getProvider();

            // TODO: 유저정보를 DB에서 삭제 해야한다.
            // TODO: 리프레시 토큰을 삭제 해야한다.
            // 연동 끊기(회원탈퇴)는 OAuth2UserUnlinkManager가 담당한다.
            oAuth2UserUnlinkManager.unlink(provider, accessToken);

            return UriComponentsBuilder.fromUriString(targetUrl)
                    .build().toUriString();
        }

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", "Login failed")
                .build().toUriString();
    }

    private OAuth2UserPrincipal getOAuth2UserPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2UserPrincipal) {
            return (OAuth2UserPrincipal) principal;
        }
        return null;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
