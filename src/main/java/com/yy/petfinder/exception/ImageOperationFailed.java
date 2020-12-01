package com.yy.petfinder.exception;

import org.springframework.http.HttpStatus;

public class ImageOperationFailed extends BaseException {
  public ImageOperationFailed(String message, HttpStatus status) {
    super(message, status);
  }
}
