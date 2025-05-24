package cauCapstone.openCanvas.rdb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.entity.Writing;

public interface WritingRepository extends JpaRepository<Writing, Long> {
	
    // 특정 depth에 있는 siblingIndex 1, 2가 사용중인지 알려준다.
	// 예를들어 Writing삭제시 siblingIndex가 2는 사용중인데 1은 사용하지 않는 경우가 생긴다. 
	// 이럴때 1을 사용할 수 있다는 식으로 알려주는 것이다.
	@Query("SELECT COUNT(w) FROM Writing w WHERE w.depth = :depth AND w.siblingIndex = :siblingIndex "
			+ "AND w.content.title = :title")
	int countByDepthAndSiblingIndex(@Param("depth") int depth, @Param("siblingIndex") int siblingIndex, @Param("title") String title);
    
    // 유저 이메일로 유저가 쓴 Writing을 리턴한다(유저가 썼던 글 목록 보는 용도).
	@Query("""
		    SELECT w FROM Writing w
		    WHERE w.user.email = :email
		""")
		List<Writing> findByUserName(@Param("email") String email);
	
	// 유저 이메일, 현재 글(Content)로 유저가 쓴 Writing을 리턴한다(유저가 root인지 체크함).
	@Query("""
			SELECT w FROM Writing w
			WHERE w.user.email = :email AND w.content.title = :title
			""")
	Optional<Writing> findByUserNameAndTitle(@Param("email") String email, @Param("title") String title);
	
	// depth, siblingIndex, contentId로 Writing을 검색한다.
	Optional<Writing> findByDepthAndSiblingIndexAndContentId(int depth, int siblingIndex, String title);
	
	// 글(content)의 모든 WritingDto를 가져온다. WritingDto는 depth와 siblingIndex가 작은 순으로 정렬된다.
	@Query("""
		    SELECT new cauCapstone.openCanvas.rdb.dto.WritingDto(
		        w.depth, w.siblingIndex, w.time, u.email)
		    FROM Writing w
		    JOIN w.user u
		    JOIN w.content c
		    LEFT JOIN w.parent p
		    WHERE w.content.title = :title
		    ORDER BY w.depth ASC, w.siblingIndex ASC
		""")
		List<WritingDto> findAllDtosByContentTitle(@Param("title") String title);
	
	// 부모 엔티티로 자식을 찾기 위해 이 메소드를 씀. 
	List<Writing> findAllByParent(Writing parent);
}
