package cauCapstone.openCanvas.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

// 자체 서비스 access 토큰과 refresh 토큰을 생성한다.
// generateAccessToken 메소드의 claim, subject.. 등등에 넣을 것은 수정이 가능하다.
@Component
public class JwtTokenizer {
	
	// application.yml에 저장된 secretkey를 가져옴.
	@Value("${spring.jwt.secret-key}")
	private String secretKey;
	
	// Secret key를 byte화 한다.
	public String encodeBase64SecretKey() {
		return Encoders.BASE64.encode(secretKey.getBytes(StandardCharsets.UTF_8));
	}
	
	// 어느 서버에서든 서버에서 access 토큰을 발급한다.
	public String generateAccessToken(Map<String, Object> claims,
			  String subject,
			  Date expiration) {

		Key key = getKeyFromBase64EncodedKey(encodeBase64SecretKey());

		return Jwts.builder()
				.setClaims(claims) // 유저정보가 담긴다 수정가능함.
				.setSubject(subject) // 유저정보가 담긴다 수정가능함.
				.setIssuedAt(Calendar.getInstance().getTime()) // 언제 발행했는지
				.setExpiration(expiration) // 만료기간 설정
				.signWith(key) // 서명에 관련된 것. 검증에 필요하다.
				.compact();
	}
	
	// refresh 토큰을 발급하는데,유저정보는 access 토큰에 담겨서 담을 필요가 없다.
	public String generateRefreshToken(String subject,
			   Date expiration) {

		Key key = getKeyFromBase64EncodedKey(encodeBase64SecretKey());

		return Jwts.builder()
				.setSubject(subject)
				.setIssuedAt(Calendar.getInstance().getTime())
				.setExpiration(expiration)
				.signWith(key)
				.compact();
	}
	
	// byte화된 secretKey를 파라미터로 받고 Key 클래스의 secretKey를 리턴한다.
	private Key getKeyFromBase64EncodedKey(String base64EncodedSecretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	
	// 서명(signature)을 통해 검증한다.
	// 서명 검증을 통과하면 claims를 얻을 수 있다.
	public void verifySignature(String jws, String base64EncodedSecretKey) {
		Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);
		
		Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJwt(jws);
	}
}

