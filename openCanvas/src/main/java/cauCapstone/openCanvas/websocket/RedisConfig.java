package cauCapstone.openCanvas.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import cauCapstone.openCanvas.websocket.chatmessage.RedisSubscriber;
import cauCapstone.openCanvas.websocket.chatroom.RemoveChatRoomService;
import cauCapstone.openCanvas.websocket.chatroom.RemoveEditorService;
import cauCapstone.openCanvas.websocket.chatroom.SubscribeRepository;
import cauCapstone.openCanvas.websocket.snapshot.SnapshotService;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

// pub/sub 기능과 Redis를 사용하기 위한 설정 클래스이다.

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String config_host;

    @Value("${spring.data.redis.port}")
    private int config_port;

    @Value("${spring.data.redis.password}")
    private String config_password;
	// TODO: localhost(컨테이너)는 테스트용이다.
    // RedisConnectionFactory 설정 (Docker Redis 사용 시 호스트 설정)
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(config_host);  // Docker 환경에서 Redis에 연결하기 위한 설정
        config.setPort(config_port);  // Redis 기본 포트
        // config.setPassword(config_password);
        return new LettuceConnectionFactory(config);  // LettuceConnectionFactory 사용
    }
	
	// pub/sub 기능을 사용할 때, 구독한 채널의 메세지를 수신하기 위해 RedisMessageListener를 사용한다.
	// RedisConnectionFactory를 통해 Redis와 연결한다.
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory,
                                                              MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
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
	
	// DISCONNECT시에 3분 TTL 키가 만료됬는지 확인하기 위한 리스너이다.
	@Bean
	public RedisKeyExpirationListener redisKeyExpirationListener(
	        RedisMessageListenerContainer container,
            SubscribeRepository subscribeRepository,
            RemoveEditorService removeEditorService,
            SnapshotService snapshotService,
            RemoveChatRoomService removeChatRoomService) {
	    return new RedisKeyExpirationListener(container, subscribeRepository,removeEditorService, 
	    		snapshotService,removeChatRoomService);
	}
}