package com.saasquatch.android;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.saasquatch.sdk.RequestOptions;
import com.saasquatch.sdk.SaaSquatchClient;
import com.saasquatch.sdk.exceptions.SaaSquatchApiException;
import com.saasquatch.sdk.input.RenderWidgetInput;
import com.saasquatch.sdk.input.WidgetUpsertInput;
import com.saasquatch.sdk.models.WidgetUpsertResult;
import com.saasquatch.sdk.output.ApiError;
import com.saasquatch.sdk.output.JsonObjectApiResponse;
import com.saasquatch.sdk.output.TextApiResponse;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.reactivestreams.Publisher;

final class SquatchAndroidImpl implements SquatchAndroid {

  private final SaaSquatchClient saasquatchClient;

  SquatchAndroidImpl(@Nonnull SaaSquatchClient saasquatchClient) {
    this.saasquatchClient = saasquatchClient;
  }

  @Override
  public void close() throws IOException {
    saasquatchClient.close();
  }

  /**
   * @return The underlying {@link SaaSquatchClient}.
   */
  @Nonnull
  @Override
  public SaaSquatchClient getSaaSquatchClient() {
    return saasquatchClient;
  }

  @Override
  public void loadHtmlToWebView(@Nonnull WebView webView, @Nonnull String htmlString) {
    commonWebViewMutation(webView);
    final String htmlBase64 = Base64.encodeToString(htmlString.getBytes(UTF_8), Base64.DEFAULT);
    webView.loadData(htmlBase64, "text/html", "base64");
  }

  private void loadErrorHtmlToWebView(@Nonnull WebView webView, Throwable t) {
    int count = 0;
    String rsCode = null;
    do {
      if (t instanceof SaaSquatchApiException) {
        final ApiError apiError = ((SaaSquatchApiException) t).getApiError();
        rsCode = apiError.getRsCode();
        break;
      }
    } while ((t = t.getCause()) != null && count++ < 100);
    final String htmlString = new MessageFormat(ERR_HTML_TEMPLATE, Locale.ROOT)
        .format(new Object[]{rsCode});
    loadHtmlToWebView(webView, htmlString);
  }

  @Override
  public Publisher<TextApiResponse> renderWidget(@Nonnull RenderWidgetInput renderWidgetInput,
      @Nullable RequestOptions requestOptions, @Nonnull WebView webView) {
    return fromPublisherCommon(saasquatchClient.renderWidget(renderWidgetInput, requestOptions))
        .doOnNext(apiResponse -> loadHtmlToWebView(webView, apiResponse.getData()));
  }

  @Override
  @SuppressLint("SetJavaScriptEnabled")
  public Publisher<JsonObjectApiResponse> widgetUpsert(@Nonnull WidgetUpsertInput widgetUpsertInput,
      @Nullable RequestOptions requestOptions, @Nonnull WebView webView) {
    Objects.requireNonNull(webView);
    return fromPublisherCommon(saasquatchClient.widgetUpsert(widgetUpsertInput, requestOptions))
        .doOnNext(apiResponse -> {
          final WidgetUpsertResult widgetUpsertResult =
              apiResponse.toModel(WidgetUpsertResult.class);
          loadHtmlToWebView(webView, widgetUpsertResult.getTemplate());
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

  private static final String ERR_HTML_TEMPLATE = "<!DOCTYPE html>\n"
      + "<html>\n"
      + "  <head>\n"
      + "    <link\n"
      + "      rel=\"stylesheet\"\n"
      + "      media=\"all\"\n"
      + "      href=\"https://fast.ssqt.io/assets/css/widget/errorpage.css\"\n"
      + "    />\n"
      + "  </head>\n"
      + "  <body>\n"
      + "    <div class=\"squatch-container embed\" style=\"width: 100%\">\n"
      + "      <div class=\"errorbody\">\n"
      + "        <div class=\"sadface\">\n"
      + "          <img src=\"https://fast.ssqt.io/assets/images/face.png\" />\n"
      + "        </div>\n"
      + "        <h4>Our referral program is temporarily unavailable.</h4>\n"
      + "        <br />\n"
      + "        <p>Please reload the page or check back later.</p>\n"
      + "        <p>If the persists please contact our support team.</p>\n"
      + "        <br />\n"
      + "        <br />\n"
      + "        <div class=\"right-align errtxt\">Error Code: {0}</div>\n"
      + "      </div>\n"
      + "    </div>\n"
      + "  </body>\n"
      + "</html>";

}
