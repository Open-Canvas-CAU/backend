package cauCapstone.openCanvas.rdb.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.ContentGenre;
import cauCapstone.openCanvas.rdb.entity.Genre;
import cauCapstone.openCanvas.rdb.entity.Writing;
import cauCapstone.openCanvas.rdb.repository.ContentGenreRepository;
import cauCapstone.openCanvas.rdb.repository.ContentRepository;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.rdb.repository.GenreRepository;
import cauCapstone.openCanvas.rdb.repository.WritingRepository;
import cauCapstone.openCanvas.recommend.service.RecommendService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

// 태그추가하기
@Service
@RequiredArgsConstructor
public class GenreService {
	private final GenreRepository genreRepository;
	private final ContentGenreRepository contentGenreRepository;
    private final ContentRepository contentRepository;
    private final CoverRepository coverRepository;
    private final WritingRepository writingRepository;
    private final RecommendService recommendService;

    @Transactional
	public List<String> setGenre(String email, List<String> genres, Long contentId) {
		// 처음글 쓴 유저인지 체크
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found"));
        
        Writing firstW = writingRepository
                .findByDepthAndSiblingIndexAndContent_Title(1, 1, content.getTitle())
                .orElseThrow(() ->  new IllegalArgumentException("존재하지 않는 writing입니다."));
        
        if(! (firstW.getUser().getEmail().equals(email))) {
        	throw new IllegalArgumentException("유저가 루트유저가 아닙니다.");
        }
		
        for (String genreName : genres) {
            // 이미 있는 장르인지 확인
            Genre genre = genreRepository.findByName(genreName)
                .orElseGet(() -> {
                    Genre newGenre = new Genre();
                    newGenre.setName(genreName);
                    Genre g = genreRepository.save(newGenre);
                    
                    // 새로운장르 태그추가
                    recommendService.createTag(g.getId());
                    
                    return g;
                });

            ContentGenre contentGenre = new ContentGenre();
            contentGenre.setContent(content);
            contentGenre.setGenre(genre);
            contentGenreRepository.save(contentGenre);
        }
 
        return genres;
		
	}
	
    // 모든 장르 목록 리턴
	public List<String> getAllGenres(){
        return genreRepository.findAll()
                .stream()
                .map(Genre::getName)
                .collect(Collectors.toList());
	}
	
	public List<String> getGenre (Long contentId){
		// content -> contentGenre -> Genre 로 이름알아내고 리턴
	    return contentGenreRepository.findGenreNamesByContentId(contentId);
	}
	
	public List<CoverDto> searchGenre(String genreName){
		// 장르 이름으로 장르 엔티티 -> ContentGenre엔티티 -> content리스트 -> coverdto 리스트리턴
	    return coverRepository.findCoverDtosByGenreName(genreName);
		
	}
}
