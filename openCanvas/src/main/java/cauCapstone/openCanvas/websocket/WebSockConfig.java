package cauCapstone.openCanvas.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.RequiredArgsConstructor;

// handler를 이용해서 websocket을 활성화 하기위한 config 파일
// 클라이언트는 ws://localhost:8080/ws/chat으로 접속하면 웹소켓 통신을 할 수 있다.
@RequiredArgsConstructor
@Configuration
@EnableWebSocket	// 웹소켓을 활성화 한다.
public class WebSockConfig implements WebSocketConfigurer {
    private final WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    	// websocket 접속 엔드포인트는 "/ws/chat"이고, cors: setAllowedOrigins("*")을 통해서 도메인이 다른 서버에서도 접속 가능하다.
        registry.addHandler(webSocketHandler, "/ws/chat").setAllowedOrigins("*");
    }
}
