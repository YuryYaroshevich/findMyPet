package com.yy.petfinder.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TokenProvider {

  private static final String SALT_KEY =
      "JpxM4e858rc673syopdZnMFb*ExeqJtUc0HJ_iOxu~jiSYu+yPdPw93OBBjF";
  private static final int TOKEN_VALIDITY = 86400; // Value in second

  private static final String AUTHORITIES_KEY = "auth";

  private final Base64.Encoder encoder = Base64.getEncoder();

  private String secretKey;

  private long tokenValidityInMilliseconds;

  @PostConstruct
  public void init() {
    this.secretKey = encoder.encodeToString(SALT_KEY.getBytes(StandardCharsets.UTF_8));

    this.tokenValidityInMilliseconds = 1000 * TOKEN_VALIDITY;
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

  public Authentication getAuthentication(String token) {
    if (StringUtils.isEmpty(token) || !isValidToken(token)) {
      throw new BadCredentialsException("Invalid token");
    }
    Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

    User principal = new User(claims.getSubject(), "", List.of());

    return new UsernamePasswordAuthenticationToken(principal, token);
  }

  public boolean isValidToken(String authToken) {
    try {
      Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }
}
