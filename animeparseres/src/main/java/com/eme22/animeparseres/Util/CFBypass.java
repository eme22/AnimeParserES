package com.eme22.animeparseres.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.eme22.animeparseres.AnimeParserES;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static com.eme22.animeparseres.AnimeParserES.getInstance;

public class CFBypass {

    private static final String TAG = "CFBypass";
    @SuppressLint("StaticFieldLeak")
    private static WebView webView;
    private static onResult onResult;
    private static int currentTry = 0;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Runnable timeout = new Runnable() {
        @Override
        public void run() {
            result(webView.getUrl());
        }
    };


    @SuppressLint("SetJavaScriptEnabled")
    public static void init(String url, final onResult onDone ){
        Log.d("BYPASS", "INITIALIZING BYPASS");
        onResult = onDone;
        webView = new WebView(AnimeParserES.getInstance().getContext());
        WebSettings myWebSettings = webView.getSettings();
        myWebSettings.setUserAgentString(AnimeParserES.agent);
        webView.getSettings().setJavaScriptEnabled(true);
        /*
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                isbypass();
            }
        });
         */
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                isbypass();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                result(webView.getUrl());
                return true;
            }

        });
        currentTry = 0;
        webView.loadUrl(url);
        handler.postDelayed(timeout,10000);
    }

    private static void isbypass() {
        String title = webView.getTitle();
        if (!(title.isEmpty() || title.contains("bot"))){
            result(webView.getUrl());
        }
    }

    private static void destroyWebView() {
        //we.removeAllViews();
        if (webView != null){
            /*
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            webView.clearHistory();
            webView.clearCache(true);
            webView.onPause();
            webView.removeAllViews();
            webView.destroyDrawingCache();
            webView.pauseTimers();
             */
            webView.destroy();
            webView = null;
        }
    }

    private static void result(String url) {
        handler.removeCallbacks(timeout);
        Log.d(TAG, "Current Try: "+currentTry);
        Log.d(TAG,"Fucked: " + url);
        String cookies = CookieManager.getInstance().getCookie(url);
        log(cookies);
        if (cookies.contains("cf_clearance") && url.length() > 26){
            destroyWebView();
            Log.d(TAG,"Bypass Successful");
            currentTry = 0;
            onResult.onCookieGrab(cookies);
        }
        else {
            if (currentTry <= 2) {
                currentTry++;
                init(url,onResult);
            }
            else {
                destroyWebView();
                onResult.onFailure();
            }
        }
    }

    public interface onResult{

        void onCookieGrab(String cookies);
        void onFailure();

    }

    private static void log(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getInstance().getContext().openFileOutput("cf.log", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.getLocalizedMessage());
        }
    }


}
