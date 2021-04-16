package com.eme22.animeparseres.Util;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.eme22.animeparseres.AnimeParserES;

public class CFBypass {

    private static final String TAG = "CFBypass";
    @SuppressLint("StaticFieldLeak")
    private static WebView webView;
    private static onResult onResult;

    private static final Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint("SetJavaScriptEnabled")
    public static void init(String url, final onResult onDone ){

        onResult = onDone;
        webView = new WebView(AnimeParserES.getInstance().getContext());
        WebSettings myWebSettings = webView.getSettings();
        myWebSettings.setUserAgentString(AnimeParserES.agent);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                isbypass();
            }
        });
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                result(webView.getUrl());
                return true;
            }

        });

        webView.loadUrl(url);

    }

    private static void isbypass() {
        if (webView.getTitle() == null) return;
        if (webView.getTitle().contains("bot"))
            handler.postDelayed(() -> result(webView.getUrl()), 19500);
        else {
            result(webView.getUrl());
        }
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

        handler.removeCallbacks(null);
        destroyWebView();
        Log.d(TAG,"Fucked: " + url);

        String cookies = CookieManager.getInstance().getCookie(url);
        onResult.onCookieGrab(cookies);
    }

    public interface onResult{

        void onCookieGrab(String cookies);

    }


}
