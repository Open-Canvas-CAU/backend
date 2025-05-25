package cauCapstone.openCanvas.rdb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.UserDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	
	// 유저정보 저장, 기본적으로 role은 user이다.
    public User save(UserDto userDto) {
    	userDto.setRole(Role.USER);
    	User user = userDto.toEntity();
    	return userRepository.save(user); 
    	}
	
	// id로 유저엔티티 반환
    public Optional<UserDto> getUser(Long id) { 
        return userRepository.findById(id)
                .map((user) -> UserDto.fromEntity(user));
    	}
	
	// 유저가 좋아요한 content 반환
    public List<Content> getLikeContents(Long id){
    	return userRepository.findContentWithLikeByUserId(id);
    }
}
