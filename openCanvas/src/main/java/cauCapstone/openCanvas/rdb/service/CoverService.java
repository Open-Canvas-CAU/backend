package cauCapstone.openCanvas.rdb.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoverService {
	private CoverRepository coverRepository;
	
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
	
	// 커버를 삭제하는 메소드
	// TODO: 책임을 누가지어야할지(만든 유저)에 따라 메소드를 바꿔야함.
    public void deleteCover(Long id) {
        Cover cover = coverRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Cover입니다."));

        coverRepository.delete(cover);
    }
}
