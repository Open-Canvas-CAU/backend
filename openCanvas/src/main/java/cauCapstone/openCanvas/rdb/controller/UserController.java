package cauCapstone.openCanvas.rdb.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.dto.UserDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
	
    private final UserService userService;

    // 유저의 색상을 변경하는 API
    @PutMapping("/color")
    @Operation(summary = "유저가 글쓰는 색상을 변경", description = "처음에 웹을 시작할 때 색깔을 설정하게 하면 된다, "
    		+ "color 지정 필요(임시로 string 타입으로함)")
    public ResponseEntity<String> updateColor(@RequestParam(name = "color") String color) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }

        String email = (String) auth.getPrincipal();
        User updatedUser = userService.saveColor(email, color);

        return ResponseEntity.ok("색상 변경 성공: " + updatedUser.getColor());
    }

    // 유저가 좋아요한 커버 목록 반환
    @GetMapping("/likes")
    @Operation(summary = "유저가 좋아요한 coverDto 반환", description = "유저가 좋아요한 coverDto 반환, "
    		+ "좋아요 누른 작품을 찾을 수 있다")
    public ResponseEntity<List<CoverDto>> getLikedContents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String email = (String) auth.getPrincipal();
        List<CoverDto> likedContents = userService.getLikeContents(email);

        return ResponseEntity.ok(likedContents);
    }

    @GetMapping("/")
    @Operation(summary = "현재 유저 정보", description = "현재 로그인한 userDto가 반환됨")
    public ResponseEntity<UserDto> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String email = (String) auth.getPrincipal();
        return userService.getUser(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
}
