package com.saasquatch.android;

import androidx.annotation.NonNull;
import com.saasquatch.sdk.ClientOptions;
import com.saasquatch.sdk.SaaSquatchClient;
import java.io.Closeable;

/**
 * Wrapper for {@link SaaSquatchClient} that contains Android specific features.
 *
 * @author sli
 */
public final class SquatchAndroid implements Closeable {

  private final SaaSquatchClient saasquatchClient;

  private SquatchAndroid(@NonNull SaaSquatchClient saasquatchClient) {
    this.saasquatchClient = saasquatchClient;
  }

  @Override
  public void close() {
    saasquatchClient.close();
  }

  @NonNull
  public SaaSquatchClient getSaaSquatchClient() {
    return saasquatchClient;
  }

  public static SquatchAndroid create(@NonNull ClientOptions clientOptions) {
    return new SquatchAndroid(SaaSquatchClient.create(clientOptions));
  }

  public static SquatchAndroid createForTenant(@NonNull String tenantAlias) {
    return new SquatchAndroid(SaaSquatchClient.createForTenant(tenantAlias));
  }

}
