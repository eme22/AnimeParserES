package com.eme22.animeparseres.Sites;

import android.util.Log;
import android.util.Pair;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
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

public class AnimeFLVAnime {

    private static final String urlPrefix = "https://www3.animeflv.net";

    @SuppressWarnings("unchecked")
    public static AnimeResponse<Model> fetch(String url, String cookies) {
        Log.d(TAG, "Requesting: "+url);
        AnimeParserES.getInstance().setFlvCookies(cookies);
        ANResponse<String> response = AndroidNetworking.get(url).addHeaders("cookie", cookies).setUserAgent(AnimeParserES.agent).build().executeForString();
        if (response.isSuccess()){
            return new AnimeResponse<>(parse(response.getResult(), url));
        }else {
            return new AnimeResponse<>(new AnimeError(Model.SERVER.ANIMEFLV,response.getError()));
        }
    }

    @SuppressWarnings("unchecked")
    public static AnimeResponse<Model> fetch(String url) {
        Log.d(TAG, "Requesting: "+url);
        ANRequest.GetRequestBuilder a = AndroidNetworking.get(url);
        String cookies = AnimeParserES.getInstance().getFlvCookies();
        if (cookies != null) a.addHeaders("cookie" , cookies);
        ANResponse<String> response = a.setUserAgent(AnimeParserES.agent).build().executeForString();
        if (response.isSuccess()){
            return new AnimeResponse<>(parse(response.getResult(), url));
        }else {
            return new AnimeResponse<>(new AnimeError(Model.SERVER.ANIMEFLV,response.getError()));
        }
    }

    private static Model parse(String response, String url) {
        Model data = new Model();
        Document document = Jsoup.parse(response);
        String altTitle = document.select("span[class=TxtAlt]").text();
        String description = document.select("div[class=Description]").text();
        String image = urlPrefix + document.getElementsByTag("figure").select("img").attr("src");
        String type = document.select("span[class*=Type]").text();
        double punc = Double.parseDouble(document.select("span[class=vtprmd]").text());

        int internalid = 0;
        String title = null;
        String prefix;
        ArrayList<MiniModel> episodes = null;
        ArrayList<Pair<String,String>> categories = new ArrayList<>();

        Elements categories2 = document.select("nav[class=Nvgnrs]").select("a");
        for (Element element: categories2) {
            categories.add(new Pair<>(element.text(),urlPrefix + element.attr("href")));
        }



        Elements scripts =  document.getElementsByTag("script");
        for (Element script: scripts) {
            if (script.data().contains("var anime_info = ")) {

                Pattern pattern = Pattern.compile(".*var anime_info = \\[\"(.*?)\",\"(.*?)\",\"(.*?)\"];");
                Matcher matcher = pattern.matcher(script.data());
                if (matcher.find()){
                    String internal = matcher.group(1);
                    title = Parser.unescapeEntities(matcher.group(2),false);
                    prefix = matcher.group(3);
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
        data.setAlternativeNames(new String[]{altTitle});
        data.setUrl(url);
        data.setPunctuation(punc);
        Log.d(TAG, "type: "+ type);
        if (type.contains("Anime")) data.setAnimeType(Model.TYPES.ANIME);
        else if (type.contains("Película")) data.setAnimeType(Model.TYPES.PELICULA);
        else if (type.contains("OVA")) data.setAnimeType(Model.TYPES.OVA);
        else if (type.contains("Especial")) data.setAnimeType(Model.TYPES.ESPECIAL);
        if (title != null && title.contains("Latino")) data.setAnimeType(Model.TYPES.ESPANIOL);
        data.setCategories(categories);
        data.setEpisodes(episodes);

        return data;






    }

    private static ArrayList<MiniModel> readEpisodes(String name, int internalid, String group, String prefix) {
        ArrayList<MiniModel> episodes = new ArrayList<>();
        String[] groups = group.substring(1, group.length() -1).split("\\],\\[");
        for (String episode: groups) {
            MiniModel model  = new MiniModel();
            int episode2 = Integer.parseInt(episode.split(",")[0]);
            model.setTitle(name);
            model.setEpisode(episode2);
            model.setLink(urlPrefix+"/ver/"+prefix+"-"+episode2);
            model.setImage(episodeToImage(internalid,episode2));
            episodes.add(model);
        }
        return episodes;
    }

    private static String episodeToImage(int internal_id, int episode){
        return "https://cdn.animeflv.net/screenshots/"+internal_id+"/"+episode+"/th_3.jpg";
    }
}
