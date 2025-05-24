package cauCapstone.openCanvas.rdb.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.repository.LikeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService {
	private final LikeRepository likeRepository;
	
	// 유저가 좋아요 했던 cover 목록을 가져옴.
	public List<CoverDto> getLikeCover(Long userId){
		List<Like> likes = likeRepository.findByUserIdAndLiketype(userId, LikeType.LIKE);
		
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
