package com.eme22.animeparseres.SitesSync.Special;

import android.content.Context;
import android.util.Log;
import android.webkit.CookieManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.AnimeParserES2;
import com.eme22.animeparseres.Model.AnimeError;
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

    @SuppressWarnings("unchecked")
    public static WebModel fetch(String url, String cookies) throws AnimeError {

        AnimeParserES2.getInstance().setFlvCookies(cookies);

        ANResponse<String> response = AndroidNetworking.get(url).addHeaders("cookie", cookies).setUserAgent(AnimeParserES.agent).build().executeForString();

        if (response.isSuccess()){
            return parse(response.getResult(), url);
        }else {
            Log.e(TAG, response.getError().getErrorBody());
            throw new AnimeError(response.getError().getErrorCode());
        }

    }


    @SuppressWarnings("unchecked")
    public static WebModel fetch(String url) throws AnimeError {
        ANRequest.GetRequestBuilder a = AndroidNetworking.get(url);
        String cookies = AnimeParserES2.getInstance().getFlvCookies();
        if (cookies != null) a.addHeaders("cookie" , cookies);
        ANResponse<String> response = a.setUserAgent(AnimeParserES.agent).build().executeForString();

        if (response.isSuccess()){
            return parse(response.getResult(), url);
        }else {
            throw new AnimeError(response.getError().getErrorCode());
        }

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
