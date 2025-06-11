package cauCapstone.openCanvas.rdb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.entity.ContentGenre;

public interface ContentGenreRepository extends JpaRepository<ContentGenre, Long>{
	@Query("""
		    SELECT g.name
		    FROM ContentGenre cg
		    JOIN cg.genre g
		    WHERE cg.content.id = :contentId
		""")
		List<String> findGenreNamesByContentId(@Param("contentId") Long contentId);
}
