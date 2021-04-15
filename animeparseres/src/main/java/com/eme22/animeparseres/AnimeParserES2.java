package com.eme22.animeparseres;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;

import com.androidnetworking.AndroidNetworking;
import com.eme22.animeparseres.Model.AnimeError;
import com.eme22.animeparseres.Model.Model;
import com.eme22.animeparseres.Model.WebModel;
import com.eme22.animeparseres.SitesSync.AnimeFLVAnime;
import com.eme22.animeparseres.SitesSync.AnimeFLVEpisode;
import com.eme22.animeparseres.SitesSync.AnimeIDAnime;
import com.eme22.animeparseres.SitesSync.AnimeIDEpisode;
import com.eme22.animeparseres.SitesSync.AnimeJKAnime;
import com.eme22.animeparseres.SitesSync.AnimeJKEpisode;
import com.eme22.animeparseres.SitesSync.Special.AnimeFLVBulk;
import com.eme22.animeparseres.SitesSync.Special.AnimeIDBulk;
import com.eme22.animeparseres.SitesSync.Special.AnimeJKBulk;
import com.eme22.animeparseres.Util.CFBypass;
import com.eme22.animeparseres.Util.CFBypassSync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import android.os.Handler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeParserES2 {

    private static AnimeParserES2 instance;

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean bypassByDefault = true;
    private boolean animeIDAditionalLinks = false;
    private WebView bypassWebView = null;

    private String flvCookies = null;


    private final String animeFlV = "https://www3.animeflv.net";
    private final String animeJK = "https://jkanime.net";
    private final String animeID = "https://www.animeid.tv";
    private final String animeTio = "https://tioanime.com";
    private final String animeFLVRU = "https://animeflv.ac";

    private final String animeFLVEpisode = "https?:\\/\\/(www?3\\.)?(animeflv\\.net)\\/(ver)\\/.+";
    private final String animeFLVAnime = "https?:\\/\\/(www?3\\.)?(animeflv\\.net)\\/(anime)\\/.+";

    private final String animeJKEpisode = "https?:\\/\\/(jkanime\\.net)\\/(.+)\\/\\d+(?=\\/)\\/";
    private final String animeJKAnime = "https?:\\/\\/(jkanime\\.net)\\/(?:(?!genero|buscar|letra).+)\\/";

    private final String animeIDEpisode = "https?:\\/\\/(www\\.)?(animeid\\.tv)\\/(v)\\/.+";
    private final String animeIDAnime = "https?:\\/\\/(www\\.)?(animeid\\.tv)\\/(?:(?!genero|buscar|letra|peliculas|series|ovas).+)";

    private final String animeTioEpisode = "https?:\\/\\/(tioanime\\.com)\\/(ver)\\/.+";
    private final String animeTioAnime = "https?:\\/\\/(tioanime\\.com)\\/(anime)\\/.+";

    private final String animeFLVRUEpisode = "https?:\\/\\/(animeflv\\.ac)\\/(ver)\\/\\d+(?=\\/)\\/(.*)";
    private final String animeFlvRuAnime = "https?:\\/\\/(animeflv\\.ac)\\/(anime)\\/\\d+(?=\\/)\\/(.*)";

    public static AnimeParserES2 getInstance() {
        if (instance == null) {
            synchronized (AnimeParserES2.class) {
                if (instance == null) {
                    if (AnimeParserContentProvider.context == null) {
                        throw new IllegalStateException("context == null");
                    }
                    instance = new AnimeParserES2(AnimeParserContentProvider.context);
                }
            }
        }
        return instance;
    }

    public Context getContext(){
        return AnimeParserContentProvider.context;
    }

    public AnimeParserES2(Context context) {
        AndroidNetworking.initialize(context);
    }

    public boolean isAnimeIDAditionalLinksEnabled() {
        return animeIDAditionalLinks;
    }

    public void setAnimeIDAditionalLinks(boolean animeIDAditionalLinks) {
        this.animeIDAditionalLinks = animeIDAditionalLinks;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public boolean isBypassEnabledByDefault() {
        return bypassByDefault;
    }

    public void setBypassEnabledByDefault(boolean bypassByDefault) {
        this.bypassByDefault = bypassByDefault;
    }

    public String getFlvCookies() {
        return flvCookies;
    }

    public void setFlvCookies(String flvCookies) {
        this.flvCookies = flvCookies;
    }

    public void setBypassWebView(WebView bypassWebView) {
        this.bypassWebView = bypassWebView;
    }

    private boolean check(String regex, String url) {
        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(url);
        boolean m = matcher.find();
        Log.d(TAG, String.valueOf(m));
        return m;
    }

    public void getForSingle(String url, OnTaskCompleted onTaskCompleted) throws AnimeError {
        if (check(animeFLVEpisode,url)){
            try {
                onTaskCompleted.onTaskCompleted(AnimeFLVEpisode.fetch(url));
            } catch (AnimeError error){
                if (error.getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                    CFBypass.init(url, cookies -> {
                        try {
                            onTaskCompleted.onTaskCompleted(AnimeFLVEpisode.fetch(url, cookies));
                        } catch (AnimeError animeError) {
                            onTaskCompleted.onError(animeError);
                        }
                    });
                }
                else onTaskCompleted.onError(error);
            }
        }
        else if (check(animeFLVAnime, url)){
            try {
                onTaskCompleted.onTaskCompleted(AnimeFLVAnime.fetch(url));
            } catch (AnimeError error){
                if (error.getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                    CFBypass.init(url, cookies -> {
                        try {
                            onTaskCompleted.onTaskCompleted(AnimeFLVAnime.fetch(url, cookies));
                        } catch (AnimeError animeError) {
                            onTaskCompleted.onError(animeError);
                        }
                    });
                }
                else onTaskCompleted.onError(error);
            }
        }
        else if (check(animeJKEpisode,url)){
            onTaskCompleted.onTaskCompleted(AnimeJKEpisode.fetch(url));
        }
        else if (check(animeJKAnime, url)){
            onTaskCompleted.onTaskCompleted(AnimeJKAnime.fetch(url));
        }

        else if (check(animeIDEpisode,url)){
            onTaskCompleted.onTaskCompleted(AnimeIDEpisode.fetch(url));
        }
        else if (check(animeIDAnime, url)){
            onTaskCompleted.onTaskCompleted(AnimeIDAnime.fetch(url));
        }
        /*
        else if (check(animeTioEpisode,url)){
            AnimeTioEpisode.fetch(url, onComplete);
        }
        else if (check(animeTioAnime, url)){
            AnimeTioAnime.fetch(url,onComplete);
        }
        else if (check(animeFLVRUEpisode,url)){
            AnimeFLVRUEpisode.fetch(url, onComplete);
        }
        else if (check(animeFlvRuAnime, url)){
            AnimeFLVRUAnime.fetch(url,onComplete);
        }
        */
        else onTaskCompleted.onError(new AnimeError());
    }

    public void getForWebsite(String url, OnTaskCompleted onTaskCompleted) throws AnimeError {
        if (url.contains(animeFlV)){
            try {
                onTaskCompleted.onTaskCompleted(AnimeFLVBulk.fetch(url));
            } catch (AnimeError error){
                if (error.getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                    CFBypass.init(url, cookies -> {
                        try {
                            onTaskCompleted.onTaskCompleted(AnimeFLVBulk.fetch(url, cookies));
                        } catch (AnimeError animeError) {
                            onTaskCompleted.onError(animeError);
                        }
                    });
                }
                else onTaskCompleted.onError(error);
            }
        }
        else if (url.contains(animeJK)){
            onTaskCompleted.onTaskCompleted(AnimeJKBulk.fetch(url));
        }
        else if (url.contains(animeID)){
            onTaskCompleted.onTaskCompleted(AnimeIDBulk.fetch(url));
        }
        /*
        else if (url.contains(animeTio)){
            AnimeTioBulk.fetch(url,onBulkComplete);
        }
        else if (url.contains(animeFLVRU)){
            AnimeFLVRUBulk.fetch(url,onBulkComplete);
        }

         */
        else onTaskCompleted.onError(new AnimeError());
    }


    public Model executeForSingle(String url) throws AnimeError, ExecutionException, InterruptedException {
        if (check(animeFLVEpisode,url)){
            try {
                return AnimeFLVEpisode.fetch(url);
            } catch (AnimeError error){
                if (error.getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                    final CFBypassSync[] bypassSync = {null};
                    new Handler(Looper.getMainLooper()).post(() -> bypassSync[0] = new CFBypassSync(bypassWebView,url));
                    while (bypassSync[0] == null) {Thread.sleep(100);}
                    Future<String> cookieManagerFuture = executorService.submit(bypassSync[0]);
                    String cookieManager = cookieManagerFuture.get();
                    return AnimeFLVEpisode.fetch(url, cookieManager);
                }
                else throw error;
            }
        }
        else if (check(animeFLVAnime, url)){
            try {
                return AnimeFLVAnime.fetch(url);
            } catch (AnimeError error){
                if (error.getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                    final CFBypassSync[] bypassSync = {null};
                    new Handler(Looper.getMainLooper()).post(() -> bypassSync[0] = new CFBypassSync(bypassWebView,url));
                    while (bypassSync[0] == null) {Thread.sleep(100);}
                    Future<String> cookieManagerFuture = executorService.submit(bypassSync[0]);
                    String cookieManager = cookieManagerFuture.get();
                    return AnimeFLVAnime.fetch(url, cookieManager);
                }
                else throw error;
            }
        }
        else if (check(animeJKEpisode,url)){
            return AnimeJKEpisode.fetch(url);
        }
        else if (check(animeJKAnime, url)){
            return AnimeJKAnime.fetch(url);
        }

        else if (check(animeIDEpisode,url)){
            return AnimeIDEpisode.fetch(url);
        }
        else if (check(animeIDAnime, url)){
            return AnimeIDAnime.fetch(url);
        }
        /*
        else if (check(animeTioEpisode,url)){
            AnimeTioEpisode.fetch(url, onComplete);
        }
        else if (check(animeTioAnime, url)){
            AnimeTioAnime.fetch(url,onComplete);
        }
        else if (check(animeFLVRUEpisode,url)){
            AnimeFLVRUEpisode.fetch(url, onComplete);
        }
        else if (check(animeFlvRuAnime, url)){
            AnimeFLVRUAnime.fetch(url,onComplete);
        }
        */
        else throw new AnimeError();
    }

    public WebModel executeForWebSite(String url) throws AnimeError, ExecutionException, InterruptedException, TimeoutException {
        if (url.contains(animeFlV)){
            try {
                return AnimeFLVBulk.fetch(url);
            } catch (AnimeError error){
                if (error.getErrorCode() == 503 && bypassByDefault && bypassWebView != null){

                    final CFBypassSync[] bypassSync = {null};
                    new Handler(Looper.getMainLooper()).post(() -> bypassSync[0] = new CFBypassSync(bypassWebView,url));
                    while (bypassSync[0] == null) {Thread.sleep(100);}
                    Future<String> cookieManagerFuture = executorService.submit(bypassSync[0]);
                    String cookieManager = cookieManagerFuture.get();
                    return AnimeFLVBulk.fetch(url, cookieManager);
                }
                else throw error;
            }
        }
        else if (url.contains(animeJK)){
            return AnimeJKBulk.fetch(url);
        }
        else if (url.contains(animeID)){
            return AnimeIDBulk.fetch(url);
        }
        /*
        else if (url.contains(animeTio)){
            AnimeTioBulk.fetch(url,onBulkComplete);
        }
        else if (url.contains(animeFLVRU)){
            AnimeFLVRUBulk.fetch(url,onBulkComplete);
        }

         */
        else throw new AnimeError();
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(Object animes);
        void onError(AnimeError error);
    }

}
