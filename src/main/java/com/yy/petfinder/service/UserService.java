package com.yy.petfinder.service;

import static com.yy.petfinder.exception.InvalidCredentialsException.oldPasswordNotMatch;

import com.yy.petfinder.exception.DuplicateEmailException;
import com.yy.petfinder.model.User;
import com.yy.petfinder.model.UserRandomKey;
import com.yy.petfinder.persistence.UserRandomKeyRepository;
import com.yy.petfinder.persistence.UserRepository;
import com.yy.petfinder.rest.model.CreateUser;
import com.yy.petfinder.rest.model.PasswordUpdate;
import com.yy.petfinder.rest.model.PasswordUpdateEmail;
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
  private final UserRepository userRepository;
  private final UserRandomKeyRepository userRandomKeyRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  @Autowired
  public UserService(
      final UserRepository userRepository,
      final UserRandomKeyRepository userRandomKeyRepository,
      final PasswordEncoder passwordEncoder,
      final EmailService emailService) {
    this.userRepository = userRepository;
    this.userRandomKeyRepository = userRandomKeyRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
  }

  public Mono<PrivateUserView> getUser(final String id) {
    final Mono<User> user = userRepository.findById(id);
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

  public Mono<PrivateUserView> updateUser(final String userId, UserUpdate rawUserUpdate) {
    Mono<Boolean> passwordUpdateValid = Mono.just(true);
    final UserUpdate userUpdate = encodePasswordsIfSet(rawUserUpdate);
    final PasswordUpdate passwordUpdate = userUpdate.getPasswordUpdate();
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

    return passwordUpdateValid.flatMap(
        ignore -> userRepository.findAndModify(userUpdate, userId).map(this::userToView));
  }

  private UserUpdate encodePasswordsIfSet(final UserUpdate userUpdate) {
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
        .map(User::getPassword)
        .map(
            oldEncodedPass ->
                passwordEncoder.matches(passwordUpdate.getOldPassword(), oldEncodedPass));
  }

  public Mono<UserRandomKey> initiatePasswordUpdate(final PasswordUpdateEmail passwordUpdateEmail) {
    return userRepository
        .findByEmail(passwordUpdateEmail.getEmail())
        .flatMap(user -> emailService.sendNewPasswordEmail(passwordUpdateEmail, user.getId()))
        .flatMap(userRandomKey -> userRandomKeyRepository.save(userRandomKey));
  }
}
