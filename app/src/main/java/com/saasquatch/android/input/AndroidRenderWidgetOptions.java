package com.saasquatch.android.input;

import android.webkit.WebView;
import java.util.Objects;
import javax.annotation.Nonnull;

public final class AndroidRenderWidgetOptions {

  private final WebView webView;
  private final String webViewBaseUrl;

  private AndroidRenderWidgetOptions(WebView webView, String webViewBaseUrl) {
    this.webView = webView;
    this.webViewBaseUrl = webViewBaseUrl;
  }

  public WebView getWebView() {
    return webView;
  }

  public String getWebViewBaseUrl() {
    return webViewBaseUrl;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static AndroidRenderWidgetOptions ofWebView(@Nonnull WebView webView) {
    return newBuilder().setWebView(webView).build();
  }

  public static final class Builder {

    private WebView webView;
    private String webViewBaseUrl = "https://fast.ssqt.io/";

    private Builder() {}

    public Builder setWebView(@Nonnull WebView webView) {
      this.webView = Objects.requireNonNull(webView, "webView");
      return this;
    }

    public Builder setWebViewBaseUrl(String webViewBaseUrl) {
      this.webViewBaseUrl = webViewBaseUrl;
      return this;
    }

    public AndroidRenderWidgetOptions build() {
      return new AndroidRenderWidgetOptions(Objects.requireNonNull(webView, "webView"),
          webViewBaseUrl);
    }

  }

}
