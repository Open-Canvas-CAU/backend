package cauCapstone.openCanvas.oauth2.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import cauCapstone.openCanvas.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import cauCapstone.openCanvas.oauth2.oauth2.service.CustomOAuth2UserService;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
	
	  @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		  	// disable 한것들은 h2 console 때문에 했음. TODO: 배포환경에선 두번째줄인 headers frameOptions는 disable 하면 안됨.
	        http.csrf(AbstractHttpConfigurer::disable)
            .headers(headersConfigurer -> headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)) // For H2 DB
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            // 권한부여(예시로 /api/admin/**은 ADMIN 권한이 있어야한다(url 수정해야할 경우 하기)). 
            // TODO: h2-console 관련은 추후에 지운다, permitAll 전체 열람 가능하다. 로그인 없이도 열람가능한 url은 다시 설정해야한다.
            // 나머지 기능은 권한이 있어야한다. 로그인 없이도 열람가능한 url은 다시 설정해야한다.
            .authorizeHttpRequests((requests) -> requests
                    .requestMatchers(antMatcher("/api/admin/**")).hasRole("ADMIN")
                    .requestMatchers(antMatcher("/api/user/**")).hasRole("USER")
                    .requestMatchers(antMatcher("/h2-console/**")).permitAll()
                    .anyRequest().authenticated()
            )
            //서버 stateless 설정
            .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // oauth2Login: 로그인 기능 관련 진입점
            // httpCookie: 쿠키기반 저장, userService: 로그인 성공 후 후속조치, handler: 성공, 실패시 핸들러
            .oauth2Login(configure ->
            	configure
            		.authorizationEndpoint(config -> config.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
                    .userInfoEndpoint(config -> config.userService(CustomOAuth2UserService))
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
            		);

	        return http.build();
	  }
}
