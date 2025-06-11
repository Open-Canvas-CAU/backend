package cauCapstone.openCanvas.rdb.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;

    @Operation(summary = "장르 설정", description = """
    		장르이름 리스트, contentId를 받음
            특정 콘텐츠에 장르를 설정합니다. 
            최초 작성자만 설정할 수 있습니다,
            
            장르목록 List<String>으로 리턴함.  
            """)
    @PostMapping("/set")
    public ResponseEntity<?> setGenre(
            @RequestParam(name = "contentId") Long contentId,
            @RequestBody List<String> genres
    ) {
    	
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("로그인되지 않음");
        }
        String email = (String) auth.getPrincipal();
        
        List<String> savedGenres = genreService.setGenre(email, genres, contentId);
        return ResponseEntity.ok(savedGenres);
    }

    // ✅ 전체 장르 목록 조회
    @GetMapping("/all")
    public ResponseEntity<List<String>> getAllGenres() {
        return ResponseEntity.ok(genreService.getAllGenres());
    }

    @Operation(summary = "content 하나의 장르 목록 반환", 
    		description = "contentId를 입력받아서 해당 content의 장르를 List<String>으로 리턴함")
    @GetMapping("/content/{contentId}")
    public ResponseEntity<List<String>> getGenresByContentId(@PathVariable(name = "contentId") Long contentId) {
        return ResponseEntity.ok(genreService.getGenre(contentId));
    }

    // ✅ 장르명으로 검색하여 해당 장르의 CoverDto 리스트 반환
    @Operation(summary = "장르로 검색하면 coverDto 반환", description = "장르 이름으로 검색하면 coverDto반환")
    @GetMapping("/search")
    public ResponseEntity<List<CoverDto>> searchGenre(@RequestParam(name = "genreName") String genreName) {
        return ResponseEntity.ok(genreService.searchGenre(genreName));
    }
}