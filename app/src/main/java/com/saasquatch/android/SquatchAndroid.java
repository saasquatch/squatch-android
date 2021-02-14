package com.saasquatch.android;

import android.annotation.SuppressLint;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.saasquatch.sdk.RequestOptions;
import com.saasquatch.sdk.SaaSquatchClient;
import com.saasquatch.sdk.input.WidgetUpsertInput;
import com.saasquatch.sdk.output.JsonObjectApiResponse;
import java.io.Closeable;
import java.util.Objects;
import org.reactivestreams.Publisher;

/**
 * Wrapper for {@link SaaSquatchClient} that contains Android specific features.
 *
 * @author sli
 */
public interface SquatchAndroid extends Closeable {

  /**
   * @return A {@link SquatchAndroid} instance that wraps
   */
  static SquatchAndroid create(@NonNull SaaSquatchClient saasquatchClient) {
    return new SquatchAndroidImpl(Objects.requireNonNull(saasquatchClient));
  }

  static SquatchAndroid createForTenant(@NonNull String tenantAlias) {
    return create(SaaSquatchClient.createForTenant(tenantAlias));
  }

  /**
   * @return The underlying {@link SaaSquatchClient}.
   */
  @NonNull
  SaaSquatchClient getSaaSquatchClient();

  Publisher<JsonObjectApiResponse> widgetUpsert(@NonNull WidgetUpsertInput widgetUpsertInput,
      @Nullable RequestOptions requestOptions, @NonNull WebView webView);

}
