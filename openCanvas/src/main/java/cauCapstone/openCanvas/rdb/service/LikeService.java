package cauCapstone.openCanvas.rdb.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.LikeRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {
	private final LikeRepository likeRepository;
	private final UserRepository userRepository;
	
	
	// !유저필요
	// 유저가 좋아요 했던 cover 목록을 가져옴.
	public List<CoverDto> getLikeCover(String email){
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
		List<Like> likes = likeRepository.findByUserIdAndLiketype(user.getId(), LikeType.LIKE);
		
		// 유저가 좋아요 누른게 없으면 빈 리스트를 리턴함.
		if(likes.isEmpty()) {
			return List.of();
		}
		
		List<CoverDto> coverDtos = likes.stream()
				.map(like -> CoverDto.fromEntity(like.getContent().getCover()))
				.distinct()
				.toList();
		
		return coverDtos;
	}
}
