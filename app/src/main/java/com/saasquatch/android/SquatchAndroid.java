package com.saasquatch.android;

import android.annotation.SuppressLint;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.saasquatch.sdk.RequestOptions;
import com.saasquatch.sdk.SaaSquatchClient;
import com.saasquatch.sdk.input.WidgetType;
import com.saasquatch.sdk.models.WidgetUpsertResult;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.Closeable;
import java.util.Map;
import java.util.Objects;

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
  public void close() {
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
  public void widgetUpsert(@NonNull Map<String, Object> userInput,
      @Nullable WidgetType widgetType, @Nullable RequestOptions requestOptions,
      @NonNull WebView webView) {
    Objects.requireNonNull(webView);
    Flowable.fromPublisher(saasquatchClient.widgetUpsert(userInput, widgetType, requestOptions))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(apiResponse -> {
          final WebSettings webSettings = webView.getSettings();
          webSettings.setJavaScriptEnabled(true);
          webSettings.setDomStorageEnabled(true);
          final WidgetUpsertResult widgetUpsertResult =
              apiResponse.toModel(WidgetUpsertResult.class);
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
