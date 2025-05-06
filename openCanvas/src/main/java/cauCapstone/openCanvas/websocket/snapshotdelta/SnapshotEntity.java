package cauCapstone.openCanvas.websocket.snapshotdelta;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 문서방이 닫히면(문서 작성이 종료되면) rdb에 저장할 엔티티이다.
@Entity
@Table(name = "snapshot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentId;

    @Lob
    private String snapshotJson;

    private Long timestamp;
}