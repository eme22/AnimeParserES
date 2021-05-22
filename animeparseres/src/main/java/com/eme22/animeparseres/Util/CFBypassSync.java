package com.eme22.animeparseres.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.eme22.animeparseres.AnimeParserES;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.eme22.animeparseres.AnimeParserES.TAG;
import static com.eme22.animeparseres.AnimeParserES.getInstance;

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
        executorService.schedule(this::result,19000, TimeUnit.MILLISECONDS);
        while (cookies == null || !cookies.contains("cf_clearance") ){
            try {
                Log.d(TAG,"are your sure that is has not been found?: ");
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
        Log.d(TAG, "Title: "+title);
        if (!(title.isEmpty() || title.contains("bot"))){
            result();
        }
    }

    private void destroyWebView() {
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.loadUrl("about:blank");
    }

    private void result() {
            executorService.shutdownNow();
            handler.removeCallbacks(null);
            destroyWebView();
            cookies = CookieManager.getInstance().getCookie(mUrl);
            //log(cookies);
            Log.d(TAG, cookies);
    }

    private void log(String data) {
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
