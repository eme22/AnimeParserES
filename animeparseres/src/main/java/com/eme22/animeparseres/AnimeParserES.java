package com.eme22.animeparseres;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.eme22.animeparseres.Model.Model;
import com.eme22.animeparseres.Model.WebModel;
import com.eme22.animeparseres.Sites.AnimeFLVAnime;
import com.eme22.animeparseres.Sites.AnimeFLVEpisode;
import com.eme22.animeparseres.Sites.AnimeFLVRUAnime;
import com.eme22.animeparseres.Sites.AnimeFLVRUEpisode;
import com.eme22.animeparseres.Sites.AnimeIDAnime;
import com.eme22.animeparseres.Sites.AnimeIDEpisode;
import com.eme22.animeparseres.Sites.AnimeJKAnime;
import com.eme22.animeparseres.Sites.AnimeJKEpisode;
import com.eme22.animeparseres.Sites.AnimeTioAnime;
import com.eme22.animeparseres.Sites.AnimeTioEpisode;
import com.eme22.animeparseres.Sites.Special.AnimeFLVBulk;
import com.eme22.animeparseres.Sites.Special.AnimeFLVRUBulk;
import com.eme22.animeparseres.Sites.Special.AnimeIDBulk;
import com.eme22.animeparseres.Sites.Special.AnimeJKBulk;
import com.eme22.animeparseres.Sites.Special.AnimeTioBulk;
import com.eme22.animeparseres.Util.BypassInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;

public class AnimeParserES {

    private static boolean animeIDAditionalLinks = false;
    private static boolean animeIDImageApi = false;

    private static String flvCookies = null;

    private final Context context;
    private OnTaskCompleted onComplete;
    private OnBulkTaskCompleted onBulkComplete;
    public static final String TAG = "animeParserES";
    public static final String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36 Edg/89.0.774.75";

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

    public AnimeParserES(Context context) {
        this.context = context;
        AndroidNetworking.initialize(context);
    }

    public AnimeParserES(Context context, OkHttpClient client) {
        this.context = context;
        AndroidNetworking.initialize(context, client);
    }

    public static boolean addAnimeIDAditionalLinks() {
        return animeIDAditionalLinks;
    }

    public static void setAnimeIDAditionalLinks(boolean animeIDAditionalLinksIn) {
        AnimeParserES.animeIDAditionalLinks = animeIDAditionalLinksIn;
    }

    public static boolean isAnimeIDImageApiEnabled() {
        return animeIDImageApi;
    }

    public static void setAnimeIDImageApi(boolean animeIDImageApi) {
        AnimeParserES.animeIDImageApi = animeIDImageApi;
    }

    public void load(String url){
        if (check(animeFLVEpisode,url)){
           AnimeFLVEpisode.fetch(context,url, onComplete);
        }
        else if (check(animeFLVAnime, url)){
            AnimeFLVAnime.fetch(context ,url,onComplete);
        }
        else if (check(animeJKEpisode,url)){
            AnimeJKEpisode.fetch(url, onComplete);
        }
        else if (check(animeJKAnime, url)){
            AnimeJKAnime.fetch(url,onComplete);
        }

        else if (check(animeIDEpisode,url)){
            AnimeIDEpisode.fetch(url, onComplete);
        }
        else if (check(animeIDAnime, url)){
            AnimeIDAnime.fetch(url,onComplete);
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
        else loadBulk(url);
    }

    public void loadBulk(String url){
        if (url.contains(animeFlV)){
            AnimeFLVBulk.fetch(context,url,onBulkComplete);
        }
        else if (url.contains(animeJK)){
            AnimeJKBulk.fetch(url,onBulkComplete);
        }
        else if (url.contains(animeID)){
            AnimeIDBulk.fetch(url,onBulkComplete);
        }
        /*
        else if (url.contains(animeTio)){
            AnimeTioBulk.fetch(url,onBulkComplete);
        }
        else if (url.contains(animeFLVRU)){
            AnimeFLVRUBulk.fetch(url,onBulkComplete);
        }

         */
        else onBulkComplete.onError();
    }

    /*
    @SuppressWarnings("deprecation")
    public static String escape(String text) {
        if (Build.VERSION.SDK_INT >= 24)
        {
           return Html.fromHtml(text , Html.FROM_HTML_MODE_LEGACY).toString();
        }
        else
        {
            return Html.fromHtml(text).toString();
        }
    }

     */
    public void onFinish(OnTaskCompleted onComplete) {
        this.onComplete = onComplete;
    }

    public void onFinish(OnBulkTaskCompleted onComplete) {
        this.onBulkComplete = onComplete;
    }

    private boolean check(String regex, String string) {
        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(string);
        boolean m = matcher.find();
        Log.d(TAG, String.valueOf(m));
        return m;
    }



    public interface OnTaskCompleted {
        void onTaskCompleted(Model vidURL,boolean episode);
        void onError();
        void onBypass(BypassInfo bypass);
    }

    public interface OnBulkTaskCompleted {
        void onTaskCompleted(WebModel animes);
        void onError();
        void onBypass(BypassInfo bypass);
    }
}
