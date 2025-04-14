package cauCapstone.openCanvas.jwt;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 86400) //RedisHash에 "refreshToken" : {id} 형태로 저장된다. TTL(유효시간)은 24시간이다.
public class RefreshToken {
	
	// 이게 JwtTokenizer에는 subject로 쓰임.
	@Id
	private String email;
	
	private String token;
}
