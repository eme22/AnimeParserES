package com.eme22.animeparseres.Sites;

import android.util.Log;
import android.util.Pair;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.Model.AnimeError;
import com.eme22.animeparseres.Model.AnimeResponse;
import com.eme22.animeparseres.Model.MiniModel;
import com.eme22.animeparseres.Model.Model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeTioAnime {

    private static final String urlPrefix = "https://tioanime.com";

    @SuppressWarnings("unchecked")
    public static AnimeResponse<Model> fetch(String url){
        Log.d(TAG, "Requesting: "+url);
        ANResponse<String> response = AndroidNetworking.get(url).setUserAgent(AnimeParserES.agent).build().executeForString();
        if (response.isSuccess()){
            return new AnimeResponse<>(parse(response.getResult(), url));
        }else {
            return new AnimeResponse<>(new AnimeError(Model.SERVER.TIOANIME,response.getError()));
        }
    }

    public static void fetch(String url, AnimeParserES.OnTaskCompleted onTaskCompleted){
        Log.d(TAG, "Requesting: "+url);
        AndroidNetworking.get(url).setUserAgent(AnimeParserES.agent).build().getAsString(new StringRequestListener() {
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

    private static Model parse(String response, String url){
        Model data = new Model();
        Document document = Jsoup.parse(response);

        Elements detailtext = document.select("article[class=anime-single]");
        String image = detailtext.select("img[alt=img]").attr("src");
        if (!image.contains(urlPrefix)) image = urlPrefix + image;
        Log.d("IMAGE", image);
        String title = detailtext.select("h1[class=title]").text();
        String tipo = detailtext.select("span[class=anime-type-peli]").text();
        String[] altern = {detailtext.select("p[class=original-title]").text()};
        String description = detailtext.select("p[class=sinopsis]").text();
        //String score = detailtext.select("span[id=score]").text();
        ArrayList<Pair<String,String>> categories = new ArrayList<>();
        Elements categories2 = detailtext.select("p[class=genres]").select("a");
        for (Element element: categories2) {
            categories.add(new Pair<>(element.text(),urlPrefix + element.attr("href")));
        }

        ArrayList<MiniModel> episodes = null;
        int internalid = 0;
        int malid = 0;
        String prefix;
        Elements scripts =  document.getElementsByTag("script");
        for (Element script: scripts) {

            if (script.data().contains("https://api.jikan.moe/v3/anime/")){
                Pattern pattern = Pattern.compile("https://api.jikan.moe/v3/anime/(.*?)'");
                Matcher matcher = pattern.matcher(script.data());
                if (matcher.find()){
                    String malids = matcher.group(1);
                    if (malids != null) malid = Integer.parseInt(malids);
                }
            }

            if (script.data().contains("var anime_info = ")) {
                Pattern pattern = Pattern.compile(".*var anime_info = \\[\"(.*?)\",\"(.*?)\",\"(.*?)\",*");
                Matcher matcher = pattern.matcher(script.data());
                if (matcher.find()){
                    String internal = matcher.group(1);
                    title = Parser.unescapeEntities(matcher.group(3),false);
                    prefix = matcher.group(2);
                    internalid = internal != null ? Integer.parseInt(internal) : 0;
                } else {
                    Log.d(TAG, "No match found!");
                    return null;
                }

                pattern = Pattern.compile(".*var episodes = \\[(.*?)\\];");
                matcher = pattern.matcher(script.data());
                if (matcher.find()) {
                    String matcherg = matcher.group(1);
                    if (matcherg != null) {
                        episodes = readEpisodes( title,internalid ,matcherg, prefix);
                    }
                    else return null;
                } else {
                    Log.e(TAG,"No match found!");
                    return null;
                }

                break;
            }
        }

        data.setInternalID(internalid);
        data.setName(title);
        data.setDetails(description);
        data.setImage(image);
        data.setAlternativeNames(altern);
        data.setUrl(url);
        data.setMalid(malid);
        //data.setPunctuation(score);
        Log.d(TAG, "type: "+ tipo);
        if (tipo.contains("Anime")) data.setAnimeType(Model.TYPES.ANIME);
        else if (tipo.contains("Película")) data.setAnimeType(Model.TYPES.PELICULA);
        else if (tipo.contains("OVA")) data.setAnimeType(Model.TYPES.OVA);
        else if (tipo.contains("Especial")) data.setAnimeType(Model.TYPES.ESPECIAL);
        if (title.contains("Latino")) data.setAnimeType(Model.TYPES.ESPANIOL);
        data.setCategories(categories);
        data.setEpisodes(episodes);

        return data;
    }

    private static ArrayList<MiniModel> readEpisodes(String name, int internalid, String group, String prefix) {

        Log.d("DATA", "Name: "+ name + "\nInternalID: "+internalid+ "\nGroup: "+ group+ "\nPrefix: "+prefix);

        ArrayList<MiniModel> episodes = new ArrayList<>();
        String[] groups = group.split(",");
        for (String episode: groups) {
            MiniModel model  = new MiniModel();
            int episode2 = Integer.parseInt(episode.split(",")[0]);
            model.setTitle(name);
            model.setEpisode(episode2);
            model.setLink(urlPrefix+"/ver/"+prefix+"-"+episode2);
            model.setImage(urlPrefix+"/uploads/thumbs/"+internalid+".jpg");
            episodes.add(model);
        }
        return episodes;
    }
}
