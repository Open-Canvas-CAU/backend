package cauCapstone.openCanvas.rdb.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.ContentDto;
import cauCapstone.openCanvas.rdb.dto.ReportDto;
import cauCapstone.openCanvas.rdb.dto.UserDto;
import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.entity.Writing;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import cauCapstone.openCanvas.rdb.repository.WritingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WritingService {

    private final WritingRepository writingRepository;
    private final UserRepository userRepository;
    private final ContentRepository contentRepository;

    // 현재 depth로 글을 써도 되는지 체크함.
    // 체크하고 문서방 만들기.
    @Transactional
    public int checkWriting(int parentDepth, int parentSiblingIndex, String title) {
        int newDepth = parentDepth + 1;

        boolean is1Used = writingRepository.countByDepthAndSiblingIndex(newDepth, 1, title) > 0;
        boolean is2Used = writingRepository.countByDepthAndSiblingIndex(newDepth, 2, title) > 0;

        if (is1Used && is2Used) {
            throw new IllegalStateException("해당 이어쓰기 단계에서는 이미 2개의 글이 작성되었습니다.");
        }

        // 인덱스 자리는 있는데 1이 사용중이면 2를, 그게 아니라면 1을 할당한다.
        int nextSiblingIndex = is1Used ? 2 : 1;
        
        return nextSiblingIndex;
    }
    
    // Wrting 리프노드에서 부모노드들을 전부 가져오는 역할: 리프노드는 실제 저장이 되있어야한다.
    @Transactional
    public List<WritingDto> getWritingWithParents(WritingDto writingDto) {
    	List<WritingDto> allWritingDtos = new ArrayList<>();
    	
    	int curDepth = writingDto.getDepth();
    	int curSiblingIndex = writingDto.getSiblingIndex();
    	String title = writingDto.getTitle();
    	
    	while(writingDto.getDepth() >0) {
            Writing current = writingRepository
                    .findByDepthAndSiblingIndexAndContent_Title(curDepth, curSiblingIndex, title)
                    .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
                        
                allWritingDtos.add(WritingDto.fromEntity(current));
                
                curDepth = curDepth - 1;
                curSiblingIndex = (current.getParent() != null) ? current.getParent().getSiblingIndex() : -1;
    	}
    	
    	return allWritingDtos;
    }
    
    // ! 유저 필요한지 체크
    // depth랑 siblingindex, title 받아서 저장하기: 검증절차는 거쳤다고 판단하고 검증 안하고 저장함.
    public Writing saveWriting(WritingDto writingDto) {
        // 1. User 조회
        User user = userRepository.findByEmail(writingDto.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. Content 조회
        Content content = contentRepository.findByTitle(writingDto.getTitle())
            .orElseThrow(() -> new IllegalArgumentException("콘텐츠를 찾을 수 없습니다."));

        // 3. 부모 Writing 조회 (루트가 아니면)
        Writing parent = null;
        if (writingDto.getDepth() > 1) {
            parent = writingRepository
                .findByDepthAndSiblingIndexAndContent_Title(writingDto.getDepth()-1, writingDto.getParentSiblingIndex(), 
                		content.getTitle())
                .orElseThrow(() -> new IllegalArgumentException("부모 글이 존재하지 않습니다."));
        }

        // 4. Writing 저장
        Writing writing = writingDto.toEntity(user, content, parent);

        return writingRepository.save(writing);
    }
    
    // !유저 필요
    // 루트 사용자(맨처음 글을 쓴 사람) 확인하고 삭제하기: 실제 삭제가 아니라 내용만 빈 내용으로 바꿈(추후에 내용을 변경할 수도 있겠다).
    // 현재유저의 dto와 지우고싶은 writingDto를 받음.
    // TODO: 글을 쓴 사람은 변경 하지 않았는데, 안보이게 하는 조치가 필요함.
    @Transactional
    public void deleteByRoot(String email, WritingDto writingDto) {
    	Writing userWriting = writingRepository.findByUserNameAndTitle(email, writingDto.getTitle())
    	            .orElseThrow(() -> new IllegalArgumentException("유저가 쓴 writing을 찾을 수 없습니다."));
    	
    	if(userWriting.getDepth() == 1) {
            Writing delete = writingRepository
                    .findByDepthAndSiblingIndexAndContent_Title(writingDto.getDepth(), writingDto.getSiblingIndex(), 
                    		writingDto.getTitle())
                    .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
            
            delete.setBody("");
            writingRepository.save(delete);
    	}else {
    		throw new IllegalArgumentException("유저가 루트가 아닙니다.");
    	}
    }
    
    // 글(content)의 모든 버전 가져오기
    // TODO: contentTitle만으로 충분하긴한데 확인해보기.
    public List<WritingDto> getSimpleWriting(ContentDto contentDto){
    	 return writingRepository.findAllDtosByContentTitle(contentDto.getTitle());
    }
    
    // ! 유저필요
	// ADMIN 유저가 글을 지울 때 쓰는 메소드.
    // ADMIN 유저의 UserDto와 삭제할 글의 WritingDto를 받음.
    // 삭제처리(공백처리)된 글의 글쓴이는 admin으로 임시지정함.
    @Transactional
    public void deleteByAdmin(String email, WritingDto writingDto) {
    	User user = userRepository.findByEmail(email)
    			.orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    	
    	if(user.getRole() == Role.ADMIN) {
            Writing delete = writingRepository
                    .findByDepthAndSiblingIndexAndContent_Title(writingDto.getDepth(), writingDto.getSiblingIndex(), 
                    		writingDto.getTitle())
                    .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
            
            delete.setBody("");
            delete.setUser(user);
            writingRepository.save(delete);
    	}else {
    		throw new IllegalArgumentException("유저가 어드민이 아닙니다.");
    	}
    }
    
    /*
    // 루트 사용자를 확인하고 삭제할 글을 다른곳에 이어붙인다.
    // 삭제할 dto를 매개변수로 받아야한다.
    public boolean reattachWriting(WritingDto writingDto, UserDto userDto, int newParentDepth, int newParentSiblingIndex) {
    	Writing userWriting = writingRepository.findByUserNameAndTitle(userDto.getEmail(), writingDto.getTitle())
	            .orElseThrow(() -> new IllegalArgumentException("유저가 쓴 writing을 찾을 수 없습니다."));
    	
    	if(userWriting.getDepth() == 1) {
        	Writing willDelete = writingRepository.findByDepthAndSiblingIndexAndContentId(writingDto.getDepth(), 
        			writingDto.getSiblingIndex(), writingDto.getTitle())
    	            .orElseThrow(() -> new IllegalArgumentException("삭제할 writing을 찾을 수 없습니다."));
        	
        	List<Writing> childs = writingRepository.findAllByParent(willDelete);
        	
        	if(childs.size() > 0) {
        		if(newParentDepth == willDelete.getDepth()) {
        	    	Writing newParent = writingRepository.findByDepthAndSiblingIndexAndContentId(
        	    			newParentDepth, newParentSiblingIndex, writingDto.getTitle())
        		            .orElseThrow(() -> new IllegalArgumentException("새 부모 writing을 찾을 수 없습니다."));
        	    	
        	    	for(Writing c: childs) {
        	    		c.setParent(newParent);
        	    	}
        		}else if(newParentDepth == willDelete.getDepth() - 1){
        			
        		}else {
        			throw new IllegalArgumentException("잘못된 parentDepth 설정입니다.");
        		}
        	}
        	
    	}else {
    		throw new IllegalArgumentException("유저가 루트가 아닙니다.");
    	}
 	
    }
    */
}
