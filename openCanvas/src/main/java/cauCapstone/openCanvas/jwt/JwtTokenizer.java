package cauCapstone.openCanvas.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

// 자체 서비스 access 토큰과 refresh 토큰을 생성한다.
// generateAccessToken 메소드의 claim, subject.. 등등에 넣을 것은 수정이 가능하다.
// TODO: successHandler에 JwtTokenizer 관련 로직을 넣어야한다.
@Component
public class JwtTokenizer {
	
	@Autowired
	RefreshTokenRepository refreshTokenRepository;
	
	// application.yml에 저장된 secretkey를 가져옴.
	@Value("${spring.jwt.secret-key}")
	private String secretKey;
	
	// Secret key를 byte화 한다.
	// base64EncodedSecretKey가 application.yml에 저장된 secretKey를 변환한것이라는걸 기억하기.
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
				.setSubject(subject) // 이메일/유저네임/유저아이디 중에 하나가 들어간다고 한다. 이메일을 넣는게 좋을듯. 수정가능함. 
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
	
	// generateRefreshToken 메소드를 쓰고, 리프레시 토큰을 Redis에 저장함.
	public void generateAndStoreRefreshToken(String subject,
			Date expiration) {
		
		String refreshToken = generateRefreshToken(subject, expiration);
		
		RefreshToken redisToken = new RefreshToken(subject, refreshToken);
		refreshTokenRepository.save(redisToken);
	}
	
	// byte화된 secretKey를 파라미터로 받고 Key 클래스의 secretKey를 리턴한다.
	private Key getKeyFromBase64EncodedKey(String base64EncodedSecretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	
	// 서명(signature)을 통해 검증한다.
	// 검증은 예외를 던지는 것으로 성공과 실패를 나눈다.
	// claim은 유저정보, 발행일, 만료기간(위에 참조)가 들어가고 subject가 유저정보(id를 넣을수도 있고 나중에 정하기)
	public Claims verifySignature(String jws, String base64EncodedSecretKey) {
		
		try {
			Key key = getKeyFromBase64EncodedKey(base64EncodedSecretKey);
			// TODO: 이 Claims 정보를 담아야할 수도 있다.
			Claims claims = Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(jws)
					.getBody();
			
			// 이런식으로 claims를 꺼낼 수 있다.
			String subject = claims.getSubject();
			
			return claims;
			
		} catch (JwtException e) {
	        throw new RuntimeException("유효하지 않은 JWT 토큰입니다.");
	    }
		
	}
}

