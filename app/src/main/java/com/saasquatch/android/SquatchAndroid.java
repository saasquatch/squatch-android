package com.saasquatch.android;

import android.webkit.WebView;
import com.saasquatch.sdk.RequestOptions;
import com.saasquatch.sdk.SaaSquatchClient;
import com.saasquatch.sdk.input.RenderWidgetInput;
import com.saasquatch.sdk.input.WidgetUpsertInput;
import com.saasquatch.sdk.output.JsonObjectApiResponse;
import com.saasquatch.sdk.output.TextApiResponse;
import java.io.Closeable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
  static SquatchAndroid create(@Nonnull SaaSquatchClient saasquatchClient) {
    return new SquatchAndroidImpl(Objects.requireNonNull(saasquatchClient));
  }

  static SquatchAndroid createForTenant(@Nonnull String tenantAlias) {
    return create(SaaSquatchClient.createForTenant(tenantAlias));
  }

  /**
   * @return The underlying {@link SaaSquatchClient}.
   */
  @Nonnull
  SaaSquatchClient getSaaSquatchClient();

  /**
   * Load the given HTML string into a {@link WebView} after applying common configuration to the
   * {@link WebView}.
   */
  void loadHtmlToWebView(@Nonnull WebView webView, @Nonnull String htmlString);

  /**
   * Wrapper for {@link SaaSquatchClient#renderWidget(RenderWidgetInput, RequestOptions)} that loads
   * the result widget HTML into a {@link WebView}.
   */
  Publisher<TextApiResponse> renderWidget(@Nonnull RenderWidgetInput renderWidgetInput,
      @Nullable RequestOptions requestOptions, @Nonnull WebView webView);

  /**
   * Wrapper for {@link SaaSquatchClient#widgetUpsert(WidgetUpsertInput, RequestOptions)} that loads
   * the result widget HTML into a {@link WebView}.
   */
  Publisher<JsonObjectApiResponse> widgetUpsert(@Nonnull WidgetUpsertInput widgetUpsertInput,
      @Nullable RequestOptions requestOptions, @Nonnull WebView webView);

}
