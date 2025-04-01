package cauCapstone.openCanvas.websocket;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

// 채팅방에서 메시지를 작성하면 해당 메시지를 topic에 발행한다.
// 메시지를 발행하면 대기하고 있던 redis 구독 서비스가 메시지를 처리한다.
@RequiredArgsConstructor
@Service
public class RedisPublisher {
 private final RedisTemplate<String, Object> redisTemplate;

 public void publish(ChannelTopic topic, ChatMessage message) {
     redisTemplate.convertAndSend(topic.getTopic(), message);
 }
}