package cauCapstone.openCanvas.rdb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.entity.Content;

public interface ContentRepository extends JpaRepository<Content, Long>{
	
	// cover_id로 content를 찾아야함.
	Optional<Content> findByCoverId(Long coverId);
	
	// 댓글을 가져와야함
	@Query("SELECT c FROM Content c LEFT JOIN FETCH c.comments WHERE c.id = :id")
	Content findByIdWithComments(@Param("id") Long id);
	
    // 좋아요 개수 구하기.
    @Query("SELECT COUNT(l) FROM Like l WHERE l.content.id = :contentId AND l.liketype = 'LIKE'")
    int countLikesById(@Param("contentId") Long id);
    
    Optional<Content> findByTitle(String title);
}
