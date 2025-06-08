package cauCapstone.openCanvas.rdb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.dto.UserDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	
	/*
	// 유저정보 저장, 기본적으로 role은 user이다.
    public User save(String email) {
    	userDto.setRole(Role.USER);
    	User user = userDto.toEntity();
    	return userRepository.save(user); 
    	}
    */
	
	// ! 유저필요
	// 유저가 있을때 색상을 설정하는 메소드.
    public User saveColor(String email, String color) {	
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    	
        user.setColor(color);
        
    	return userRepository.save(user); 
    	}
	
	// ! 유저필요
	// id로 유저엔티티 반환
    public Optional<UserDto> getUser(String email) { 
    	
        return userRepository.findByEmail(email)
                .map((user) -> UserDto.fromEntity(user));
    	}
	
    // ! 유저필요
	// 유저가 좋아요한 content 반환
    public List<CoverDto> getLikeContents(String email){
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    	
    	List<Cover> covers = userRepository.findCoversLikedByUserId(user.getId());
    	
        return covers.stream()
                .map(cover -> {
                    Long contentId = cover.getContent() != null ? cover.getContent().getId() : null;
                    return CoverDto.fromEntity(cover, contentId);
                })
                .toList();
    }
}
