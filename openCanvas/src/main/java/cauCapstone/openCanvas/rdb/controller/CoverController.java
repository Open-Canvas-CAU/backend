package cauCapstone.openCanvas.rdb.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.service.CoverService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/covers")
public class CoverController {

    private final CoverService coverService;

    @PostMapping
    @Operation(summary = "커버 생성", description = "새로운 커버를 생성함 커버란 캔버스를 누르기 전에 보이는 겉 표지이다. "
    		+ "누르면 나오는 내용물은 content라고 칭한다, "
    		+ "CoverDto가 필요하다, CoverDto를 반환한다.")
    public ResponseEntity<CoverDto> createCover(@RequestBody CoverDto coverDto) {
        return ResponseEntity.ok(CoverDto.fromEntity(coverService.makeCover(coverDto), null));
    }

    @GetMapping("/all")
    @Operation(summary = "전체 커버 조회 (최신순)", description = "모든 커버를 최신순으로 조회합니다, List<CoverDto>를 반환한다.")
    public ResponseEntity<List<CoverDto>> getAllCovers() {
        return ResponseEntity.ok(coverService.showAllCovers());
    }

    @GetMapping("/likes")
    @Operation(summary = "전체 커버 조회 (좋아요순)", description = "모든 커버를 좋아요 개수 기준으로 정렬하여 조회합니다,"
    		+ "List<CoverDto>를 반환한다")
    public ResponseEntity<List<CoverDto>> getCoversByLikes() {
        return ResponseEntity.ok(coverService.showAllCoversWithLikes());
    }

    @GetMapping("/views")
    @Operation(summary = "전체 커버 조회 (조회수순)", description = "모든 커버를 조회수 기준으로 정렬하여 조회합니다,"
    		+ "List<CoverDto>를 반환한다")
    public ResponseEntity<List<CoverDto>> getCoversByViews() {
        return ResponseEntity.ok(coverService.showAllCoversWithViews());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "커버 삭제", description = "User.ROLE이 ADMIN인 사용자가 커버를 삭제합니다, coverId를 주면된다.")
    public ResponseEntity<String> deleteCover(@PathVariable(name = "id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        coverService.deleteCover(id, email);
        return ResponseEntity.ok("커버 삭제 성공");
    }
}