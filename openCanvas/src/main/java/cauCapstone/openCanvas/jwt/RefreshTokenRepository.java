package cauCapstone.openCanvas.jwt;

import org.springframework.data.repository.CrudRepository;

// <RefreshToken, String>은 RefreshToken 엔티티 클래스, String 클래스인 Id
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
	
}
