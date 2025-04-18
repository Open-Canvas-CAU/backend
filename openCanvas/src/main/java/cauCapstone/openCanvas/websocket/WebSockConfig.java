package cauCapstone.openCanvas.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

// stomp 기반의 웹소켓 설정을 한다.
@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker	// stomp를 사용하기 위한 어노테이션
public class WebSockConfig implements WebSocketMessageBrokerConfigurer {
	
    private final StompHandler stompHandler;

	// pub/sub 메시징을 구현하기 위해 메시지를 구독하는 요청의 prefix는 /sub으로 메시지를 발행하는 요청의 prefix는 /pub으로 시작하게 한다.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/sub");
        config.setApplicationDestinationPrefixes("/pub");
    }

    // 로컬일 때 stomp-websocket 엔드포인트는 ws://localhost:8080/ws-stomp가 된다.
    // 다른 서버에서 오는 요청을 허용하기 위해 setAllowdOrigins("*")를 한다.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp").setAllowedOrigins("*")
                .withSockJS();	// 웹소켓 미지원 환경에서도 연결 가능하게 한다.
    }
    
    // 누군가 웹소켓에 연결할 때 stompHandler가 가로챈다. stompHandler는 토큰 검증을 한다.
    // 웹소켓 요청할 때마다 매번 가로챈다.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}