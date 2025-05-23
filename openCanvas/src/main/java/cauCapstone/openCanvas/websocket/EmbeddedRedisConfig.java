package cauCapstone.openCanvas.websocket;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import redis.embedded.RedisServer;

/*
// 로컬 환경일 경우 embedded redis 서버가 실행된다.

// TODO: TEST embedded reids 끄기
//@Profile("local")
@Configuration
public class EmbeddedRedisConfig {
	
	// spring.redis.port 값을 가져옴.
	@Value("${spring.redis.port}")
	private int redisPort;
	
	private RedisServer redisServer;


	// redisServer 시작
	@PostConstruct
	public void redisServer() {
		redisServer = new RedisServer(redisPort);
		redisServer.start();
	}
	
	// redisServer 종료
	@PreDestroy
	public void stopRedis() {
		if(redisServer != null) {
			redisServer.stop();
		}
	}
	
}
*/
