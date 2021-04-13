package com.eme22.animeparseres.Sites.Special;

import android.content.Context;
import android.util.Log;
import android.webkit.CookieManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.Model.MiniModel;
import com.eme22.animeparseres.Model.Model;
import com.eme22.animeparseres.Model.WebModel;
import com.eme22.animeparseres.Util.BypassInfo;
import com.eme22.animeparseres.Util.CFBypass;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeFLVBulk {

    private static final String oldPrefix = "https://animeflv.net";
    private static final String urlPrefix = "https://www3.animeflv.net";
    private static final BypassInfo bp = new BypassInfo();

    public static void fetch(String url, CookieManager cookies, AnimeParserES.OnBulkTaskCompleted onBulkComplete) {

        String cookie = cookies.getCookie(url);

        AnimeParserES.setFlvCookies(cookie);

        bp.setCookie(cookie);

        Log.d("AAAAAAAA", cookie);

        AndroidNetworking.get(url).addHeaders("cookie", cookie).setUserAgent(AnimeParserES.agent).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                bp.setIsBypassing(BypassInfo.BypasStatus.SUCCEED);
                onBulkComplete.onBypass(bp);
                WebModel model = parse(response, url);
                onBulkComplete.onTaskCompleted(model);
            }

            @Override
            public void onError(ANError anError) {
                onBulkComplete.onError();
            }
        });
    }


    public static void fetch(Context context, String url, AnimeParserES.OnBulkTaskCompleted onBulkComplete) {
        ANRequest.GetRequestBuilder a = AndroidNetworking.get(url);
        String cookies = AnimeParserES.getFlvCookies();
        if (cookies != null) a.addHeaders("cookie" , cookies);
        a.setUserAgent(AnimeParserES.agent).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                bp.setIsBypassing(BypassInfo.BypasStatus.NOT_NEEDED);
                onBulkComplete.onBypass(bp);
                WebModel model = parse(response, url);
                onBulkComplete.onTaskCompleted(model);
            }

            @Override
            public void onError(ANError anError) {
                Log.d(TAG, String.valueOf(anError.getErrorCode()));
                if (anError.getErrorCode() == 503) {
                    bp.setIsBypassing(BypassInfo.BypasStatus.BYPASSING);
                    onBulkComplete.onBypass(bp);
                    CFBypass.init(context, url, cookies -> fetch(url, cookies, onBulkComplete));
                }
                else onBulkComplete.onError();
            }
        });
    }

    private static WebModel parse(String response, String url) {

        WebModel data = new WebModel();
        data.setServer(Model.SERVER.ANIMEFLV);

        String name = "main";
        if (url.contains("genre=")) name = url.split("genre=")[1];

        Document document = Jsoup.parse(response);
        ArrayList<MiniModel> animes = new ArrayList<>();
        ArrayList<MiniModel> episodes = new ArrayList<>();
        Elements episodeLinks = document.select("a[class=fa-play]");
        Elements animeLinks = document.select("article[class=Anime alt B]");
        for (Element link: episodeLinks) {
            MiniModel episode = new MiniModel();
            String link2 = link.attr("href");
            if (!link2.contains(urlPrefix)) link2 = urlPrefix + link2;
            String title = link.select("strong[class=Title]").text();
            String image = link.select("img").attr("src");
            if (image.contains(oldPrefix)) image = image.replace(oldPrefix,urlPrefix);
            else if (!image.contains(urlPrefix)) image = urlPrefix + image;
            int episodeNum = Integer.parseInt(link.select("span[class=Capi]").text().replaceAll("[^\\d.]", ""));
            episode.setTitle(title);
            episode.setEpisode(episodeNum);
            episode.setImage(image);
            episode.setLink(link2);
            if (!episodes.contains(episode)) episodes.add(episode);
        }
        for (Element link: animeLinks) {
            MiniModel anime = new MiniModel();
            String link2 = link.select("a").attr("href");
            if (!link2.contains(urlPrefix)) link2 = urlPrefix + link2;
            String title = link.select("h3[class=Title]").text();
            String image = link.select("img").attr("src");
            if (image.contains(oldPrefix)) image = image.replace(oldPrefix,urlPrefix);
            else if (!image.contains(urlPrefix)) image = urlPrefix + image;
            String desc = link.select("div[class=Description]").select("p").get(1).text();
            String type = document.select("span[class*=Type]").text();
            anime.setLink(link2);
            anime.setTitle(title);
            anime.setImage(image);
            anime.setDescription(desc);
            if (type.contains("Anime")) anime.setType(Model.TYPES.ANIME);
            else if (type.contains("Película")) anime.setType(Model.TYPES.PELICULA);
            else if (type.contains("OVA")) anime.setType(Model.TYPES.OVA);
            else if (type.contains("Especial")) anime.setType(Model.TYPES.ESPECIAL);
            if (title != null && title.contains("Latino")) anime.setType(Model.TYPES.ESPANIOL);
            if (!animes.contains(anime)) animes.add(anime);
        }

        data.setName(name);
        data.setAnimes(animes);
        data.setEpisodes(episodes);

        return data;

    }
}
