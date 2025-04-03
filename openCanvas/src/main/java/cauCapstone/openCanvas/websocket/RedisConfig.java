package cauCapstone.openCanvas.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

// pub/sub 기능과 Redis를 사용하기 위한 설정 클래스이다.

@Configuration
public class RedisConfig {
	
	// "chatroom"이라는 단일 채널(토픽)을 사용한다.
	// 채팅방마다 토픽을 만드는게("chatroom" + roomId) 나을 수도 있다.
	@Bean
	public ChannelTopic channelTopic() {
	    return new ChannelTopic("chatroom");
	}


	// pub/sub 기능을 사용할 때, 구독한 채널의 메세지를 수신하기 위해 RedisMessageListener를 사용한다.
	// RedisConnectionFactory를 통해 Redis와 연결한다.
	// linstenerAdapter와 channelTopic을 단일화한다.
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory,
                                                              MessageListenerAdapter listenerAdapter,
                                                              ChannelTopic channelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, channelTopic);
        return container;
    }
    
    // 수신한 메시지를 처리할 객체를 설정하는 메소드이다.
    // RedisSubscriber 클래스의 sendMessage 메소드를 실행한다.
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }


	// Redis에 데이터를 읽고, 쓰기위해 사용한다.
	// Redis의 키와 밸류를 직렬화 한다.
	@Bean
	@Primary
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		return redisTemplate;
	}
}