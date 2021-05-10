package com.yy.petfinder.service;

import com.yy.petfinder.rest.model.EmailMessageData;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EmailService {
  private final JavaMailSender emailSender;
  private final String appEmail;

  @Autowired
  public EmailService(
      final JavaMailSender emailSender, @Value("${spring.mail.username}") final String appEmail) {
    this.emailSender = emailSender;
    this.appEmail = appEmail;
  }

  public Mono<Void> sendEmail(final String email, final EmailMessageData emailMessageData) {
    return Mono.fromRunnable(
        () -> {
          final MimeMessage mimeMessage = emailSender.createMimeMessage();
          try {
            mimeMessage.setFrom(appEmail);
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            mimeMessage.setSubject(emailMessageData.getSubject());
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setText(emailMessageData.getText(), "UTF-8", "html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            mimeMessage.setContent(multipart);
          } catch (MessagingException e) {
            e.printStackTrace();
          }
          emailSender.send(mimeMessage);
        });
  }
}
