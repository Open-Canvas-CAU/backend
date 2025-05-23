package cauCapstone.openCanvas.rdb.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.ReqCommentDto;
import cauCapstone.openCanvas.rdb.dto.ResCommentDto;
import cauCapstone.openCanvas.rdb.entity.Comment;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.CommentRepository;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final ContentRepository contentRepository;
	private final UserRepository userRepository;
	
	// TODO: User는 나중에 토큰을 염두해두고 메소드를 바꿀 필요가 있다.
	public Comment save(ReqCommentDto commentDto) {
	    Content content = contentRepository.findById(commentDto.getContentId())
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘텐츠입니다."));
	        User user = userRepository.findById(commentDto.getUserId())
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

	        Comment comment = commentDto.toEntity(content, user);
	        return commentRepository.save(comment);
	}
	
	// 글에(content) 있는 댓글 조회
	// TODO: 댓글의 좋아요도 보여주기.
	public List<ResCommentDto> getContentComments(Long contentId) {
       return commentRepository.findByContentId(contentId).stream()
                .map((comment) -> ResCommentDto.fromEntity(comment)).toList();
	}
	
	// 댓글을 삭제하는 메소드
	public void deleteComment(Long commentId, Long userId, Long contentId) {
	    Comment comment = commentRepository.findByIdAndUserIdAndContentId(commentId, userId, contentId)
	            .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않거나 삭제 권한이 없습니다."));

	        commentRepository.delete(comment);
	}
}
