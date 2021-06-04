package com.yy.petfinder.service;

import static com.yy.petfinder.exception.InvalidCredentialsException.oldPasswordNotMatch;
import static java.util.function.Predicate.not;

import com.yy.petfinder.exception.DuplicateEmailException;
import com.yy.petfinder.exception.InvalidPasswordRecoveryRequestException;
import com.yy.petfinder.exception.UserNotFoundException;
import com.yy.petfinder.exception.UserPasswordUpdateException;
import com.yy.petfinder.model.User;
import com.yy.petfinder.model.UserRandomKey;
import com.yy.petfinder.persistence.UserRandomKeyRepository;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.CreateUser;
import com.yy.petfinder.rest.model.PasswordUpdate;
import com.yy.petfinder.rest.model.PasswordUpdateEmail;
import com.yy.petfinder.rest.model.PasswordUpdateRequest;
import com.yy.petfinder.rest.model.PrivateUserView;
import com.yy.petfinder.rest.model.UserUpdate;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService {
  private static final String NOT_NEEDED_OLD_PASSWD = "not_needed";

  private final UserRepository userRepository;
  private final UserRandomKeyRepository userRandomKeyRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswdUpdateEmailService passwdUpdateEmailService;

  @Autowired
  public UserService(
      final UserRepository userRepository,
      final UserRandomKeyRepository userRandomKeyRepository,
      final PasswordEncoder passwordEncoder,
      final PasswdUpdateEmailService passwdUpdateEmailService) {
    this.userRepository = userRepository;
    this.userRandomKeyRepository = userRandomKeyRepository;
    this.passwordEncoder = passwordEncoder;
    this.passwdUpdateEmailService = passwdUpdateEmailService;
  }

  public Mono<PrivateUserView> getUser(final String id) {
    final Mono<User> user =
        userRepository.findById(id).switchIfEmpty(Mono.error(new UserNotFoundException(id)));
    final Mono<PrivateUserView> userView = user.map(this::userToView);
    return userView;
  }

  public Mono<PrivateUserView> createUser(CreateUser createUser) {
    final String id = new ObjectId().toHexString();
    final String encodedPassword = passwordEncoder.encode(createUser.getPassword());
    final User newUser =
        User.builder()
            .id(id)
            .email(createUser.getEmail())
            .phone(createUser.getPhone())
            .password(encodedPassword)
            .messengers(createUser.getMessengers())
            .build();

    final Mono<User> createdUser =
        userRepository
            .save(newUser)
            .onErrorMap(
                DuplicateKeyException.class,
                e -> new DuplicateEmailException(createUser.getEmail()));

    final Mono<PrivateUserView> userView = createdUser.map(this::userToView);
    return userView;
  }

  private PrivateUserView userToView(final User user) {
    return PrivateUserView.builder()
        .id(user.getId())
        .email(user.getEmail())
        .phone(user.getPhone())
        .messengers(user.getMessengers())
        .build();
  }

  public Mono<PrivateUserView> updateUser(final String userId, final UserUpdate rawUserUpdate) {
    Mono<Boolean> passwordUpdateValid = Mono.just(true);
    final PasswordUpdate passwordUpdate = rawUserUpdate.getPasswordUpdate();
    if (passwordUpdate != null) {
      passwordUpdateValid =
          passwordUpdateValid
              .flatMap(ignore -> isOldPasswordMatch(passwordUpdate, userId))
              .doOnNext(
                  passwordsMatch -> {
                    if (!passwordsMatch) {
                      throw oldPasswordNotMatch();
                    }
                  });
    }

    final UserUpdate userUpdate = encodePasswordIfSet(rawUserUpdate);
    return passwordUpdateValid.flatMap(
        ignore -> userRepository.findAndModify(userUpdate, userId).map(this::userToView));
  }

  private UserUpdate encodePasswordIfSet(final UserUpdate userUpdate) {
    final PasswordUpdate passwordUpdate = userUpdate.getPasswordUpdate();
    if (passwordUpdate != null) {
      final String encodedNewPassword = passwordEncoder.encode(passwordUpdate.getNewPassword());
      return userUpdate
          .toBuilder()
          .passwordUpdate(new PasswordUpdate(encodedNewPassword, passwordUpdate.getOldPassword()))
          .build();
    }
    return userUpdate;
  }

  private Mono<Boolean> isOldPasswordMatch(
      final PasswordUpdate passwordUpdate, final String userId) {
    return userRepository
        .findById(userId)
        .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
        .filter(not(User::isOAuth2Authenticated))
        .switchIfEmpty(Mono.error(new UserPasswordUpdateException()))
        .map(User::getPassword)
        .map(
            oldEncodedPass ->
                passwordEncoder.matches(passwordUpdate.getOldPassword(), oldEncodedPass));
  }

  public Mono<UserRandomKey> initiatePasswordUpdate(final PasswordUpdateEmail passwordUpdateEmail) {
    return userRepository
        .findByEmail(passwordUpdateEmail.getEmail())
        .flatMap(
            user ->
                passwdUpdateEmailService.sendNewPasswordEmail(passwordUpdateEmail, user.getId()))
        .flatMap(userRandomKey -> userRandomKeyRepository.save(userRandomKey));
  }

  public Mono<User> setNewPassword(PasswordUpdateRequest passwordUpdateRequest) {
    return userRandomKeyRepository
        .findByIdAndRandomKey(passwordUpdateRequest.getUserId(), passwordUpdateRequest.getKey())
        .switchIfEmpty(Mono.error(new InvalidPasswordRecoveryRequestException()))
        .flatMap(
            ignore -> {
              final String encodedNewPassword =
                  passwordEncoder.encode(passwordUpdateRequest.getNewPassword());
              final UserUpdate userUpdate =
                  UserUpdate.builder()
                      .passwordUpdate(new PasswordUpdate(encodedNewPassword, NOT_NEEDED_OLD_PASSWD))
                      .build();
              return userRepository.findAndModify(userUpdate, passwordUpdateRequest.getUserId());
            });
  }
}
