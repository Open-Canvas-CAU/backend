package cauCapstone.openCanvas.rdb.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoverService {
	private final CoverRepository coverRepository;
	private final UserRepository userRepository;
	
	// 커버를 생성하는 메소드
	public Cover makeCover(CoverDto coverDto) {
		Cover cover = coverDto.toEntity();
		return coverRepository.save(cover);
	}
	
	// 모든 커버를 최신순으로 불러오는 메소드(조회수와 좋아요 포함)
	public List<CoverDto> showAllCovers(){
		return coverRepository.findAllWithLikeCountByIdDesc();
	}
	
	// 모든 커버를 좋아요순으로 불러오는 메소드(조회수와 좋아요 포함)
	public List<CoverDto> showAllCoversWithLikes(){
		return coverRepository.findAllOrderByLikeCountDesc();
	}
	
	// 모든 커버를 조회순으로 불러오는 메소드(조회수와 좋아요 포함)
	public List<CoverDto> showAllCoversWithViews(){
		return coverRepository.findAllOrderByViewDesc();
	}
	
	// ! 유저필요
	// 커버를 삭제하는 메소드, ADMIN이 삭제할 수 있게함.
    public void deleteCover(Long id, String email) {
        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    	
        if(user.getRole() == Role.ADMIN) {
            Cover cover = coverRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Cover입니다."));

                coverRepository.delete(cover);
        }else {
        	throw new IllegalArgumentException("유저가 어드민이 아닙니다.");
        }
    }
    
    public List<CoverDto> searchCoversByTitle(String keyword) {
       return coverRepository.searchByTitleKeyword(keyword);
    }
}
