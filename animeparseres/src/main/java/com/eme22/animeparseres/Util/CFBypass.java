package com.eme22.animeparseres.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.eme22.animeparseres.AnimeParserES;

public class CFBypass {

    private static final String TAG = "CFBypass";
    private static WebView webView;
    private static onResult onResult;
    private static DelayedAction cancel;
    private static DelayedAction judge;

    @SuppressLint("SetJavaScriptEnabled")
    public static void init(WebView webView2, String url, final onResult onDone ){
        onResult = onDone;
        webView = webView2;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                result(webView.getUrl());
                return true;
            }
        });

        webView.loadUrl(url);

        judge = new DelayedAction(() -> isbypass(), 500);


    }

    private static void isbypass() {
        if (webView.getTitle().contains("bot")) cancel = new DelayedAction(() -> {
            result(webView.getUrl());
        },20000-500);
        else {
            result(webView.getUrl());
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void init(Context context, String url, final onResult onDone ){

        onResult = onDone;
        webView = new WebView(context);
        WebSettings myWebSettings = webView.getSettings();
        myWebSettings.setUserAgentString(AnimeParserES.agent);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                result(webView.getUrl());
                return true;
            }

        });

        webView.loadUrl(url);

        judge = new DelayedAction(CFBypass::isbypass, 500);
    }

    private static void destroyWebView() {
        //we.removeAllViews();
        webView.clearHistory();
        webView.clearCache(true);
        webView.onPause();
        webView.removeAllViews();
        webView.destroyDrawingCache();
        webView.pauseTimers();
        webView.destroy();
        webView = null;
    }

    private static void result(String url) {

        if (cancel != null) cancel.cancel();
        destroyWebView();
        Log.d(TAG,"Fucked: " + url);

        CookieManager cookies = CookieManager.getInstance();
        onResult.onCookieGrab(cookies);
    }

    public interface onResult{

        void onCookieGrab(CookieManager cookieJar);

    }


}
