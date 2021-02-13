package com.saasquatch.android;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.saasquatch.sdk.RequestOptions;
import com.saasquatch.sdk.SaaSquatchClient;
import com.saasquatch.sdk.input.WidgetUpsertInput;
import com.saasquatch.sdk.models.WidgetUpsertResult;
import com.saasquatch.sdk.output.JsonObjectApiResponse;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import org.reactivestreams.Publisher;

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
  public void close() throws IOException {
    saasquatchClient.close();
  }

  /**
   * @return The underlying {@link SaaSquatchClient}.
   */
  @NonNull
  public SaaSquatchClient getSaaSquatchClient() {
    return saasquatchClient;
  }

  @SuppressLint("SetJavaScriptEnabled")
  public Publisher<JsonObjectApiResponse> widgetUpsert(@NonNull WidgetUpsertInput widgetUpsertInput,
      @Nullable RequestOptions requestOptions, @NonNull WebView webView) {
    Objects.requireNonNull(webView);
    return Flowable.fromPublisher(saasquatchClient.widgetUpsert(widgetUpsertInput, requestOptions))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(apiResponse -> {
          final WebSettings webSettings = webView.getSettings();
          webSettings.setJavaScriptEnabled(true);
          webSettings.setDomStorageEnabled(true);
          SquatchJavascriptInterface.applyToWebView(webView);
          final WidgetUpsertResult widgetUpsertResult =
              apiResponse.toModel(WidgetUpsertResult.class);
          final String template = widgetUpsertResult.getTemplate();
          final String templateBase64 = Base64.encodeToString(
              template.getBytes(UTF_8), Base64.DEFAULT);
          webView.loadData(templateBase64, "text/html", "base64");
        });
  }

  /**
   * @return A {@link SquatchAndroid} instance that wraps
   */
  public static SquatchAndroid create(@NonNull SaaSquatchClient saasquatchClient) {
    return new SquatchAndroid(Objects.requireNonNull(saasquatchClient));
  }

  public static SquatchAndroid createForTenant(@NonNull String tenantAlias) {
    return create(SaaSquatchClient.createForTenant(tenantAlias));
  }

}
