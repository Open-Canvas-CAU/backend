package cauCapstone.openCanvas.recommend.controller;

import cauCapstone.openCanvas.recommend.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    @Operation(
            summary = "유저 기반 추천",
            description = "JWT에서 유저 ID를 추출해서 유저 기반 추천을 수행합니다. " +
                    "topN이 전달되면 해당 개수만큼 추천, 전달되지 않으면 기본값 5개 추천을 수행합니다."
    )
    @GetMapping("/user")
    public ResponseEntity<String> recommendByUser(
            @RequestParam(required = false) Integer topN, HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        Long userId = recommendService.getUserIdFromEmail(email);

        List<Integer> result = (topN != null)
                ? recommendService.recommendByUser(userId, topN)
                : recommendService.recommendByUser(userId);

        return ResponseEntity.ok(result.toString());
    }

    @Operation(
            summary = "아이템 기반 추천",
            description = "아이템 ID를 기반으로 유사한 아이템을 추천합니다. " +
                    "topN이 전달되면 해당 개수만큼 추천, 전달되지 않으면 기본값 5개 추천을 수행합니다."
    )
    @GetMapping("/item/{itemId}")
    public ResponseEntity<String> recommendByItem(
            @PathVariable Long itemId,
            @RequestParam(required = false) Integer topN) {
        List<Integer> result = (topN != null)
                ? recommendService.recommendByItem(itemId, topN)
                : recommendService.recommendByItem(itemId);

        return ResponseEntity.ok(result.toString());
    }
}