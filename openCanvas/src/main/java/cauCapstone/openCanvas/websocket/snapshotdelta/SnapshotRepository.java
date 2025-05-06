package cauCapstone.openCanvas.websocket.snapshotdelta;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// findTopByDocumentIdOrderByTimestampDesc(String roomId) 이 메소드를 쓰기보단 스냅샷 최신 것 1개만 RDB에 저장한다.
public interface SnapshotRepository extends JpaRepository<SnapshotEntity, Long> {
    Optional<SnapshotEntity> findTopByDocumentIdOrderByTimestampDesc(String roomId);
}