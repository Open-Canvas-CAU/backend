package cauCapstone.openCanvas.rdb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.ReqCommentDto;
import cauCapstone.openCanvas.rdb.dto.ResCommentDto;
import cauCapstone.openCanvas.rdb.entity.Comment;
import cauCapstone.openCanvas.rdb.entity.CommentLike;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.CommentLikeRepository;
import cauCapstone.openCanvas.rdb.repository.CommentRepository;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final ContentRepository contentRepository;
	private final UserRepository userRepository;
	private final CommentLikeRepository commentLikeRepository;
	
	// ! 유저필요
	public Comment save(ReqCommentDto commentDto, String email) {
	    Content content = contentRepository.findById(commentDto.getContentId())
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘텐츠입니다."));
	        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

	        Comment comment = commentDto.toEntity(content, user);
	        return commentRepository.save(comment);
	}
	
	// 글에(content) 있는 댓글 조회
	public List<ResCommentDto> getContentComments(String email, Long contentId) {
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
		
        Content conWithComments = contentRepository.findByIdWithComments(contentId); 
	  
	    List<ResCommentDto> commentDtos = conWithComments.getComments().stream()
	    	    .map(comment -> {
	    	        Long commentId = comment.getId();

	    	        int comLikeNum = commentLikeRepository.countByCommentIdAndLikeType(commentId, LikeType.LIKE);
	    	        int comDisLikeNum = commentLikeRepository.countByCommentIdAndLikeType(commentId, LikeType.DISLIKE);

	    	        LikeType myType = commentLikeRepository.findByUserIdAndCommentId(user.getId(), commentId)
	    	            .map(CommentLike::getLikeType)
	    	            .orElse(null);
	    	        
	    	        return ResCommentDto.fromEntity(comment, comLikeNum, comDisLikeNum, myType);

	    	    })
	    	    .toList();
		
	    return commentDtos;
	}
	
	// ! 유저필요
	// 댓글을 삭제하는 메소드
	public void deleteComment(Long commentId, String email, Long contentId) {
		User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
		
	    Comment comment = commentRepository.findByIdAndUserIdAndContentId(commentId, user.getId(), contentId)
	            .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않거나 삭제 권한이 없습니다."));

	        commentRepository.delete(comment);
	}
	
	// ! 유저필요
    // 좋아요 또는 싫어요를 눌렀을때 토글하기 : 안눌렀던 것을 눌렀으면 기존에 눌렀던 것 찾아서 삭제후 안눌렀던거 추가
	// 댓글 id를 리턴함.
	@Transactional
	public Long toggleLike(String email, Long commentId, LikeType newLikeType) {
	    User user = userRepository.findByEmail(email)
	        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

	    Comment comment = commentRepository.findById(commentId)
	        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

	    Optional<CommentLike> existingLikeOpt = commentLikeRepository.findByUserIdAndCommentId(user.getId(), commentId);
	    if (existingLikeOpt.isPresent()) {
	    	CommentLike existingLike = existingLikeOpt.get();
	    	
            // 1. 같은 타입을 또 누른 경우 → 삭제 (토글 취소)
            if (existingLike.getLikeType() == newLikeType) {
            	commentLikeRepository.delete(existingLike);
                return existingLike.getComment().getId();
            }

            // 2. 다른 타입을 누른 경우 → 기존 삭제 후 새로 생성
            commentLikeRepository.delete(existingLike);
	    }
        // 안 누른 경우 추가
        CommentLike newLike = new CommentLike();
        newLike.setUser(user);
        newLike.setComment(comment);
        commentLikeRepository.save(newLike);
        
        return comment.getId();
	}
	
	// commentId로 ResCommentDto 리턴
	public ResCommentDto getCommentById(Long commentId, String email) {
	    Comment comment = commentRepository.findById(commentId)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
	    
	    // 좋아요/싫어요 수 조회
	    int likeNum = commentLikeRepository.countByCommentIdAndLikeType(commentId, LikeType.LIKE);
	    int disLikeNum = commentLikeRepository.countByCommentIdAndLikeType(commentId, LikeType.DISLIKE);

	    // 유저가 누른 타입
	    Optional<CommentLike> likeOpt = commentLikeRepository.findByUserIdAndCommentId(user.getId(), commentId);
	    LikeType myLikeType = likeOpt.map(CommentLike::getLikeType).orElse(null);

	    // 기본 DTO 생성
	    ResCommentDto dto = ResCommentDto.fromEntity(comment, likeNum, disLikeNum, myLikeType);


	    return dto;
	}
}
