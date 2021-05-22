package com.eme22.animeparseres.Sites.Special;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANResponse;
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

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeIDBulk {

    private static final String urlprefix = "https://www.animeid.tv";

    @SuppressWarnings("unchecked")
    public static AnimeResponse<WebModel> fetch(String url) {
        Log.d(TAG, "Requesting: "+url);
        ANResponse<String> response = AndroidNetworking.get(url).setUserAgent(AnimeParserES.agent).build().executeForString();
        if (response.isSuccess()){
            return new AnimeResponse<>(parse(response.getResult(), url));
        }else {
            return new AnimeResponse<>(new AnimeError(Model.SERVER.ANIMEID,response.getError()));
        }
    }

    private static WebModel parse(String response, String url) {
        WebModel data = new WebModel();
        data.setServer(Model.SERVER.ANIMEID);
        String name;
        if (url.contains("/genero/") || url.contains("/letra/")) name = url.split("/")[4];
        else name = "main";

        Document document = Jsoup.parse(response);
        ArrayList<MiniModel> animes = new ArrayList<>();
        ArrayList<MiniModel> episodes = new ArrayList<>();


        Elements episodeLinks = document.select("section[class=lastcap]").select("a");
        Elements animeLinks = document.select("article[class=item]");


        for (Element episode: episodeLinks) {
            MiniModel episode2 = new MiniModel();
            String link2 = urlprefix + episode.attr("href");
            Log.d(TAG,link2);
            String[] nameandnum = episode.select("header").text().split(" #");
            String name2 = nameandnum[0];
            int num = Integer.parseInt(nameandnum[1]);
            String image = episode.select("img").attr("src");
            episode2.setTitle(name2);
            episode2.setEpisode(num);
            episode2.setImage(image);
            episode2.setLink(link2);
            if (!episodes.contains(episode2)) episodes.add(episode2);
        }

        for (Element anime: animeLinks) {
            MiniModel anime2 = new MiniModel();
            String link2 = urlprefix + anime.select("a").attr("href");
            String name2 = anime.select("header").text();
            String image2 = anime.select("img").attr("src");
            String desc = anime.select("p").text();

            anime2.setLink(link2);
            anime2.setTitle(name2);
            anime2.setImage(image2);
            anime2.setDescription(desc);

            if (link2.contains("peliculas")) anime2.setType(Model.TYPES.PELICULA);
            else if (link2.contains("ovas")) anime2.setType(Model.TYPES.OVA);
            else if (link2.contains("especiales")) anime2.setType(Model.TYPES.ESPECIAL);
            else anime2.setType(Model.TYPES.ANIME);
            if (name2.contains("Latino")) anime2.setType(Model.TYPES.ESPANIOL);
            if (!animes.contains(anime2)) animes.add(anime2);

        }

        data.setName(name);
        data.setAnimes(animes);
        data.setEpisodes(episodes);

        return data;

    }
}
