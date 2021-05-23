package com.eme22.animeparseres;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;

import com.androidnetworking.AndroidNetworking;
import com.eme22.animeparseres.Model.AnimeError;
import com.eme22.animeparseres.Model.AnimeResponse;
import com.eme22.animeparseres.Model.Model;
import com.eme22.animeparseres.Model.ServerNotFoundException;
import com.eme22.animeparseres.Model.WebModel;
import com.eme22.animeparseres.Sites.AnimeFLVAnime;
import com.eme22.animeparseres.Sites.AnimeFLVEpisode;
import com.eme22.animeparseres.Sites.AnimeIDAnime;
import com.eme22.animeparseres.Sites.AnimeIDEpisode;
import com.eme22.animeparseres.Sites.AnimeJKAnime;
import com.eme22.animeparseres.Sites.AnimeJKEpisode;
import com.eme22.animeparseres.Sites.Special.AnimeFLVBulk;
import com.eme22.animeparseres.Sites.Special.AnimeIDBulk;
import com.eme22.animeparseres.Sites.Special.AnimeJKBulk;
import com.eme22.animeparseres.Util.CFBypass;
import com.eme22.animeparseres.Util.CFBypassSync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class AnimeParserES {

    public static final String TAG = "AnimeParserES";
    public static final String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36 Edg/90.0.818.62";
    @SuppressLint("StaticFieldLeak")
    private static AnimeParserES instance;

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

    //private final String animeJKEpisode = "https?:\\/\\/(jkanime\\.net)\\/(.+)\\/\\d+(?=\\/)\\/";
    private final String animeJKEpisode = "https?:\\/\\/(jkanime\\.net)\\/(.+)\\/(pelicula|\\d+(?=\\/))\\/";
    private final String animeJKAnime = "https?:\\/\\/(jkanime\\.net)\\/(?:(?!genero|buscar|letra).+)\\/";

    private final String animeIDEpisode = "https?:\\/\\/(www\\.)?(animeid\\.tv)\\/(v)\\/.+";
    private final String animeIDAnime = "https?:\\/\\/(www\\.)?(animeid\\.tv)\\/(?:(?!genero|buscar|letra|peliculas|series|ovas).+)";

    private final String animeTioEpisode = "https?:\\/\\/(tioanime\\.com)\\/(ver)\\/.+";
    private final String animeTioAnime = "https?:\\/\\/(tioanime\\.com)\\/(anime)\\/.+";

    private final String animeFLVRUEpisode = "https?:\\/\\/(animeflv\\.ac)\\/(ver)\\/\\d+(?=\\/)\\/(.*)";
    private final String animeFlvRuAnime = "https?:\\/\\/(animeflv\\.ac)\\/(anime)\\/\\d+(?=\\/)\\/(.*)";

    public static AnimeParserES getInstance() {
        if (instance == null) {
            synchronized (AnimeParserES.class) {
                if (instance == null) {
                    if (AnimeParserContentProvider.context == null) {
                        throw new IllegalStateException("context == null");
                    }
                    instance = new AnimeParserES(AnimeParserContentProvider.context);
                }
            }
        }
        return instance;
    }

    public Context getContext(){
        return AnimeParserContentProvider.context;
    }

    private AnimeParserES(Context context) {
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


    public void getForSingle(String url, OnTaskCompleted onTaskCompleted) {
        if (check(animeFLVEpisode,url)){
            AnimeFLVEpisode.fetch(url, new OnTaskCompleted() {
                @Override
                public void onTaskCompleted(Object animes) {
                    onTaskCompleted.onTaskCompleted(animes);
                }
                @Override
                public void onError(AnimeError error) {
                    if (error.getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                        CFBypass.init(url, new CFBypass.onResult() {
                            @Override
                            public void onCookieGrab(String cookies) {
                                AnimeFLVEpisode.fetch(url, cookies,onTaskCompleted);
                            }

                            @Override
                            public void onFailure() {
                                onTaskCompleted.onError(error);
                            }
                        });
                    }
                    else onTaskCompleted.onError(error);
                }
            });
        }
        else if (check(animeFLVAnime, url)){
            AnimeFLVAnime.fetch(url, new OnTaskCompleted() {
                @Override
                public void onTaskCompleted(Object animes) {
                    onTaskCompleted.onTaskCompleted(animes);
                }
                @Override
                public void onError(AnimeError error) {
                    if (error.getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                        CFBypass.init(url, new CFBypass.onResult() {
                            @Override
                            public void onCookieGrab(String cookies) {
                            AnimeFLVAnime.fetch(url, cookies,onTaskCompleted);
                            }

                            @Override
                            public void onFailure() {
                                onTaskCompleted.onError(error);
                            }
                        });
                    }
                    else onTaskCompleted.onError(error);
                }
            });
        }
        else if (check(animeJKEpisode,url)){
            AnimeJKEpisode.fetch(url, onTaskCompleted);
        }
        else if (check(animeJKAnime, url)){
            AnimeJKAnime.fetch(url,onTaskCompleted);
        }
        else if (check(animeIDEpisode,url)){
            AnimeIDEpisode.fetch(url, onTaskCompleted);
        }
        else if (check(animeIDAnime, url)){
            AnimeIDAnime.fetch(url, onTaskCompleted);
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
        else onTaskCompleted.onError(new AnimeError(new ServerNotFoundException()));
    }

    public void getForWebsite(String url, OnTaskCompleted onTaskCompleted) {
        if (url.contains(animeFlV)){
            AnimeFLVBulk.fetch(url, new OnTaskCompleted(){
                @Override
                public void onTaskCompleted(Object animes) {
                    onTaskCompleted.onTaskCompleted(animes);
                }
                @Override
                public void onError(AnimeError error) {
                    if (error.getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                        CFBypass.init(url, new CFBypass.onResult() {
                            @Override
                            public void onCookieGrab(String cookies) {
                                AnimeFLVBulk.fetch(url, cookies,onTaskCompleted);
                            }

                            @Override
                            public void onFailure() {
                                onTaskCompleted.onError(error);
                            }
                        });
                    }
                    else onTaskCompleted.onError(error);
                }
            });
        }
        else if (url.contains(animeJK)){
            AnimeJKBulk.fetch(url, onTaskCompleted);
        }
        else if (url.contains(animeID)){
            AnimeIDBulk.fetch(url, onTaskCompleted);
        }
        /*
        else if (url.contains(animeTio)){
            AnimeTioBulk.fetch(url,onBulkComplete);
        }
        else if (url.contains(animeFLVRU)){
            AnimeFLVRUBulk.fetch(url,onBulkComplete);
        }

         */
        else onTaskCompleted.onError(new AnimeError(new ServerNotFoundException()));
    }


    public AnimeResponse<Model> executeForSingle(String url) {
        if (check(animeFLVEpisode,url)){
            AnimeResponse<Model> response = AnimeFLVEpisode.fetch(url);
            if (!response.isSuccess() && response.getmError().getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                final CFBypassSync[] bypassSync = {null};
                new Handler(Looper.getMainLooper()).post(() -> bypassSync[0] = new CFBypassSync(bypassWebView,url));
                while (bypassSync[0] == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return new AnimeResponse<>(new AnimeError(e));
                    }
                }
                Future<String> cookieManagerFuture = executorService.submit(bypassSync[0]);
                String cookieManager;
                try {
                    cookieManager = cookieManagerFuture.get();
                } catch (ExecutionException e) {
                    return new AnimeResponse<>(new AnimeError(e));
                } catch (InterruptedException e) {
                    return new AnimeResponse<>(new AnimeError(e));
                }
                response = AnimeFLVEpisode.fetch(url,cookieManager);
            }
            return response;
        }
        else if (check(animeFLVAnime, url)){
            AnimeResponse<Model> response = AnimeFLVAnime.fetch(url);
            if (!response.isSuccess() && response.getmError().getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                final CFBypassSync[] bypassSync = {null};
                new Handler(Looper.getMainLooper()).post(() -> bypassSync[0] = new CFBypassSync(bypassWebView,url));
                while (bypassSync[0] == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return new AnimeResponse<>(new AnimeError(e));
                    }
                }
                Future<String> cookieManagerFuture = executorService.submit(bypassSync[0]);
                String cookieManager;
                try {
                    cookieManager = cookieManagerFuture.get();
                } catch (ExecutionException e) {
                    return new AnimeResponse<>(new AnimeError(e));
                } catch (InterruptedException e) {
                    return new AnimeResponse<>(new AnimeError(e));
                }
                response = AnimeFLVAnime.fetch(url,cookieManager);
            }
            return response;
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
        else return new AnimeResponse<>(new AnimeError(new ServerNotFoundException()));
    }

    public AnimeResponse<WebModel> executeForWebSite(String url) {
        if (url.contains(animeFlV)){
            AnimeResponse<WebModel> response = AnimeFLVBulk.fetch(url);
            if (!response.isSuccess() && response.getmError().getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                final CFBypassSync[] bypassSync = {null};
                new Handler(Looper.getMainLooper()).post(() -> bypassSync[0] = new CFBypassSync(bypassWebView,url));
                while (bypassSync[0] == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return new AnimeResponse<>(new AnimeError(e));
                    }
                }
                Future<String> cookieManagerFuture = executorService.submit(bypassSync[0]);
                String cookieManager;
                try {
                    cookieManager = cookieManagerFuture.get();
                } catch (ExecutionException e) {
                    return new AnimeResponse<>(new AnimeError(e));
                } catch (InterruptedException e) {
                    return new AnimeResponse<>(new AnimeError(e));
                }
                response = AnimeFLVBulk.fetch(url,cookieManager);
            }
            return response;
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
        else return new AnimeResponse<>(new AnimeError(new ServerNotFoundException()));
    }

    public void getAsync(String url, OnTaskCompleted onTaskCompleted) {
        if (check(animeFLVEpisode,url)){
            AnimeFLVEpisode.fetch(url, new OnTaskCompleted() {
                @Override
                public void onTaskCompleted(Object animes) {
                    onTaskCompleted.onTaskCompleted(animes);
                }
                @Override
                public void onError(AnimeError error) {
                    if (error.getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                        CFBypass.init(url, new CFBypass.onResult() {
                            @Override
                            public void onCookieGrab(String cookies) {
                            AnimeFLVEpisode.fetch(url, cookies,onTaskCompleted);
                            }

                            @Override
                            public void onFailure() {
                                onTaskCompleted.onError(error);
                            }
                        });
                    }
                    else onTaskCompleted.onError(error);
                }
            });
        }
        else if (check(animeFLVAnime, url)){
            AnimeFLVAnime.fetch(url, new OnTaskCompleted() {
                @Override
                public void onTaskCompleted(Object animes) {
                    onTaskCompleted.onTaskCompleted(animes);
                }
                @Override
                public void onError(AnimeError error) {
                    if (error.getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                        CFBypass.init(url, new CFBypass.onResult() {
                            @Override
                            public void onCookieGrab(String cookies) {
                                AnimeFLVAnime.fetch(url, cookies,onTaskCompleted);
                            }

                            @Override
                            public void onFailure() {
                                onTaskCompleted.onError(error);
                            }
                        });
                    }
                    else onTaskCompleted.onError(error);
                }
            });
        }
        else if (check(animeJKEpisode,url)){
            AnimeJKEpisode.fetch(url, onTaskCompleted);
        }
        else if (check(animeJKAnime, url)){
            AnimeJKAnime.fetch(url,onTaskCompleted);
        }
        else if (check(animeIDEpisode,url)){
            AnimeIDEpisode.fetch(url, onTaskCompleted);
        }
        else if (check(animeIDAnime, url)){
            AnimeIDAnime.fetch(url, onTaskCompleted);
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
        else if (url.contains(animeFlV)){
            AnimeFLVBulk.fetch(url, new OnTaskCompleted(){
                @Override
                public void onTaskCompleted(Object animes) {
                    onTaskCompleted.onTaskCompleted(animes);
                }
                @Override
                public void onError(AnimeError error) {
                    if (error.getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                        CFBypass.init(url, new CFBypass.onResult() {
                            @Override
                            public void onCookieGrab(String cookies) {
                            AnimeFLVBulk.fetch(url, cookies,onTaskCompleted);
                            }

                            @Override
                            public void onFailure() {
                                onTaskCompleted.onError(error);
                            }
                        });
                    }
                    else onTaskCompleted.onError(error);
                }
            });
        }
        else if (url.contains(animeJK)){
            AnimeJKBulk.fetch(url, onTaskCompleted);
        }
        else if (url.contains(animeID)){
            AnimeIDBulk.fetch(url, onTaskCompleted);
        }
        /*
        else if (url.contains(animeTio)){
            AnimeTioBulk.fetch(url,onBulkComplete);
        }
        else if (url.contains(animeFLVRU)){
            AnimeFLVRUBulk.fetch(url,onBulkComplete);
        }

         */
        else onTaskCompleted.onError(new AnimeError(new ServerNotFoundException()));
    }

    public AnimeResponse getSync(String url) {
        if (check(animeFLVEpisode,url)){
            AnimeResponse<Model> response = AnimeFLVEpisode.fetch(url);
            if (!response.isSuccess() && response.getmError().getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                final CFBypassSync[] bypassSync = {null};
                new Handler(Looper.getMainLooper()).post(() -> bypassSync[0] = new CFBypassSync(bypassWebView,url));
                while (bypassSync[0] == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return new AnimeResponse<Model>(new AnimeError(e));
                    }
                }
                Future<String> cookieManagerFuture = executorService.submit(bypassSync[0]);
                String cookieManager;
                try {
                    cookieManager = cookieManagerFuture.get();
                } catch (ExecutionException e) {
                    return new AnimeResponse<Model>(new AnimeError(e));
                } catch (InterruptedException e) {
                    return new AnimeResponse<Model>(new AnimeError(e));
                }
                response = AnimeFLVEpisode.fetch(url,cookieManager);
            }
            return response;
        }
        else if (check(animeFLVAnime, url)){
            AnimeResponse<Model> response = AnimeFLVAnime.fetch(url);
            if (!response.isSuccess() && response.getmError().getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                final CFBypassSync[] bypassSync = {null};
                new Handler(Looper.getMainLooper()).post(() -> bypassSync[0] = new CFBypassSync(bypassWebView,url));
                while (bypassSync[0] == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return new AnimeResponse<Model>(new AnimeError(e));
                    }
                }
                Future<String> cookieManagerFuture = executorService.submit(bypassSync[0]);
                String cookieManager;
                try {
                    cookieManager = cookieManagerFuture.get();
                } catch (ExecutionException e) {
                    return new AnimeResponse<Model>(new AnimeError(e));
                } catch (InterruptedException e) {
                    return new AnimeResponse<Model>(new AnimeError(e));
                }
                response = AnimeFLVAnime.fetch(url,cookieManager);
            }
            return response;
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
        else if (url.contains(animeFlV)){
            AnimeResponse<WebModel> response = AnimeFLVBulk.fetch(url);
            if (!response.isSuccess() && response.getmError().getError().getErrorCode() == 503 && bypassByDefault && bypassWebView != null){
                final CFBypassSync[] bypassSync = {null};
                new Handler(Looper.getMainLooper()).post(() -> bypassSync[0] = new CFBypassSync(bypassWebView,url));
                while (bypassSync[0] == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return new AnimeResponse<>(new AnimeError(e));
                    }
                }
                Future<String> cookieManagerFuture = executorService.submit(bypassSync[0]);
                String cookieManager;
                try {
                    cookieManager = cookieManagerFuture.get();
                } catch (ExecutionException e) {
                    return new AnimeResponse<>(new AnimeError(e));
                } catch (InterruptedException e) {
                    return new AnimeResponse<>(new AnimeError(e));
                }
                response = AnimeFLVBulk.fetch(url,cookieManager);
            }
            return response;
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
        else return new AnimeResponse<>(new AnimeError(new ServerNotFoundException()));
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(Object animes);
        void onError(AnimeError error);
    }

}
