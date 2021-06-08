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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeTioEpisode {

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

    private static Model parse(String response, String url) {
        Model data = new Model();
        Document document = Jsoup.parse(response);
        String titleandepisode = document.select("h1[class*=anime-title]").text();
        Pattern pattern = Pattern.compile("(.+) (\\d+)$");
        Matcher matcher = pattern.matcher(titleandepisode);
        if (matcher.find()){
            data.setName(matcher.group(1));
            data.setEpisode(Integer.parseInt(matcher.group(2)));
        }

        ArrayList<Pair<String,String>> links = null;
        Elements script = document.getElementsByTag("script");
        for (Element scripts : script) {
            if (scripts.data().contains("var videos = ")) {
                pattern = Pattern.compile(".*var videos = (\\[.*?\\]);");
                matcher = pattern.matcher(scripts.data());
                if (matcher.find()) {
                    try {
                        links = jsonToServerList(matcher.group(1));
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            }
        }

        String downloadurl = document.select("a[class*=btn-download]").attr("href");
        try {
            aditional(downloadurl, links);
        } catch (IOException e) {
            e.printStackTrace();
        }

        data.setUrl(url);
        data.setEpisodeLinks(links);

        return data;
    }

    private static void aditional(String link, ArrayList<Pair<String, String>> links) throws IOException {
        links.add(Decode.decodeLink(link));
    }

    private static ArrayList<Pair<String, String>> jsonToServerList(String episodes) throws JSONException {
        ArrayList<Pair<String, String>> servers = new ArrayList<>();
        JSONArray epsArray = new JSONArray(episodes);
        for (int i = 0; i < epsArray.length() ; i++) {
            JSONArray serverArray =epsArray.getJSONArray(i);
            servers.add(new Pair<>(serverArray.getString(0), serverArray.getString(1)));
        }
        return servers;
    }


}
