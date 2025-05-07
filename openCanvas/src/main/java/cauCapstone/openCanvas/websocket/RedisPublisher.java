package cauCapstone.openCanvas.websocket;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

// editPublish와 다르게 절차검증이 필요없는 시스템상의 메세지를 다룬다.
// 순환구조를 방지하기 위한 클래스이다.
@RequiredArgsConstructor
@Service
public class RedisPublisher {
	private final RedisTemplate<String, Object> redisTemplate;
	
	public void publish(ChannelTopic topic, ChatMessage message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
	}
}
