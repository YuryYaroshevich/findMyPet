package com.yy.petfinder.exception;

public class OwnerIdUpdateException extends RuntimeException {
  private static final String ERROR_MSG_TMPL =
      "Attempt to update owner id from %s to %s for ad %s.";

  public OwnerIdUpdateException(
      final String ownerId, final String newOwnerId, final String adUuid) {
    super(String.format(ERROR_MSG_TMPL, ownerId, newOwnerId, adUuid));
  }
}
