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

public class SquatchJavascriptInterface {

  public static final String JAVASCRIPT_INTERFACE_NAME = "SquatchAndroid";

  final Context mContext;

  /**
   * Instantiate the interface and set the context
   */
  private SquatchJavascriptInterface(Context mContext) {
    this.mContext = mContext;
  }

  /**
   * Share on Facebook with browser fallback
   */
  @JavascriptInterface
  public void shareOnFacebook(String shareUrl, String fallbackUrl) {
    final Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_TEXT, shareUrl);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    // See if official Facebook app is found
    // From https://stackoverflow.com/questions/7545254/android-and-facebook-share-intent
    boolean facebookAppFound = false;
    List<ResolveInfo> matches = mContext.getPackageManager().queryIntentActivities(intent, 0);
    for (ResolveInfo info : matches) {
      if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
        intent.setPackage(info.activityInfo.packageName);
        facebookAppFound = true;
        break;
      }
    }

    // As fallback to a browser
    if (facebookAppFound) {
      mContext.startActivity(intent);
    } else {
      final Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
      fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      mContext.startActivity(fallbackIntent);
    }
  }

  /**
   * Show a toast from the web page
   */
  @JavascriptInterface
  public void showToast(String toast) {
    Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
  }

  public static SquatchJavascriptInterface create(Context mContext) {
    return new SquatchJavascriptInterface(Objects.requireNonNull(mContext));
  }

  public static void applyToActivity(WebView myActivity) {
    myActivity
        .addJavascriptInterface(create(myActivity.getContext()), JAVASCRIPT_INTERFACE_NAME);
  }

}
