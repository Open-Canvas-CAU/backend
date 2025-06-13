package cauCapstone.openCanvas.oauth2.config;

import cauCapstone.openCanvas.jwt.JwtAuthenticationFilter;
import cauCapstone.openCanvas.jwt.JwtTokenizer;
import cauCapstone.openCanvas.oauth2.handler.OAuth2AuthenticationFailureHandler;
import cauCapstone.openCanvas.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import cauCapstone.openCanvas.oauth2.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import cauCapstone.openCanvas.oauth2.oauth2.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final JwtTokenizer jwtTokenizer;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headersConfigurer -> headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                    )
                .authorizeHttpRequests((requests) -> requests
                        // 1. CORS Preflight 요청은 인증 없이 항상 허용합니다.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. 인증 없이 누구나 접근 가능한 경로들을 명확하게 지정합니다.
                        .requestMatchers(
                                "/auth/**",
                                "/oauth2/**",
                                "/login/**",
                                "/api/covers",
                                "/api/covers/**", // 모든 커버 관련 API 허용
                                "/api/contents/{contentId}", // 컨텐츠 상세 보기 허용
                                "/api/comments/by-content", // 댓글 보기 허용
                                "/ws-stomp/**" // 웹소켓 연결 경로 허용
                        ).permitAll()
                        
                        // 개발용 h2 콘솔
                        .requestMatchers(antMatcher("/h2-console/**")).permitAll()

                        // 3. 그 외의 모든 요청은 최소한 인증(로그인)이 되어야 접근 가능하도록 설정합니다.
                        // 이 규칙으로 /api/rooms/create, /api/writings/set/official 등이 보호됩니다.
                        .anyRequest().authenticated()
                )
                .oauth2Login(configure ->
                    configure
                        .authorizationEndpoint(config -> config.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
                        .userInfoEndpoint(config -> config.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenizer), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
     
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // 모든 origin 허용
        config.setAllowedMethods(List.of("*")); // 모든 메서드 허용
        config.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
        config.setAllowCredentials(false); // 쿠키 미허용 (true로 바꿔도 됨)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 적용
        return source;
        // CorsConfiguration config = new CorsConfiguration();
        // config.setAllowCredentials(true);
        // config.setAllowedOriginPatterns(List.of("http://localhost:5173")); // 프론트엔드 주소
        // config.setAllowedHeaders(List.of("*"));
        // config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // config.setExposedHeaders(List.of("Authorization", "Authorization-Refresh"));

        // UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // source.registerCorsConfiguration("/**", config);
        // return source;
    }
}
