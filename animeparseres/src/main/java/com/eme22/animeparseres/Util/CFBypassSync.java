package com.eme22.animeparseres.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.Model.AnimeError;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class CFBypassSync implements Callable<String> {

    private final String mUrl;
    private final WebView webView;

    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private static final Handler handler = new Handler();

    private volatile String cookies = null;

    @SuppressLint("SetJavaScriptEnabled")
    public CFBypassSync(Context context, String url) {
        mUrl = url;
        webView = new WebView(context);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                Log.d(TAG, "title");
                isbypass(title);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                result();
                return true;
            }
        });
        webView.getSettings().setUserAgentString(AnimeParserES.agent);
        webView.getSettings().setJavaScriptEnabled(true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public CFBypassSync(WebView webView, String url) {
        this.webView = webView;
        mUrl = url;
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                isbypass(title);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                result();
                return true;
            }
        });
        webView.getSettings().setUserAgentString(AnimeParserES.agent);
        webView.getSettings().setJavaScriptEnabled(true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public String call() {
        webView.post(() -> webView.loadUrl(mUrl));
        while (cookies == null ){
            try {
                Log.d(TAG,"are your sure that is has not been found?");
                webView.post(() ->isbypass(webView.getTitle()));
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return cookies;
    }

    private void isbypass(String title) {
        if (title == null) return;
        Log.d(TAG, title);
        if (title.contains("bot"))
            executorService.schedule(this::result,19000, TimeUnit.MILLISECONDS);
        else {
            result();
        }
    }

    private void destroyWebView() {
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
    }

    private void result() {
            executorService.shutdownNow();
            handler.removeCallbacks(null);
            destroyWebView();
            cookies = CookieManager.getInstance().getCookie(mUrl);
            Log.d(TAG, cookies);
    }
}
