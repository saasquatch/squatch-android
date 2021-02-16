# SquatchAndroid

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![](https://jitpack.io/v/saasquatch/squatch-android.svg)](https://jitpack.io/#saasquatch/squatch-android)

Helper library for loading SaaSquatch widgets in Android WebView

## Adding SaaSquatch Java SDK to your project

SaaSquatch Java SDK is hosted on JitPack.

Add JitPack repository:

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency:

```gradle
dependencies {
    implementation 'com.github.saasquatch:squatch-android:Tag'
}
```

For more information and other built tools, [please refer to the JitPack page](https://jitpack.io/#saasquatch/squatch-android).

## Using the SDK

This library is a wrapper of [SaaSquatch Java SDK](https://github.com/saasquatch/saasquatch-java-sdk) with Android specific features, specifically loading widgets into a WebView. In fact, The `SquatchAndroid` interface has a method called `getSaaSquatchClient()`, which you can use to retrieve the underlying `SaaSquatchClient`. Depending on your use case, [SaaSquatch Java SDK](https://github.com/saasquatch/saasquatch-java-sdk) may be what you need.

The entry point of the SDK is `SquatchAndroid`. To create a `SquatchAndroid` for your tenant with default options, use:

```java
SquatchAndroid.createForTenant("yourTenantAlias");
```

It is recommended that you keep a singleton `SquatchAndroid` for all your requests instead of creating a new `SquatchAndroid` for every request. `SquatchAndroid` implements `Closeable`, and it's a good idea to call `close()` to release resources when you are done with it.

`SquatchAndroid` returns [Reactive Streams](https://www.reactive-streams.org/) interfaces. Assuming you are using RxJava, then a typical API call made with this SDK would look something like this:

```java
Flowable.fromPublisher(squatchAndroid.widgetUpsert(
    WidgetUpsertInput.newBuilder()
        .setUserInputWithUserJwt(userJwt)
        .build(),
    null, AndroidRenderWidgetOptions.ofWebView(webView)))
    .subscribe();
```

In the code above, a widget upsert is performed asynchronously with the given `userJwt`, and the resulting widget is loaded into the given `webView` with the Android main thread.

## License

Unless explicitly stated otherwise all files in this repository are licensed under the Apache
License 2.0.

License boilerplate:

```
Copyright 2021 ReferralSaaSquatch.com Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
