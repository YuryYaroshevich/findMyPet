package com.yy.petfinder.util;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UUIDService {
  private final byte[] namespace;

  public UUIDService(@Value("${uuid.namespace}") final String namespace) {
    this.namespace = namespace.getBytes();
  }

  public UUID generateUUIDFromBytes(final byte[] bytes) {
    final byte[] seed = new byte[namespace.length + bytes.length];
    System.arraycopy(namespace, 0, seed, 0, namespace.length);
    System.arraycopy(bytes, 0, seed, namespace.length, bytes.length);
    return UUID.nameUUIDFromBytes(seed);
  }
}
