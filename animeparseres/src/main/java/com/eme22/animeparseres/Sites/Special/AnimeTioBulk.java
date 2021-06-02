package com.eme22.animeparseres.Sites.Special;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.Model.AnimeError;
import com.eme22.animeparseres.Model.AnimeResponse;
import com.eme22.animeparseres.Model.MiniModel;
import com.eme22.animeparseres.Model.Model;
import com.eme22.animeparseres.Model.WebModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeTioBulk {
    private static final String urlPrefix = "https://tioanime.com";

    @SuppressWarnings("unchecked")
    public static AnimeResponse<WebModel> fetch(String url) {
        Log.d(TAG, "Requesting: "+url);
        ANRequest.GetRequestBuilder a = AndroidNetworking.get(url);
        String cookies = AnimeParserES.getInstance().getFlvCookies();
        if (cookies != null) a.addHeaders("cookie" , cookies);
        ANResponse<String> response = a.setUserAgent(AnimeParserES.agent).build().executeForString();
        if (response.isSuccess()){
            return new AnimeResponse<>(parse(response.getResult(), url));
        }else {
            return new AnimeResponse<>(new AnimeError(Model.SERVER.TIOANIME,response.getError()));
        }
    }

    public static void fetch(String url, AnimeParserES.OnTaskCompleted onTaskCompleted){
        Log.d(TAG, "Requesting: "+url);
        ANRequest.GetRequestBuilder a = AndroidNetworking.get(url);
        String cookies = AnimeParserES.getInstance().getFlvCookies();
        if (cookies != null) a.addHeaders("cookie" , cookies);
        a.setUserAgent(AnimeParserES.agent).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                onTaskCompleted.onTaskCompleted(parse(response, url));
            }
            @Override
            public void onError(ANError anError) {
                onTaskCompleted.onError(new AnimeError(Model.SERVER.TIOANIME,anError));
            }
        });
    }

    private static WebModel parse(String response, String url) {
        WebModel data = new WebModel();
        data.setServer(Model.SERVER.ANIMEFLV);

        String name = "main";
        if (url.contains("genero=")) name = url.split("genero=")[1];
        Document document = Jsoup.parse(response);
        ArrayList<MiniModel> animes = new ArrayList<>();
        ArrayList<MiniModel> episodes = new ArrayList<>();
        Elements episodeLinks = document.select("article[class=episode]");
        Elements animeLinks = document.select("article[class=anime]");

        for (Element link: episodeLinks) {
            MiniModel episode = new MiniModel();
            String link2 = link.select("a").attr("href");
            if (!link2.contains(urlPrefix)) link2 = urlPrefix + link2;
            String image = link.select("img").attr("src");
            if (!image.contains(urlPrefix)) image = urlPrefix + image;

            Log.d("Anime", link2);
            String titleandepisode = link.select("h3[class=title]").text();
            Log.d("Anime", titleandepisode);

            Pattern pattern = Pattern.compile("(.+) (\\d+)$");
            Matcher matcher = pattern.matcher(titleandepisode);
            if (matcher.find()){
                episode.setTitle(matcher.group(1));
                episode.setEpisode(Integer.parseInt(matcher.group(2)));
            }
            else continue;
            episode.setImage(image);
            episode.setLink(link2);
            if (!episodes.contains(episode)) episodes.add(episode);
        }
        for (Element link: animeLinks) {
            MiniModel anime = new MiniModel();
            String link2 = link.select("a").attr("href");
            if (!link2.contains(urlPrefix)) link2 = urlPrefix + link2;
            String title = link.select("h3[class=title]").text();
            String image = link.select("img").attr("src");
            if (!image.contains(urlPrefix)) image = urlPrefix + image;
            anime.setLink(link2);
            anime.setTitle(title);
            anime.setImage(image);
            if (title != null && title.contains("Latino")) anime.setType(Model.TYPES.ESPANIOL);
            if (!animes.contains(anime)) animes.add(anime);
        }


        data.setName(name);
        data.setAnimes(animes);
        data.setEpisodes(episodes);

        return data;


    }
}
