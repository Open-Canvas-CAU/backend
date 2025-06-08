package cauCapstone.openCanvas.rdb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
    // 좋아요를 누른 Cover만 반환
    @Query("""
        SELECT l.content.cover
        FROM Like l 
        WHERE l.user.id = :userId
    """)
    List<Cover> findCoversLikedByUserId(@Param("userId") Long userId);
	
	// TODO: 유저의 색을 정하는 메소드 구현하기(그냥 색 세팅만 해주면됨).
	Optional<User> findByEmail(String email);
	
	List<User> findAllByRole(Role role);
	
}
