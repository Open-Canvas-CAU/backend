package cauCapstone.openCanvas.jwt;

import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "인증 관련 API", description = "JWT 토큰 인증 및 재발급 처리")
public class AuthController {

    private final JwtTokenizer jwtTokenizer;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    @Operation(
            summary = "엑세스 토큰 재발급",
            description = "리프레시 토큰을 이용해 새로운 엑세스 토큰을 발급받습니다.")
            @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "새로운 엑세스 토큰 발급 성공"),
                @ApiResponse(responseCode = "401", description = "리프레시 토큰이 없거나 유효하지 않음")
            }
        )
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        try {
            // 1. 서명 검증
            Claims claims = jwtTokenizer.verifySignature(refreshToken, jwtTokenizer.encodeBase64SecretKey());
            String email = claims.getSubject();

            // 2. Redis에서 저장된 리프레시 토큰 검증
            RefreshToken savedToken = jwtTokenizer.refreshTokenRepository.findById(email)
                    .orElseThrow(() -> new RuntimeException("저장된 리프레시 토큰이 없습니다."));

            if (!savedToken.getToken().equals(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 일치하지 않습니다.");
            }

            // 3. 유저 DB에서 권한 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

            // 4. 새 엑세스 토큰 발급
            Map<String, Object> newClaims = Map.of("email", user.getEmail(),
            	    					"role", user.getRole().name());
            Date newExpiration = jwtTokenizer.createAccessTokenExpiration();

            String newAccessToken = jwtTokenizer.generateAccessToken(newClaims, user.getEmail(), newExpiration);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰 검증 실패: " + e.getMessage());
        }
    }
}
