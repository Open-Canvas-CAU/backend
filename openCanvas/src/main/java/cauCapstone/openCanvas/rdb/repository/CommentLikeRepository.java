package cauCapstone.openCanvas.rdb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cauCapstone.openCanvas.rdb.entity.CommentLike;
import cauCapstone.openCanvas.rdb.entity.LikeType;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long>{
	
    // 특정 유저가 특정 댓글에 누른 좋아요 타입 조회
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);
    
    void deleteByUserIdAndCommentId(Long userId, Long commentId);
    
    long countByCommentId(Long commentId);
    
    // 특정 댓글에 대해 특정 타입의 좋아요 수 구하기
    int countByCommentIdAndLikeType(Long commentId, LikeType likeType);

}
