package cauCapstone.openCanvas.websocket.snapshot;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.entity.Writing;
import cauCapstone.openCanvas.rdb.repository.WritingRepository;
import cauCapstone.openCanvas.rdb.service.WritingService;
import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRedisEntity;
import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRepository;
import lombok.RequiredArgsConstructor;

// ChatRoomRepository, SnapshotRepository 가져오고 WritingService에 위임해서 WritingRepository에 저장한다.
// 현재 루트의 parentSiblingIndex를 -1로 저장한 것을 알고 있어야한다.
@Service
@RequiredArgsConstructor
public class SnapshotService {
    private final SnapshotRepository snapshotRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final WritingService writingService;
    
    public void saveSnapshotToDB(String roomId) {
        // 1. 문서방 정보 가져오기 (제목, 버전 등)
        ChatRoomRedisEntity room = chatRoomRepository.findRoomById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("존재하지 않는 문서방입니다: " + roomId);
        }
        
        List<Integer> version = getIntVersion(room.getVersion());
        
        int parentSiblingIndex = (version.size() > 2) ? version.get(2) : -1;

        // 2. 스냅샷 가져오기
        SnapshotEntity snapshot = snapshotRepository.getSnapshot(roomId);
        if (snapshot == null) {
            throw new IllegalStateException("스냅샷이 존재하지 않습니다: " + roomId);
        }
        
        LocalDateTime time = Instant.ofEpochMilli(snapshot.getTime())
        	    .atZone(ZoneId.systemDefault())
        	    .toLocalDateTime();

        // 4. WritingDto 생성
        WritingDto writingDto = new WritingDto(version.get(0), version.get(1), parentSiblingIndex, 
        		snapshot.getBody(), time, room.getSubject(), room.getName());

        // 5. Writing 저장 로직 위임
        writingService.saveWriting(writingDto);

        // 6. 스냅샷 삭제 (선택)
        snapshotRepository.deleteSnapshot(roomId);
    }
    
    // String으로 되있던 버전을 List<Integer>로 리턴함.
    public List<Integer> getIntVersion(String version){
        if (version == null || version.isEmpty()) {
            return List.of(); // 또는 예외 던지기
        }
        
        return List.of(version.split("\\.")).stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
