package com.yy.petfinder.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenService {
  private static final long tokenValidityInMilliseconds = 1000 * 86400;

  private final String secretKey;

  public TokenService(@Value("${salt}") final String salt) {
    this.secretKey = Base64.getEncoder().encodeToString(salt.getBytes(StandardCharsets.UTF_8));
  }

  public String createToken(final String username) {
    long now = (new Date()).getTime();
    final Date validity = new Date(now + this.tokenValidityInMilliseconds);
    return Jwts.builder()
        .setSubject(username)
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .setExpiration(validity)
        .compact();
  }

  public String getUserFromToken(final String token) {
    final Claims claims = parseToken(token);
    return claims.getSubject();
  }

  public boolean isExpired(String token) {
    final Claims tokenClaims = parseToken(token);
    final Date expiration = tokenClaims.getExpiration();
    final Date now = new Date();
    return now.after(expiration);
  }

  private Claims parseToken(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
  }
}
