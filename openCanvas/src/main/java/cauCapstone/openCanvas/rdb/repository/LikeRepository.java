package cauCapstone.openCanvas.rdb.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cauCapstone.openCanvas.rdb.entity.Like;

public interface LikeRepository extends JpaRepository<Like, Long> {
	
	// 글에서 유저가 좋아요를 눌렀던 것을 가져옴.
	Optional<Like> findByUserIdAndContentId(Long userId, Long contentId);
}
