package cauCapstone.openCanvas.recommend.service;

import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {
    @Value("${spring.data.recommend.host}")
    private String recommendHost;

    @Value("${spring.data.recommend.port}")
    private String recommendPort;

    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;

    private String getBaseUrl() {
        return "http://" + recommendHost + ":" + recommendPort;
    }

    // 유저
    public void createUser(Long userId) {
        String url = getBaseUrl() + "/user/" + userId;
        restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
    }

    public void deleteUser(Long userId) {
        String url = getBaseUrl() + "/user/" + userId;
        restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
    }

    // 태그
    public void createTag(Long tagId) {
        String url = getBaseUrl() + "/tag/" + tagId;
        restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
    }

    public void deleteTag(Long tagId) {
        String url = getBaseUrl() + "/tag/" + tagId;
        restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
    }

    // 아이템
    public void createItem(Map<String, Object> itemRequest) {
        String url = getBaseUrl() + "/item";
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(itemRequest);
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public void deleteItem(Long itemId) {
        String url = getBaseUrl() + "/item/" + itemId;
        restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
    }

    // 유저 뷰
    public void createUserView(Long userId, Long itemId) {
        String url = getBaseUrl() + "/user/" + userId + "/view/" + itemId;
        restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
    }

    public void deleteUserView(Long userId, Long itemId) {
        String url = getBaseUrl() + "/user/" + userId + "/view/" + itemId;
        restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
    }

    // 유저 좋아요
    public void createUserLike(Long userId, Long itemId) {
        String url = getBaseUrl() + "/user/" + userId + "/like/" + itemId;
        restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
    }

    public void deleteUserLike(Long userId, Long itemId) {
        String url = getBaseUrl() + "/user/" + userId + "/like/" + itemId;
        restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
    }

    // 아이템 기반 추천
    public List<Integer> recommendByItem(Long itemId) {
        return recommendByItem(itemId, 5); // 기본값 5 사용
    }
    public List<Integer> recommendByItem(Long itemId, Integer topN) {
        String url = getBaseUrl() + "/recommend_item/" + itemId + "?top_n=" + topN;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return (List<Integer>) response.getBody().get("items");
    }

    // 유저 기반 추천
    public List<Integer> recommendByUser(Long userId) {
        return recommendByUser(userId, 5); // 기본값 5 사용
    }

    public List<Integer> recommendByUser(Long userId, Integer topN) {
        String url = getBaseUrl() + "/recommend_user/" + userId + "?top_n=" + topN;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        return (List<Integer>) response.getBody().get("items");
    }

    public Long getUserIdFromEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return user.getId();
    }
}
