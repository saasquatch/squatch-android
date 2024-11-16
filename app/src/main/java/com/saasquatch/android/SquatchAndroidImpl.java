package com.saasquatch.android;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.saasquatch.android.input.AndroidRenderWidgetOptions;
import com.saasquatch.sdk.RequestOptions;
import com.saasquatch.sdk.SaaSquatchClient;
import com.saasquatch.sdk.exceptions.SaaSquatchApiException;
import com.saasquatch.sdk.input.PushWidgetAnalyticsEventInput;
import com.saasquatch.sdk.input.RenderWidgetInput;
import com.saasquatch.sdk.input.UserIdInput;
import com.saasquatch.sdk.input.WidgetType;
import com.saasquatch.sdk.input.WidgetUpsertInput;
import com.saasquatch.sdk.models.WidgetUpsertResult;
import com.saasquatch.sdk.output.ApiError;
import com.saasquatch.sdk.output.JsonObjectApiResponse;
import com.saasquatch.sdk.output.StatusOnlyApiResponse;
import com.saasquatch.sdk.output.TextApiResponse;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
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

  @Nonnull
  @Override
  public SaaSquatchClient getSaaSquatchClient() {
    return saasquatchClient;
  }

  @Override
  public Publisher<TextApiResponse> renderWidget(@Nonnull RenderWidgetInput renderWidgetInput,
      @Nullable RequestOptions requestOptions,
      @Nonnull AndroidRenderWidgetOptions androidRenderWidgetOptions) {
    Objects.requireNonNull(androidRenderWidgetOptions, "androidRenderWidgetOptions");
    return Flowable.fromPublisher(saasquatchClient.renderWidget(renderWidgetInput, requestOptions))
        .compose(publisherCommon(androidRenderWidgetOptions))
        .doOnNext(apiResponse -> loadHtmlToWebView(androidRenderWidgetOptions,
            Objects.requireNonNull(apiResponse.getData())))
        .concatMap(apiResponse -> {
          if (renderWidgetInput.getUser() == null) {
            // No analytics if the widget was rendered without a user
            return Flowable.just(apiResponse);
          }
          final WidgetType widgetType = renderWidgetInput.getWidgetType();
          return Flowable.fromPublisher(recordWidgetLoadAnalytics(renderWidgetInput.getUser(),
                  widgetType == null ? null : widgetType.getProgramId(), requestOptions))
              .compose(publisherCommon(androidRenderWidgetOptions))
              .ignoreElements()
              .andThen(Flowable.just(apiResponse));
        });
  }

  @Override
  public Publisher<JsonObjectApiResponse> widgetUpsert(@Nonnull WidgetUpsertInput widgetUpsertInput,
      @Nullable RequestOptions requestOptions,
      @Nonnull AndroidRenderWidgetOptions androidRenderWidgetOptions) {
    Objects.requireNonNull(androidRenderWidgetOptions, "androidRenderWidgetOptions");
    return Flowable.fromPublisher(saasquatchClient.widgetUpsert(widgetUpsertInput, requestOptions))
        .compose(publisherCommon(androidRenderWidgetOptions))
        .doOnNext(apiResponse -> {
          final WidgetUpsertResult widgetUpsertResult =
              apiResponse.toModel(WidgetUpsertResult.class);
          loadHtmlToWebView(androidRenderWidgetOptions, widgetUpsertResult.getTemplate());
        })
        .concatMap(apiResponse -> {
          final WidgetType widgetType = widgetUpsertInput.getWidgetType();
          return Flowable.fromPublisher(recordWidgetLoadAnalytics(
                  UserIdInput.of(widgetUpsertInput.getAccountId(), widgetUpsertInput.getUserId()),
                  widgetType == null ? null : widgetType.getProgramId(), requestOptions))
              .compose(publisherCommon(androidRenderWidgetOptions))
              .ignoreElements()
              .andThen(Flowable.just(apiResponse));
        });
  }

  private <T> FlowableTransformer<T, T> publisherCommon(
      @Nonnull AndroidRenderWidgetOptions androidRenderWidgetOptions) {
    return p -> p.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError(t -> loadErrorHtmlToWebView(androidRenderWidgetOptions, t));
  }

  private Publisher<StatusOnlyApiResponse> recordWidgetLoadAnalytics(UserIdInput user,
      String programId, @Nullable RequestOptions requestOptions) {
    final PushWidgetAnalyticsEventInput.Builder analyticsBuilder =
        PushWidgetAnalyticsEventInput.newBuilder();
    analyticsBuilder.setUser(user);
    if (programId != null) {
      analyticsBuilder.setProgramId(programId);
    }
    analyticsBuilder.setEngagementMedium("MOBILE");
    return saasquatchClient.pushWidgetLoadedAnalyticsEvent(
        analyticsBuilder.build(), requestOptions);
  }

  @SuppressLint("SetJavaScriptEnabled")
  private void loadHtmlToWebView(@Nonnull AndroidRenderWidgetOptions androidRenderWidgetOptions,
      @Nonnull String htmlString) {
    final WebView webView = androidRenderWidgetOptions.getWebView();
    final WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDomStorageEnabled(true);
    SquatchJavascriptInterface.applyToWebView(webView);
    final String htmlBase64 = Base64.encodeToString(htmlString.getBytes(UTF_8), Base64.DEFAULT);
    webView.loadData(htmlBase64, "text/html; charset=utf-8", "base64");
  }

  private void loadErrorHtmlToWebView(
      @Nonnull AndroidRenderWidgetOptions androidRenderWidgetOptions, Throwable throwable) {
    int count = 0;
    String rsCode = null;
    do {
      if (throwable instanceof SaaSquatchApiException) {
        final ApiError apiError = ((SaaSquatchApiException) throwable).getApiError();
        rsCode = apiError.getRsCode();
        break;
      }
    } while ((throwable = throwable.getCause()) != null && count++ < 100);
    final String htmlString = new MessageFormat(ERR_HTML_TEMPLATE, Locale.ROOT)
        .format(new Object[]{rsCode});
    loadHtmlToWebView(androidRenderWidgetOptions, htmlString);
  }

  private static final String ERR_HTML_TEMPLATE = ""
      + "<!DOCTYPE html>\n"
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
