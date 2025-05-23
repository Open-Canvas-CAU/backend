package cauCapstone.openCanvas.rdb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>{
	
	// 댓글의 좋아요를 가져오기.
	@Query("SELECT c FROM Comment c LEFT JOIN FETCH c.commentLikes WHERE c.id = :id")
	Optional<Comment> findByIdWithCommentLikes(@Param("id") Long id);
	
	// 고른 댓글을 userId와 contentId로 확인함. 댓글 삭제용.
	Optional<Comment> findByIdAndUserIdAndContentId(Long id, Long userId, Long contentId);
	
	// 글에(Content) 있는 댓글을 가져옴.
	List<Comment> findByContentId(Long contetnId);
}
