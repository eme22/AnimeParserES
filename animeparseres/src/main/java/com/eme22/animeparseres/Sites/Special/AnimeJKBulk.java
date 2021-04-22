package com.eme22.animeparseres.Sites.Special;

import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANResponse;
import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.Model.AnimeError;
import com.eme22.animeparseres.Model.MiniModel;
import com.eme22.animeparseres.Model.Model;
import com.eme22.animeparseres.Model.WebModel;
import com.eme22.animeparseres.Util.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeJKBulk {

    //private static final String urlprefix = "https://jkanime.net/";

    @SuppressWarnings("unchecked")
    public static WebModel fetch(String url) throws AnimeError {
        Log.d(TAG, "Requesting: "+url);

        ANResponse<String> response = AndroidNetworking.get(url).setUserAgent(AnimeParserES.agent).build().executeForString();
        if (response.isSuccess()){
            return parse(response.getResult(), url);
        }else {
            Log.e(TAG, response.getError().getLocalizedMessage());
            throw new AnimeError(response.getError().getErrorCode());
        }

    }

    private static WebModel parse(String response, String url) {

        WebModel data = new WebModel();
        data.setServer(Model.SERVER.JKANIME);

        String name;
        if (url.contains("/buscar/") || url.contains("/letra/")) name = url.split("/")[4];
        else name = "main";

        Document document = Jsoup.parse(response);
        ArrayList<MiniModel> animes = new ArrayList<>();
        ArrayList<MiniModel> episodes = new ArrayList<>();

        Elements episodeLinks = document.select("a[class=bloqq]");
        Elements animeLinks = document.select("section[class=contenido spad]").select("div[class=anime__item]");

        for (Element episode: episodeLinks) {
            MiniModel episode2 = new MiniModel();
            String link2 = episode.attr("href");
            String title = episode.select("h5").text();
            String image = episode.select("img").attr("src");
            String episodenumtemp = link2.split("/")[4];
            int episodenum = Util.parseEp(episodenumtemp);
            episode2.setTitle(title);
            episode2.setEpisode(episodenum);
            episode2.setImage(image);
            episode2.setLink(link2);
            if (!episodes.contains(episode2)) episodes.add(episode2);
        }

        for (Element link: animeLinks) {
            MiniModel anime = new MiniModel();
            Element linkandname = link.select("a").get(1);

            String link2 = linkandname.attr("href");
            String title = linkandname.text();
            if (title.isEmpty()) continue;
            String desc = link.select("p").text();
            String type = link.select("li[class=anime]").text();
            String image = link.select("div[class*=anime__item__pic]").attr("data-setbg");

            anime.setLink(link2);
            anime.setTitle(title);
            anime.setImage(image);
            anime.setDescription(desc);

            if (type.contains("Serie")) anime.setType(Model.TYPES.ANIME);
            else if (type.contains("Pelicula")) anime.setType(Model.TYPES.PELICULA);
            else if (type.contains("OVA")) anime.setType(Model.TYPES.OVA);
            else if (type.contains("ONA")) anime.setType(Model.TYPES.ONA);
            else if (type.contains("Especial")) anime.setType(Model.TYPES.ESPECIAL);
            if (title.contains("Latino")) anime.setType(Model.TYPES.ESPANIOL);

            if (!animes.contains(anime)) animes.add(anime);

        }

        data.setName(name);
        data.setAnimes(animes);
        data.setEpisodes(episodes);

        return data;
    }


}
