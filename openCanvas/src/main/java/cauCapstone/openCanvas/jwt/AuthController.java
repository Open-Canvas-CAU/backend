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
@Tag(name = "엑세스 토큰 재발급 API", description = """
		 [JWT 토큰 재발급 가이드]
 1. Access Token이 만료되었을 때

    서버에 요청을 보낼 때 Authorization: Bearer {accessToken} 헤더가 필요한데,
    이 토큰이 만료되면 401 Unauthorized 응답이 옵니다.

 2. Refresh API로 Access Token 재발급 받기

    만료된 Access Token 대신 저장해둔 Refresh Token을 사용해
    새로운 Access Token을 발급받을 수 있습니다.

요청 예시

POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR..."
}

응답 예시 (200 OK)

{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
}

    새로 발급된 Access Token은 이후 요청 시 Authorization 헤더에 넣어 사용합니다.

Authorization: Bearer 새_엑세스토큰
		""")
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
