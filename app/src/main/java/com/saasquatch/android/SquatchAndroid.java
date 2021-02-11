package com.saasquatch.android;

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

  private SquatchAndroid(SaaSquatchClient saasquatchClient) {
    this.saasquatchClient = saasquatchClient;
  }

  @Override
  public void close() {
    saasquatchClient.close();
  }

  public SaaSquatchClient getSaaSquatchClient() {
    return saasquatchClient;
  }

  public static SquatchAndroid create(ClientOptions clientOptions) {
    return new SquatchAndroid(SaaSquatchClient.create(clientOptions));
  }

  public static SquatchAndroid createForTenant(String tenantAlias) {
    return new SquatchAndroid(SaaSquatchClient.createForTenant(tenantAlias));
  }

}
