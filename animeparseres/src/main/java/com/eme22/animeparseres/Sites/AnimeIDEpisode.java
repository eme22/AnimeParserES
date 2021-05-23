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
import com.eme22.animeparseres.Model.Model;
import com.eme22.animeparseres.Util.Decode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeIDEpisode {

    @SuppressWarnings("unchecked")
    public static AnimeResponse<Model> fetch(String url){
        Log.d(TAG, "Requesting: "+url);
        ANResponse<String> response = AndroidNetworking.get(url).setUserAgent(AnimeParserES.agent).build().executeForString();
        if (response.isSuccess()){
            return new AnimeResponse<>(parse(response.getResult(), url));
        }else {
            return new AnimeResponse<>(new AnimeError(Model.SERVER.ANIMEID,response.getError()));
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
                onTaskCompleted.onError(new AnimeError(Model.SERVER.ANIMEID,anError));
            }
        });
    }

    private static Model parse(String response, String url) {
        Model data = new Model();
        Document document = Jsoup.parse(response);

        Elements data1 = document.select("article[id=infoanime]");
        String name = data1.select("a").first().text();
        String tempepisode = data1.select("strong").first().text().replaceAll("[^\\d.]", "");
        int episode = Integer.parseInt(tempepisode);
        String image = data1.select("img").attr("src");
        String description = data1.select("p").text();

        Elements links2 =document.select("ul[id=partes]").select("div[class=parte]");
        ArrayList<Pair<String,String>> links = new ArrayList<>();
        for (Element link: links2) {
            String source = link.attr("data");
            Pattern pattern = Pattern.compile("src=\\\\u0022(.*?)\\\\u0022");
            Matcher matcher = pattern.matcher(source);
            if (matcher.find()){
                source = matcher.group(1);
                if (source == null) continue;
                source = source.replace("\\","");
                Log.d(TAG,source);
                try {
                    links.add(decodeID(source));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        String aditionalurl = document.select("a:containsOwn(DESCARGAR)").get(1).attr("href");
        //Log.d(TAG, aditionalurl);

        if (AnimeParserES.getInstance().isAnimeIDAditionalLinksEnabled()){
            try {
                aditional(aditionalurl, links);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        data.setName(name);
        data.setDetails(description);
        data.setUrl(url);
        data.setEpisode(episode);
        data.setImage(image);
        data.setEpisodeLinks(links);

        return data;

    }

    private static Pair<String,String> decodeID(String link) throws IOException {
        if (link.contains("netu.php")){
            link = Jsoup.connect(link).timeout(3000).get().select("iframe").attr("src");
            return new Pair<>("netu",link);
        }
        else if (link.contains("streamtape.com")){
            return new Pair<>("stape",link);
        }
        else if (link.contains("animeid.tv/?vid=")){
            link = Jsoup.connect(link).timeout(3000).get().select("IFRAME").attr("src");
            return new Pair<>("clipwatching",link);
        }
        else return null;
    }

    private static void aditional(String url, ArrayList<Pair<String, String>> links) throws IOException {
        String link = Jsoup.connect("https://www.animeid.tv"+url).timeout(3000).get().select("meta[http-equiv=refresh]").attr("content").replaceAll("(.*)(?:http)","http");
        Log.d(TAG, link);
        if (link.contains("animekb")) links.addAll(animeKBsearch(link));
        else links.add(Decode.decodeLink(link));
    }

    private static ArrayList<Pair<String,String>> animeKBsearch(String url) throws IOException {
        ArrayList<Pair<String,String>> data = new ArrayList<>();
        Elements links = Jsoup.connect(url).timeout(3000).get().select("div[class=botondescarga]").parents();
        for (Element link: links) {
            data.add(new Pair<>(link.text(),link.attr("href")));
        }
        return data;
    }


}
