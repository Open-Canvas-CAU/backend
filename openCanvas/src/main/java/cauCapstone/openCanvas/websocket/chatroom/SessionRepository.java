package cauCapstone.openCanvas.websocket.chatroom;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// 웹소켓에 연결할때(CONNECT) sessionId와 subject를 저장한다.
// 세션 ID를 키로, 유저정보를 밸류로 redis에 저장해둔다.
// 웹소켓에 연결하는 유저마다 고유의 세션id를 가지고, 웹소켓 자체에서 유저별 세션 ID를 얻을 수 있다.
// 얻은 세션 ID로 유저정보를 얻을 수 있다.
@Service
@RequiredArgsConstructor
public class SessionRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SESSION_PREFIX = "ws:session:";

    // 세션 ID에 해당하는 유저의 유저정보(subject)를 저장
    // ttl은 하루로 설정해둠(하루동안 글을 연달아 쓰는 사람이 없을거라고 생각)
    public void registerSession(String sessionId, String subject) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, subject, Duration.ofDays(1));
    }

    // 세션 ID로 유저정보 조회
    public String getSubjectBySessionId(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return redisTemplate.opsForValue().get(key);
    }

    // 세션 종료 시 삭제
    // DISCONNECT할때 호출한다.
    public void removeSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
     
}
