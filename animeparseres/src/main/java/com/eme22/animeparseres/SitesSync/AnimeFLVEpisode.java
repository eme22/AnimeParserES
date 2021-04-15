package com.eme22.animeparseres.SitesSync;


import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.webkit.CookieManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.AnimeParserES2;
import com.eme22.animeparseres.Model.AnimeError;
import com.eme22.animeparseres.Model.Model;
import com.eme22.animeparseres.Util.BypassInfo;
import com.eme22.animeparseres.Util.CFBypass;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeFLVEpisode {

    private static final String urlPrefix = "https://www3.animeflv.net";

    @SuppressWarnings("unchecked")
    public static Model fetch(String url, String cookies) throws AnimeError {
        Log.d(TAG, "Requesting: "+url);
        AnimeParserES2.getInstance().setFlvCookies(cookies);

        ANResponse<String> response = AndroidNetworking.get(url).addHeaders("cookie", cookies).setUserAgent(AnimeParserES.agent).build().executeForString();

        if (response.isSuccess()){
            return parse(response.getResult(), url);
        }else {
            throw new AnimeError(response.getError().getErrorCode());
        }
    }

    @SuppressWarnings("unchecked")
    public static Model fetch(String url) throws AnimeError {
        Log.d(TAG, "Requesting: "+url);

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

    private static Model parse(String response, String url) {
        Model data = new Model();
        Document document = Jsoup.parse(response);

        Element origin = document.getElementsByClass("Brdcrmb fa-home").select("a").get(1);

        String title = origin.text();
        ArrayList<Pair<String,String>> links = null;
        int internalid = 0;
        int episode = 0;

        Elements script = document.getElementsByTag("script");
        for (Element scripts : script) {
            if (scripts.data().contains("var videos = ")) {

                Pattern pattern = Pattern.compile(".*var videos = \\{\"SUB\":(\\[.*?\\])\\};");
                Matcher matcher = pattern.matcher(scripts.data());
                if (matcher.find()) {
                    JSONArray jsonArr;
                    try {
                        jsonArr = new JSONArray(matcher.group(1));
                        links = jsonToServerList(jsonArr);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }

                pattern = Pattern.compile(".*var anime_id = (.*?);");
                matcher = pattern.matcher(scripts.data());

                if (matcher.find()){
                    String internalidtemp = matcher.group(1);
                    if (internalidtemp != null) {
                        internalid = Integer.parseInt(internalidtemp);
                    }
                }

                pattern = Pattern.compile(".*var episode_number = (.*?);");
                matcher = pattern.matcher(scripts.data());

                if (matcher.find()){
                    String internalidtemp = matcher.group(1);
                    if (internalidtemp != null) {
                        episode = Integer.parseInt(internalidtemp);
                    }
                }

                break;

            }
        }

        Elements aditionalLinks = document.getElementsByClass("RTbl Dwnl").select("a");
        for (Element link: aditionalLinks) {
            String link2 = link.attr("href");
            if (link2.contains("mega.nz")) links.add(new Pair<>("mega", link2));
            else if (link2.contains("zippyshare.com")) links.add(new Pair<>("zippyshare", link2));
            else if (link2.contains("streamtape.com")) links.add(new Pair<>("stape", link2));
        }

        data.setName(title);
        data.setUrl(url);
        data.setEpisode(episode);
        data.setImage(episodeToImage(internalid, episode));
        data.setInternalID(internalid);
        data.setEpisodeLinks(links);

        return data;


    }

    private static ArrayList<Pair<String, String>> jsonToServerList(JSONArray episodes) throws JSONException {
        ArrayList<Pair<String, String>> servers = new ArrayList<>();
        for (int i = 0; i < episodes.length() ; i++) {
            JSONObject serverJSON =episodes.getJSONObject(i);
            servers.add(new Pair<>(serverJSON.getString("server"), serverJSON.getString("code")));
        }
        return servers;
    }


    private static String episodeToImage(int internal_id, int episode){
        return "https://cdn.animeflv.net/screenshots/"+internal_id+"/"+episode+"/th_3.jpg";
    }
}
