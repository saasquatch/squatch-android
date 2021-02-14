package com.saasquatch.android.input;

import android.webkit.WebView;
import java.util.Objects;
import javax.annotation.Nonnull;

public final class AndroidRenderWidgetOptions {

  private final WebView webView;

  private AndroidRenderWidgetOptions(WebView webView) {
    this.webView = webView;
  }

  public WebView getWebView() {
    return webView;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static AndroidRenderWidgetOptions ofWebView(@Nonnull WebView webView) {
    return newBuilder().setWebView(webView).build();
  }

  public static final class Builder {

    private WebView webView;

    private Builder() {}

    public Builder setWebView(@Nonnull WebView webView) {
      this.webView = Objects.requireNonNull(webView, "webView");
      return this;
    }

    public AndroidRenderWidgetOptions build() {
      return new AndroidRenderWidgetOptions(Objects.requireNonNull(webView, "webView"));
    }

  }

}
