package cauCapstone.openCanvas.rdb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents")
public class ContentController {

    private final ContentService contentService;

    @GetMapping("/{coverId}")
    @Operation(summary = "컨텐츠 조회", description = "커버 ID에 해당하는 컨텐츠를 조회하거나 없으면 생성하며, "
    		+ "조회수 증가 및 내가 좋아요 또는 싫어요를 눌렀는지도 알려줌,"
    		+ "coverId가 필요하고, 성공했을때는 contentDto를 실패했을때는 string을 리턴함")
    public ResponseEntity<?> getContent(@PathVariable(name = "coverId") Long coverId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        ContentDto contentDto = contentService.getContent(coverId, email);
        return ResponseEntity.ok(contentDto);
    }

    @PostMapping("/like-toggle")
    @Operation(summary = "좋아요/싫어요 토글", description = "사용자가 해당 컨텐츠에 대해 좋아요 또는 싫어요를 토글합니다. "
    		+ "동일한 타입을 누르면 취소됩니다(좋아요 2번누르면취소),"
    		+ "contentId, likeType 필요함, contentDto 리턴함")
    public ResponseEntity<?> toggleLike(
            @RequestParam Long contentId,
            @RequestParam LikeType likeType) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        Long coverId = contentService.toggleLike(email, contentId, likeType);
        ContentDto contentDto = contentService.getContent(coverId, email);
        return ResponseEntity.ok(contentDto);
    }
}