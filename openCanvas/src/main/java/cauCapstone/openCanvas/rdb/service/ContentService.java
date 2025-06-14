package cauCapstone.openCanvas.rdb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.dto.ResCommentDto;
import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.entity.Comment;
import cauCapstone.openCanvas.rdb.entity.CommentLike;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.CommentLikeRepository;
import cauCapstone.openCanvas.rdb.repository.CommentRepository;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.rdb.repository.LikeRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import cauCapstone.openCanvas.recommend.service.RecommendService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {
	private final ContentRepository contentRepository;
	private final CoverRepository coverRepository;
	private final LikeRepository likeRepository;
	private final UserRepository userRepository;
	private final CommentLikeRepository commentLikeRepository;
	private final RecommendService recommendService;
	
	// ! 유저필요
	// coverId를 받아서 Content를 리턴하는 메소드, Content가 없으면 새로 저장한다.
	public ContentDto getContent(Long coverId, String email, boolean isView) {
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

	    Content content = contentRepository.findByCoverId(coverId)
	        .orElseGet(() -> {
	        	
	        	// 기존 Content가 없는 경우 Cover을 조회함.
	            Cover cover = coverRepository.findById(coverId)
	                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Cover입니다."));

	            // Content 생성 및 저장
	            Content newContent = new Content(cover);
	            return contentRepository.save(newContent);
	        });
	    
	    // 댓글을 찾음.
	    Content conWithComments = contentRepository.findByIdWithComments(content.getId()); 
	    
	    if(isView) {
		    // 조회수 +1 함.
		    conWithComments.setView(conWithComments.getView() + 1);
		    contentRepository.save(conWithComments);	
	    }
	    
	    // 추천 서버에 유저 조회 기록 전송
	    recommendService.createUserView(user.getId(), conWithComments.getId());
	    
	    // 좋아요 갯수를 찾음.
	    int likeNum = contentRepository.countLikesById(conWithComments.getId());
	    
	    // 유저 본인이 좋아요 또는 싫어요를 눌렀는지 확인하는 메소드.
	    Optional<Like> like = likeRepository.findByUserIdAndContentId(user.getId(), conWithComments.getId());
	    LikeType likeType = like.map((a) -> a.getLiketype()).orElse(null);
	    
	    
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

	    return ContentDto.fromEntityWithLike(commentDtos, conWithComments, likeNum, likeType);
	    
	}
	
	// ! 유저필요
    // 좋아요 또는 싫어요를 눌렀을때 토글하기 : 안눌렀던 것을 눌렀으면 기존에 눌렀던 것 찾아서 삭제후 안눌렀던거 추가
    @Transactional
    public Long toggleLike(String email, Long contentId, LikeType newLikeType) {
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    	
        Optional<Like> existingLikeOpt = likeRepository.findByUserIdAndContentId(user.getId(), contentId);

        // 사용자가 좋아요 또는 싫어요를 이미 눌렀을 때
        if (existingLikeOpt.isPresent()) {
            Like existingLike = existingLikeOpt.get();

            // 1. 같은 타입을 또 누른 경우 → 삭제 (토글 취소)
            if (existingLike.getLiketype() == newLikeType) {
                likeRepository.delete(existingLike);
                
                recommendService.deleteUserLike(user.getId(), contentId);
                
                return existingLike.getContent().getCover().getId();
            }

            // 2. 다른 타입을 누른 경우 → 기존 삭제 후 새로 생성
            likeRepository.delete(existingLike);
            
            recommendService.deleteUserLike(user.getId(), contentId);
        }

        // 3. 아무것도 없거나 다른 거 눌렀던 경우 → 새로운 Like 저장
        Like newLike = new Like();
        newLike.setUser(userRepository.getReferenceById(user.getId()));
        Content content = contentRepository.getReferenceById(contentId);
        newLike.setContent(contentRepository.getReferenceById(contentId));
        newLike.setLiketype(newLikeType);

        likeRepository.save(newLike);
        
        // newLikeType이 LIKE일 때만 추천 서버에 등록
        if (newLikeType == LikeType.LIKE) {
            recommendService.createUserLike(user.getId(), contentId);
        }
        
        return content.getCover().getId();
    }
    
    public void recommendSet(List<WritingDto> writingDto) {
    	
    }
}
