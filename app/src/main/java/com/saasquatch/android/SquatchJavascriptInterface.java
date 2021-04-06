package com.saasquatch.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.saasquatch.sdk.RequestOptions;
import com.saasquatch.sdk.input.PushWidgetAnalyticsEventInput;
import com.saasquatch.sdk.input.UserIdInput;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Javascript interface with utility methods for SaaSquatch widgets.
 *
 * @see WebView#addJavascriptInterface(Object, String)
 */
public final class SquatchJavascriptInterface {

  public static final String JAVASCRIPT_INTERFACE_NAME = "SquatchAndroid";

  private final Context mContext;
  private final SquatchAndroid squatchAndroid;

  private SquatchJavascriptInterface(@Nullable SquatchAndroid squatchAndroid,
      @Nonnull Context mContext) {
    this.mContext = mContext;
    this.squatchAndroid = squatchAndroid;
  }

  /**
   * Share on Facebook with browser fallback
   */
  @JavascriptInterface
  public void shareOnFacebook(@Nonnull String shareLink, @Nonnull String messageLink) {
    Objects.requireNonNull(shareLink);
    Objects.requireNonNull(messageLink);
    final Intent fbIntent = new Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, shareLink)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // See if official Facebook app is found
    // From https://stackoverflow.com/questions/7545254/android-and-facebook-share-intent
    final List<ResolveInfo> resolveInfoList = mContext.getPackageManager()
        .queryIntentActivities(fbIntent, 0);
    for (ResolveInfo resolveInfo : resolveInfoList) {
      if (resolveInfo.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
        fbIntent.setPackage(resolveInfo.activityInfo.packageName);
        mContext.startActivity(fbIntent);
        return;
      }
    }
    // As fallback to a browser
    final Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(messageLink))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mContext.startActivity(fallbackIntent);
  }

  @JavascriptInterface
  public void shareOnFacebook(@Nonnull String params) {
    final JsonObject paramsJson = JsonParser.parseString(params).getAsJsonObject();
    final String shareLink = paramsJson.getAsJsonPrimitive("shareLink").getAsString();
    final String messageLink = paramsJson.getAsJsonPrimitive("messageLink").getAsString();
    if (squatchAndroid != null) {
      final String tenantAlias = paramsJson.getAsJsonPrimitive("tenantAlias").getAsString();
      final JsonObject userInput = paramsJson.getAsJsonObject("user");
      final JsonPrimitive programIdJson = paramsJson.getAsJsonPrimitive("programId");
      final PushWidgetAnalyticsEventInput.Builder analyticsBuilder = PushWidgetAnalyticsEventInput
          .newBuilder()
          .setUser(UserIdInput.of(userInput.getAsJsonPrimitive("accountId").getAsString(),
              userInput.getAsJsonPrimitive("id").getAsString()))
          .setEngagementMedium("MOBILE")
          .setShareMedium("FACEBOOK");
      if (programIdJson != null) {
        analyticsBuilder.setProgramId(programIdJson.getAsString());
      }
      Flowable.fromPublisher(squatchAndroid.getSaaSquatchClient().pushWidgetSharedAnalyticsEvent(
          analyticsBuilder.build(),
          RequestOptions.newBuilder().setTenantAlias(tenantAlias).build()))
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe();
    }
    shareOnFacebook(shareLink, messageLink);
  }

  /**
   * Show a toast from the web page
   */
  @JavascriptInterface
  public void showToast(@Nonnull String toast) {
    Objects.requireNonNull(toast);
    Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
  }

  /**
   * Default factory method for {@link SquatchJavascriptInterface}.
   *
   * @see SquatchJavascriptInterface#JAVASCRIPT_INTERFACE_NAME
   * @deprecated use {@link #applyToWebView(SquatchAndroid, WebView)}
   */
  @Deprecated
  public static SquatchJavascriptInterface create(@Nonnull Context mContext) {
    return new SquatchJavascriptInterface(null, Objects.requireNonNull(mContext));
  }

  private static SquatchJavascriptInterface create(@Nonnull SquatchAndroid squatchAndroid,
      @Nonnull Context mContext) {
    return new SquatchJavascriptInterface(Objects.requireNonNull(squatchAndroid),
        Objects.requireNonNull(mContext));
  }

  /**
   * Apply {@link SquatchJavascriptInterface} to a given {@link WebView}.
   *
   * @deprecated use {@link #applyToWebView(SquatchAndroid, WebView)}
   */
  @Deprecated
  public static void applyToWebView(@Nonnull WebView webView) {
    webView.addJavascriptInterface(create(webView.getContext()), JAVASCRIPT_INTERFACE_NAME);
  }

  /**
   * Apply {@link SquatchJavascriptInterface} to a given {@link WebView}.
   */
  public static void applyToWebView(@Nonnull SquatchAndroid squatchAndroid,
      @Nonnull WebView webView) {
    webView.addJavascriptInterface(create(squatchAndroid, webView.getContext()),
        JAVASCRIPT_INTERFACE_NAME);
  }

}
