package com.eme22.animeparseres.Sites;


import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.webkit.CookieManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.eme22.animeparseres.AnimeParserES;
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
    private static final BypassInfo bp = new BypassInfo();

    public static void fetch(String url, CookieManager cookies, AnimeParserES.OnTaskCompleted onComplete) {
        Log.d(TAG, "Requesting: "+url);
        String cookie = cookies.getCookie(url);
        AnimeParserES.setFlvCookies(cookie);
        bp.setCookie(cookie);
        AndroidNetworking.get(url).addHeaders("cookie", cookie).setUserAgent(AnimeParserES.agent).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Server Response Successful");
                bp.setIsBypassing(BypassInfo.BypasStatus.SUCCEED);
                onComplete.onBypass(bp);
                Model model = parse(response, url);
                if (model!=null){
                    onComplete.onTaskCompleted(model,true);
                }else onComplete.onError();
            }

            @Override
            public void onError(ANError anError) {
                onComplete.onError();
            }
        });
    }

    public static void fetch(Context context, String url, AnimeParserES.OnTaskCompleted onComplete) {
        Log.d(TAG, "Requesting: "+url);

        ANRequest.GetRequestBuilder a = AndroidNetworking.get(url);
        String cookies = AnimeParserES.getFlvCookies();
        if (cookies != null) a.addHeaders("cookie" , cookies);

        a.setUserAgent(AnimeParserES.agent).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Server Response Successful");
                bp.setIsBypassing(BypassInfo.BypasStatus.NOT_NEEDED);
                onComplete.onBypass(bp);
                Model model = parse(response, url);
                if (model!=null){
                    onComplete.onTaskCompleted(model,true);
                }else onComplete.onError();
            }

            @Override
            public void onError(ANError anError) {
                if (anError.getErrorCode() == 503) {
                    bp.setIsBypassing(BypassInfo.BypasStatus.BYPASSING);
                    onComplete.onBypass(bp);
                    CFBypass.init(context, url, cookies -> fetch(url, cookies,onComplete));
                }
                else onComplete.onError();
            }
        });
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
