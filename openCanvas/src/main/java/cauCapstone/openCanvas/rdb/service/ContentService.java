package cauCapstone.openCanvas.rdb.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.rdb.repository.LikeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {
	private ContentRepository contentRepository;
	private CoverRepository coverRepository;
	private LikeRepository likeRepository;
	
	// coverId를 받아서 Content를 리턴하는 메소드, Content가 없으면 새로 저장한다.
	// TODO: userId는 토큰 도입후 수정한다.
	public ContentDto getContent(Long coverId, Long userId) {

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
	    
	    // 조회수 +1 함.
	    conWithComments.setView(conWithComments.getView() + 1);
	    contentRepository.save(conWithComments);
	    
	    // 좋아요 갯수를 찾음.
	    int likeNum = contentRepository.countLikesById(conWithComments.getId());
	    
	    // 유저가 좋아요 또는 싫어요를 눌렀는지 확인.
	    Optional<Like> like = likeRepository.findByUserIdAndContentId(userId, conWithComments.getId());
	    LikeType likeType = like.map((a) -> a.getLiketype()).orElse(null);

	    return ContentDto.fromEntityWithLike(conWithComments, likeNum, likeType);
	}
	
	// 댓글을 삭제하는 메소드
	
	// 좋아요 또는 싫어요 버튼을 누른경우. Like객체를 가져와야함.
}
