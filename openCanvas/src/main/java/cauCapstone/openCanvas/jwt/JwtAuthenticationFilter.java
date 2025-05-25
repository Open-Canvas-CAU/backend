package cauCapstone.openCanvas.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenizer jwtTokenizer;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null) {
            try {
                Claims claims = jwtTokenizer.verifySignature(token, jwtTokenizer.encodeBase64SecretKey());
                String email = claims.get("email", String.class);
                String role = claims.get("role", String.class);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, null, null // 권한이 있으면 GrantedAuthority로 넣기
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ✅ SecurityContext에 등록!
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException ex) {
                // 유효하지 않은 JWT → 인증은 실패하지만 요청은 계속 진행
                logger.warn("JWT 검증 실패: " + ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    // HTTP 요청 헤더에서 토큰 문자열만 꺼내는 역할
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}