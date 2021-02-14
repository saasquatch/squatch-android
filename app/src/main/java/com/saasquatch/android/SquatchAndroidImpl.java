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
import com.saasquatch.sdk.input.RenderWidgetInput;
import com.saasquatch.sdk.input.WidgetUpsertInput;
import com.saasquatch.sdk.models.WidgetUpsertResult;
import com.saasquatch.sdk.output.JsonObjectApiResponse;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.reactivestreams.Publisher;

final class SquatchAndroidImpl implements SquatchAndroid {

  private final SaaSquatchClient saasquatchClient;

  SquatchAndroidImpl(@NonNull SaaSquatchClient saasquatchClient) {
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
  @Override
  public SaaSquatchClient getSaaSquatchClient() {
    return saasquatchClient;
  }

  @Override
  @SuppressLint("SetJavaScriptEnabled")
  public Publisher<JsonObjectApiResponse> widgetUpsert(@NonNull WidgetUpsertInput widgetUpsertInput,
      @Nullable RequestOptions requestOptions, @NonNull WebView webView) {
    Objects.requireNonNull(webView);
    return fromPublisherCommon(saasquatchClient.widgetUpsert(widgetUpsertInput, requestOptions))
        .doOnNext(apiResponse -> {
          final WidgetUpsertResult widgetUpsertResult =
              apiResponse.toModel(WidgetUpsertResult.class);
          loadHtml(webView, widgetUpsertResult.getTemplate());
        });
  }

  private <T> Flowable<T> fromPublisherCommon(Publisher<? extends T> publisher) {
    return Flowable.<T>fromPublisher(publisher)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  private void commonWebViewMutation(@Nonnull WebView webView) {
    final WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDomStorageEnabled(true);
    SquatchJavascriptInterface.applyToWebView(webView);
  }

  private void loadHtml(@Nonnull WebView webView, String htmlString) {
    commonWebViewMutation(webView);
    final String htmlBase64 = Base64.encodeToString(htmlString.getBytes(UTF_8), Base64.DEFAULT);
    webView.loadData(htmlBase64, "text/html", "base64");
  }

}
