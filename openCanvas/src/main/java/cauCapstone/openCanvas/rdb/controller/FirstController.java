package cauCapstone.openCanvas.rdb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FirstController {
	
	// 아무동작 안하게 해도됨.
    @GetMapping("/")
    @Operation(summary = "테스트용", description = "아무동작안하지만 필요한 컨트롤러.")
    public ResponseEntity<String> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }


        Object principal = auth.getPrincipal();
        String userInfo;

        if (principal instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User oAuth2User) {
            userInfo = "OAuth2 사용자: " + oAuth2User.getAttributes().get("email");
        } else if (principal instanceof String stringPrincipal) {
            userInfo = "JWT 사용자: " + stringPrincipal;
        } else {
            userInfo = "알 수 없는 사용자 유형: " + principal.getClass().getSimpleName();
        }

        return ResponseEntity.ok("현재 로그인된 사용자: " + userInfo);
    }
    
}
