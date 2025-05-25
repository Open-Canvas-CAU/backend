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

import cauCapstone.openCanvas.jwt.JwtTokenizer;
import cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import cauCapstone.openCanvas.oauth2.oauth2.service.OAuth2UserPrincipal;
import cauCapstone.openCanvas.oauth2.oauth2.user.OAuth2Provider;
import cauCapstone.openCanvas.oauth2.oauth2.user.OAuth2UserUnlinkManager;
import cauCapstone.openCanvas.oauth2.util.CookieUtils;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.UserRepository;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.MODE_PARAM_COOKIE_NAME;
import static cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

// 로그인 상황과 회원탈퇴 상황이 있다.
// 상속한 SimpleUrlAuthenticationSuccessHandler는 로그인 성공 시 리다이렉트 하는 로직을 제공한다.
// 리프레시토큰은 로그인 상태에서의 생명주기를 가진다.
@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	//HttpCookie..는 쿠키관련이고, ..UnlinkManager은 연동 해제(회원 탈퇴) 하는데 사용된다.
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final UserRepository userRepository;
    private final OAuth2UserUnlinkManager oAuth2UserUnlinkManager;
    private final JwtTokenizer jwtTokenizer;

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
        	String email = principal.getUsername();	// 이게 이메일
        	
            // 처음 로그인한다면 User 엔티티 만들고 db에 저장함, 따로 oauth2User 엔티티는 없고 Color도 저장안함.
        	// 저장된 유저가 없으면 DB에 저장 (회원가입 처리)
        	User user = userRepository.findByEmail(email)
        		    .orElseGet(() -> {
        		        User newUser = new User(email, email, Role.USER);
        		        return userRepository.save(newUser);
        		    });
        	
            // 서비스 자체 서버에서 액세스 토큰, 리프레시 토큰을 발급한다.
        	// 엑세스토큰 발급.
        	Map<String, Object> claims = Map.of(
        		    "email", principal.getUserInfo().getEmail(),
        		    "role", user.getRole().name()	// TODO: Role enum을 String으로 저장했는데 잘되나 보기.
        		);

        		String accessToken = jwtTokenizer.generateAccessToken(
        		    claims,
        		    principal.getUserInfo().getEmail(),
        		    new Date(System.currentTimeMillis() + 1000 * 60 * 15) // 15분짜리 Access Token
        		);
        	
            // 리프레시토큰 발급 후 레디스에 저장(db에 저장).
        		String refreshToken = jwtTokenizer.generateAndStoreRefreshToken(
        			principal.getUserInfo().getEmail(),
        			new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7) // 7일짜리 Refresh Token
        		);	
        		
        	/*
            log.info("email={}, name={}, nickname={}, accessToken={}", principal.getUserInfo().getEmail(),
                    principal.getUserInfo().getName(),
                    principal.getUserInfo().getNickname(),
                    principal.getUserInfo().getAccessToken()
            );
            */

            // targetUrl에 엑세스 토큰과 리프레시 토큰의 쿼리 패러미터를 붙여서 전송한다.
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("access_token", accessToken)
                    .queryParam("refresh_token", refreshToken)
                    .build().toUriString();

        } else if ("unlink".equalsIgnoreCase(mode)) {

            String accessToken = principal.getUserInfo().getAccessToken();
            OAuth2Provider provider = principal.getUserInfo().getProvider();

            // 유저정보를 DB에서 삭제 한다.
        	String email = principal.getUsername();	// 이게 이메일
        	
        	User user = userRepository.findByEmail(email)
        		    .orElseGet(() -> {
        		    	throw new IllegalArgumentException("회원탈퇴할 유저를 찾을 수 없습니다.");
        		    });
            
        	userRepository.delete(user);
            
            // 리프레시 토큰을 삭제 한다.
        	jwtTokenizer.deleteRefreshTokenBySubject(email);
        	
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
