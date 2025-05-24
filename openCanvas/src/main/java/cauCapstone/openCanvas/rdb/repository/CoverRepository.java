package cauCapstone.openCanvas.rdb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.entity.Cover;

public interface CoverRepository extends JpaRepository<Cover, Long>{

	// 좋아요 순으로 정렬하기.
	@Query("""
		    SELECT new cauCapstone.openCanvas.rdb.dto.CoverDto(c.title, c.coverImageUrl, c.time, ct.view, COUNT(l))
		    FROM Cover c
		    LEFT JOIN c.content ct
		    LEFT JOIN ct.likes l
		    GROUP BY c.id, c.title, c.coverImageUrl, c.time, ct.view
		    ORDER BY COUNT(l) DESC
		""")
		List<CoverDto> findAllOrderByLikeCountDesc();
    
    // 조회수 순으로 정렬하기.
	@Query("""
		    SELECT new cauCapstone.openCanvas.rdb.dto.CoverDto(c.title, c.coverImageUrl, c.time, ct.view, COUNT(l))
		    FROM Cover c
		    LEFT JOIN c.content ct
		    LEFT JOIN ct.likes l
		    GROUP BY c.id, c.title, c.coverImageUrl, c.time, ct.view
		    ORDER BY ct.view DESC
		""")
		List<CoverDto> findAllOrderByViewDesc();
    
    
    // 모든 커버의 좋아요 수와 조회수를 세고 최신순으로 커버dto 리턴.
    @Query("""
    	    SELECT new cauCapstone.openCanvas.rdb.dto.CoverDto(c.title, c.coverImageUrl, c.time, ct.view, COUNT(l))
    	    FROM Cover c
    	    LEFT JOIN c.content ct
    	    LEFT JOIN ct.likes l
    	    GROUP BY c.id, c.title, c.coverImageUrl, c.time, ct.view
    	    ORDER BY c.id DESC
    	""")
    	List<CoverDto> findAllWithLikeCountByIdDesc();
}
