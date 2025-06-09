package cauCapstone.openCanvas.rdb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.dto.CoverDto;
import cauCapstone.openCanvas.rdb.entity.Cover;

public interface CoverRepository extends JpaRepository<Cover, Long>{

	// 좋아요 순으로 정렬하기.
	@Query("""
		    SELECT new cauCapstone.openCanvas.rdb.dto.CoverDto(c.id, c.title, c.coverImageUrl, c.time, 
		    COALESCE(ct.view, 0), COALESCE(COUNT(l), 0), c.roomType, c.roomId)
		    FROM Cover c
		    LEFT JOIN c.content ct
		    LEFT JOIN ct.likes l
		    GROUP BY c.id, c.title, c.coverImageUrl, c.time, ct.view
		    ORDER BY COUNT(l) DESC
		""")
		List<CoverDto> findAllOrderByLikeCountDesc();
    
    // 조회수 순으로 정렬하기.
	@Query("""
		    SELECT new cauCapstone.openCanvas.rdb.dto.CoverDto(c.id, c.title, c.coverImageUrl, c.time,
		    COALESCE(ct.view, 0), COALESCE(COUNT(l), 0), c.roomType, c.roomId)
		    FROM Cover c
		    LEFT JOIN c.content ct
		    LEFT JOIN ct.likes l
		    GROUP BY c.id, c.title, c.coverImageUrl, c.time, ct.view
		    ORDER BY ct.view DESC
		""")
		List<CoverDto> findAllOrderByViewDesc();
    
    
    // 모든 커버의 좋아요 수와 조회수를 세고 최신순으로 커버dto 리턴.
    @Query("""
    	    SELECT new cauCapstone.openCanvas.rdb.dto.CoverDto(c.id, c.title, c.coverImageUrl, c.time,
    	    COALESCE(ct.view, 0), COALESCE(COUNT(l), 0), c.roomType, c.roomId)
    	    FROM Cover c
    	    LEFT JOIN c.content ct
    	    LEFT JOIN ct.likes l
    	    GROUP BY c.id, c.title, c.coverImageUrl, c.time, ct.view
    	    ORDER BY c.id DESC
    	""")
    	List<CoverDto> findAllWithLikeCountByIdDesc();
    
    @Query("""
    	    SELECT new cauCapstone.openCanvas.rdb.dto.CoverDto(
    		    c.id,
    	        c.title,
    	        c.coverImageUrl,
    	        c.time,
    	        COALESCE(ct.view, 0),
    	        COALESCE(COUNT(l), 0), 
    	        c.roomType, 
    	        c.roomId
    	    )
    	    FROM Cover c
    	    LEFT JOIN c.content ct
    	    LEFT JOIN ct.likes l
    	    WHERE c.title IS NOT NULL AND LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
    	    GROUP BY c.id, c.title, c.coverImageUrl, c.time, ct.view
    	    ORDER BY c.id DESC
    	""")
    	List<CoverDto> searchByTitleKeyword(@Param("keyword") String keyword);
    
    	Optional<Cover> findByTitle(String title);
    	
    	Optional<Cover> findByRoomId(String roomId);
}
