package cauCapstone.openCanvas.websocket;


import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 핸들러를 통해 여러 클라이언트의 메세지들을 서버가 받아서 처리한다. 
@Slf4j
@RequiredArgsConstructor
@Component
public class WebSockChatHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final ChatService chatService;

 @Override
 protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
	 // 클라이언트는 payload에 메시지를 담는다.
     String payload = message.getPayload();
     log.info("payload {}", payload);
     // payload에 담긴 메시지를 ChatMessage 객체로 바꾼다.
     ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
     // chatMessage에 담긴 roomId로 보내야할 채팅방을 찾는다.
     ChatRoom room = chatService.findRoomById(chatMessage.getRoomId());
     // 모든 세션에 메시지의 타입에따른 메시지를 발송한다.
     room.handleActions(session, chatMessage, chatService);
 }
}