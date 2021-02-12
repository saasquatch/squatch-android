package com.saasquatch.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.List;
import java.util.Objects;

/**
 * Javascript interface with utility methods for SaaSquatch widgets.
 *
 * @see WebView#addJavascriptInterface(Object, String)
 */
public final class SquatchJavascriptInterface {

  public static final String JAVASCRIPT_INTERFACE_NAME = "SquatchAndroid";

  private final Context mContext;

  private SquatchJavascriptInterface(Context mContext) {
    this.mContext = mContext;
  }

  /**
   * Share on Facebook with browser fallback
   */
  @JavascriptInterface
  public void shareOnFacebook(@NonNull String shareUrl, @NonNull String fallbackUrl) {
    Objects.requireNonNull(shareUrl);
    Objects.requireNonNull(fallbackUrl);
    final Intent fbIntent = new Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, shareUrl)
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
    final Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mContext.startActivity(fallbackIntent);
  }

  /**
   * Show a toast from the web page
   */
  @JavascriptInterface
  public void showToast(@NonNull String toast) {
    Objects.requireNonNull(toast);
    Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
  }

  /**
   * Show a toast from the web page
   */
  @JavascriptInterface
  public void showToast2(@NonNull String toast) {
    Objects.requireNonNull(toast);
    Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
  }

  /**
   * Default factory method for {@link SquatchJavascriptInterface}.
   *
   * @see SquatchJavascriptInterface#JAVASCRIPT_INTERFACE_NAME
   */
  public static SquatchJavascriptInterface create(@NonNull Context mContext) {
    return new SquatchJavascriptInterface(Objects.requireNonNull(mContext));
  }

  /**
   * Apply {@link SquatchJavascriptInterface} to a given {@link WebView}.
   */
  public static void applyToWebView(@NonNull WebView webView) {
    webView.addJavascriptInterface(create(webView.getContext()), JAVASCRIPT_INTERFACE_NAME);
  }

}
