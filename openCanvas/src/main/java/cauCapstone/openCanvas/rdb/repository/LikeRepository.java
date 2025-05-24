package cauCapstone.openCanvas.rdb.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;

public interface LikeRepository extends JpaRepository<Like, Long> {
	
	// 유저가 좋아요를 눌렀던 Like 객체를 가져옴. -> 눌러서 글(content)로 접근할 수 있음.
	List<Like> findByUserIdAndLiketype(Long userId, LikeType liketype);
	
	// 글에서 유저가 좋아요를 눌렀던 것을 가져옴. -> 좋아요 취소, 싫어요로 바꾸기를 위한 메소드
	Optional<Like> findByUserIdAndContentId(Long userId, Long contentId);
}
