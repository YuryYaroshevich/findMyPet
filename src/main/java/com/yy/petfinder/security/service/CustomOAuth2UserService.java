package com.yy.petfinder.security.service;

import static com.yy.petfinder.model.User.PASSWORD_PLACEHOLDER;

import com.yy.petfinder.model.OAuth2Provider;
import com.yy.petfinder.model.User;
import com.yy.petfinder.persistence.UserRepository;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomOAuth2UserService extends DefaultReactiveOAuth2UserService {
  private final UserRepository userRepository;

  @Autowired
  public CustomOAuth2UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) {
    final String registrationId = userRequest.getClientRegistration().getRegistrationId();
    final Optional<OAuth2Provider> oAuth2ProviderOpt = OAuth2Provider.of(registrationId);
    return oAuth2ProviderOpt
        .map(
            oAuth2Provider -> {
              final Mono<OAuth2User> oAuth2User = super.loadUser(userRequest);
              return oAuth2User.flatMap(
                  oauthData -> {
                    final String email = oauthData.getAttribute("email");
                    return userRepository
                        .findByEmail(email)
                        .map(user -> user.toBuilder().oAuth2Provider(oAuth2Provider).build())
                        .flatMap(user -> userRepository.save(user))
                        .switchIfEmpty(
                            userRepository.save(
                                User.builder()
                                    .id(new ObjectId().toHexString())
                                    .email(email)
                                    .password(PASSWORD_PLACEHOLDER)
                                    .oAuth2Provider(oAuth2Provider)
                                    .build()))
                        .map(ignore -> oauthData);
                  });
            })
        .orElseThrow(
            () ->
                new OAuth2AuthenticationException(
                    new OAuth2Error(
                        "unsupported_oauth2_provider",
                        "Only Google and Facebook are currently supported",
                        null)));
  }
}
