package com.yy.petfinder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yy.petfinder.security.service.TokenService;
import org.junit.jupiter.api.Test;

public class TokenServiceTest {
  private static final String SALT = "fd67gdf58765g";

  @Test
  public void testThatTokenExpires() throws InterruptedException {
    // given
    final TokenService tokenService = new TokenService(SALT, 1000);

    // when
    final String token = tokenService.createToken("someUserId");
    Thread.sleep(2000);

    // then
    assertFalse(tokenService.isValid(token));
  }

  @Test
  public void testThatTokenCanBeNotExpired() throws InterruptedException {
    // given
    final TokenService tokenService = new TokenService(SALT, 10_000);

    // when
    final String token = tokenService.createToken("someUserId");
    Thread.sleep(2000);

    // then
    assertTrue(tokenService.isValid(token));
  }

  @Test
  public void isValidRejectsTokenSignedByWrongSalt() throws InterruptedException {
    // given
    final TokenService tokenServiceWithDifferentSalt = new TokenService("someOtherSalt", 10_000);

    // when
    final String token = tokenServiceWithDifferentSalt.createToken("someUserId");

    // then
    final TokenService tokenService = new TokenService(SALT, 10_000);
    assertFalse(tokenService.isValid(token));
  }

  @Test
  public void getUserIdFromTokenReturnsValidUserId() throws InterruptedException {
    // given
    final TokenService tokenService = new TokenService(SALT, 10_000);
    final String userId = "someUserId";

    // when
    final String token = tokenService.createToken(userId);

    // then
    assertEquals(userId, tokenService.getUserIdFromToken(token));
  }
}
