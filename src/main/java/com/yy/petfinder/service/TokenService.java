package com.yy.petfinder.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class TokenService {
  private static final long tokenValidityInMilliseconds = 1000 * 86400;

  private String secretKey;

  public TokenService(@Value("${salt}") final String salt) {
    this.secretKey = Base64.getEncoder().encodeToString(salt.getBytes(StandardCharsets.UTF_8));
  }

  public String createToken(Authentication authentication) {
    long now = (new Date()).getTime();
    Date validity = new Date(now + this.tokenValidityInMilliseconds);
    return Jwts.builder()
        .setSubject(authentication.getName())
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .setExpiration(validity)
        .compact();
  }

  public String getUserFromToken(final String token) {
    final Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    return claims.getSubject();
  }
}
