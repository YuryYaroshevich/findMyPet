package com.yy.petfinder.security.service;

import com.yy.petfinder.security.exception.TokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenService {
  private final String secretKey;
  private final long expirationMs;

  public TokenService(
      @Value("${token.salt}") final String salt,
      @Value("${token.expirationMs}") final long expirationMs) {
    this.secretKey = Base64.getEncoder().encodeToString(salt.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  public String createToken(final String userId) {
    long now = (new Date()).getTime();
    final Date validity = new Date(now + expirationMs);
    return Jwts.builder()
        .setSubject(userId)
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .setExpiration(validity)
        .compact();
  }

  public String getUserIdFromToken(final String token) {
    final Claims claims;
    try {
      claims = parseToken(token);
    } catch (JwtException e) {
      throw new TokenException("invalid token");
    }
    return claims.getSubject();
  }

  public boolean isValid(String token) {
    try {
      parseToken(token);
    } catch (JwtException e) {
      return false;
    }
    return true;
  }

  private Claims parseToken(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
  }
}
