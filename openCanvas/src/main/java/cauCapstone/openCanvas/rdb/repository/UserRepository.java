package cauCapstone.openCanvas.rdb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	// 좋아요를 누른 글을 가져오기
	@Query("""
	    SELECT l.content
	    FROM Like l 
	    WHERE l.user.id = :userId
	""")
	List<Content> findContentWithLikeByUserId(@Param("userId") Long id);
	
	//TODO: writing 과의 관계도 만들어야함.
}
