package com.saasquatch.android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Javascript interface with utility methods for SaaSquatch widgets.
 *
 * @see WebView#addJavascriptInterface(Object, String)
 */
public final class SquatchJavascriptInterface {

  public static final String JAVASCRIPT_INTERFACE_NAME = "SquatchAndroid";

  /**
   * JavaScript polyfill for {@code navigator.share}. Android WebView does not support the
   * Web Share API natively, so this bridges it to {@link #shareContent} via the JS interface.
   * Inject this into a WebView after page load via {@code evaluateJavascript}.
   */
  public static final String NAVIGATOR_SHARE_POLYFILL = ""
      + "if (!navigator.share && window.SquatchAndroid && SquatchAndroid.shareContent) {"
      + "  navigator.share = function(data) {"
      + "    data = data || {};"
      + "    return new Promise(function(resolve, reject) {"
      + "      try {"
      + "        SquatchAndroid.shareContent("
      + "          data.title || '',"
      + "          data.text || '',"
      + "          data.url || ''"
      + "        );"
      + "        resolve();"
      + "      } catch(e) {"
      + "        reject(e);"
      + "      }"
      + "    });"
      + "  };"
      + "}";

  private final Context mContext;

  private SquatchJavascriptInterface(Context mContext) {
    this.mContext = mContext;
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

  /**
   * Open the native Android Share Sheet via {@link Intent#createChooser}.
   * This is called by the navigator.share polyfill injected in
   * {@link SquatchAndroidImpl#loadHtmlToWebView}.
   */
  @JavascriptInterface
  public void shareContent(@Nonnull String title, @Nonnull String text, @Nonnull String url) {
    Objects.requireNonNull(title);
    Objects.requireNonNull(text);
    Objects.requireNonNull(url);
    if (title.isEmpty() && text.isEmpty() && url.isEmpty()) {
      return;
    }
    final String body = url.isEmpty() ? text : text.isEmpty() ? url : text + "\n" + url;
    final Intent sendIntent = new Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_SUBJECT, title)
        .putExtra(Intent.EXTRA_TEXT, body)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    final Intent chooser = Intent.createChooser(sendIntent, title)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mContext.startActivity(chooser);
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
   */
  public static SquatchJavascriptInterface create(@Nonnull Context mContext) {
    return new SquatchJavascriptInterface(Objects.requireNonNull(mContext));
  }

  /**
   * Apply {@link SquatchJavascriptInterface} to a given {@link WebView}.
   */
  public static void applyToWebView(@Nonnull WebView webView) {
    webView.addJavascriptInterface(create(webView.getContext()), JAVASCRIPT_INTERFACE_NAME);
  }

}
