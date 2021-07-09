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
import com.eme22.animeparseres.Util.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeJKEpisode {

    private static final String urlprefix = "https://jkanime.net/";

    @SuppressWarnings("unchecked")
    public static AnimeResponse<Model> fetch(String url) {
        Log.d(TAG, "Requesting: "+url);
        ANResponse<String> response = AndroidNetworking.get(url).setUserAgent(AnimeParserES.agent).build().executeForString();
        if (response.isSuccess()){
            return new AnimeResponse<>(parse(response.getResult(), url));
        }else {
            return new AnimeResponse<>(new AnimeError(Model.SERVER.JKANIME,response.getError()));
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
                onTaskCompleted.onError(new AnimeError(Model.SERVER.JKANIME,anError));
            }
        });
    }

    private static Model parse(String response, String url) {
        Model data = new Model();
        Document document = Jsoup.parse(response);

        Elements detailtext = document.select("div[class=video-info]");

        String title = detailtext.select("h2").text();
        String image = detailtext.select("img").attr("src");
        String description = detailtext.select("p").text();

        ArrayList<Pair<String,String>> links = new ArrayList<>();
        int internalid = 0;


        Pattern pattern = Pattern.compile(urlprefix+"(?:.*?)/(.*?)/");
        Matcher matcher = pattern.matcher(url);

        int episode = 0 ;

        if (matcher.find()){
           String tempep = matcher.group(1);
            if (tempep != null) {
                episode = Util.parseEp(tempep);
            }
        }


        Elements linkscript =  document.getElementsByTag("script");
        Elements morelinks =  document.select("td ").select("a");

        for (Element todos : linkscript) {
            if(todos.data().contains("var video = []")){
                pattern = Pattern.compile("video\\[[0-9]\\] = '(.*)'");
                matcher = pattern.matcher(todos.data());
                while (matcher.find()){
                    //Log.d("MATCHED", matcher.group(1));
                    String link = Jsoup.parse(matcher.group(1)).getElementsByTag("iframe").first().attr("src");
                    try {
                        links.add(decodeJK(link));
                    }
                    catch (NullPointerException ignored){ }
                }

                break;
            }
        }

        for (Element links2: morelinks) {
            try {
                links.add(decodeJK(links2.attr("href")));
            } catch (NullPointerException ignored){}

        }

        data.setName(title);
        data.setDetails(description);
        data.setUrl(url);
        data.setEpisode(episode);
        data.setImage(image);
        data.setInternalID(internalid);

        links.removeAll(Collections.singleton(null));
        data.setEpisodeLinks(links);



        return data;

    }

    private static Pair<String, String> decodeJK(String link) {
        if (link.contains("mega.nz")) return new Pair<>("mega",link);
        else if (link.contains("zippyshare.com")) return new Pair<>("zippyshare",link);
        else if (link.contains("um.php")){
            ANResponse<String> anResponse;
            try {
                anResponse = Util.asyncCall(link, null);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Async Request Failed:\n"+ e.getLocalizedMessage());
                return null;
            }
            //ANResponse<String> anResponse = AndroidNetworking.get(link).build().executeForString();
            if (anResponse != null && anResponse.isSuccess()){
                Pattern pattern = Pattern.compile("swarmId: '(.*?)'");
                Matcher matcher = pattern.matcher(anResponse.getResult());
                if (matcher.find()){
                    link = matcher.group(1);
                    return new Pair<>("um",link);
                }
                else {
                    Log.e(TAG, "No MATCH");
                    return null;
                }
            }
            else {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Network Request Failed:\n"+ (anResponse != null ? anResponse.getError().getErrorCode() : "Null Request"));
                return null;
            }

        }
        else if (link.contains("um2.php")){
            ANResponse<String> anResponse;
            try {
                anResponse = Util.asyncCall(link, new Pair<>("Referer", urlprefix));
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Async Request Failed:\n"+ e.getLocalizedMessage());
                return null;
            }
            //ANResponse<String> anResponse = AndroidNetworking.get(link).addHeaders("Referer", urlprefix).build().executeForString();
            if (anResponse != null && anResponse.isSuccess()){
                link = anResponse.getOkHttpResponse().request().url().toString();
                //Log.d(TAG, "URL: "+link);
                return new Pair<>("um2", link);

            }
            else {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Network Request Failed:\n"+ (anResponse != null ? anResponse.getError().getErrorCode() : "Null Request"));
                return null;
            }
        }

        else if (link.contains("jkfembed.php")){
            ANResponse<String> anResponse;
            try {
                anResponse = Util.asyncCall(link, null);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Async Request Failed:\n"+ e.getLocalizedMessage());
                return null;
            }
            //ANResponse<String> anResponse = AndroidNetworking.get(link).build().executeForString();
            if (anResponse != null && anResponse.isSuccess()){
                Document document = Jsoup.parse(anResponse.getResult());
                link = document.select("iframe").first().attr("src");
                return new Pair<>("fembed",link);
            }
            else {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Network Request Failed:\n"+ (anResponse != null ? anResponse.getError().getErrorCode() : "Null Request"));
                return null;
            }
        }
        else if (link.contains("jkokru.php")){
            ANResponse<String> anResponse;
            try {
                anResponse = Util.asyncCall(link, null);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Async Request Failed:\n"+ e.getLocalizedMessage());
                return null;
            }
            //ANResponse<String> anResponse = AndroidNetworking.get(link).build().executeForString();
            if (anResponse != null && anResponse.isSuccess()){
                Document document = Jsoup.parse(anResponse.getResult());
                link = document.select("iframe").first().attr("src");
                return new Pair<>("okru",link);
            }
            else {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Network Request Failed:\n"+ (anResponse != null ? anResponse.getError().getErrorCode() : "Null Request"));
                return null;
            }
        }
        else if (link.contains("jkvmixdrop.php")){
            ANResponse<String> anResponse;
            try {
                anResponse = Util.asyncCall(link, null);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Async Request Failed:\n"+ e.getLocalizedMessage());
                return null;
            }
            //ANResponse<String> anResponse = AndroidNetworking.get(link).build().executeForString();
            if (anResponse != null && anResponse.isSuccess()){
                Document document = Jsoup.parse(anResponse.getResult());
                link = document.select("iframe").first().attr("src");
                if (link.startsWith("//")) link = "https:" + link;
                return new Pair<>("mixdrop",link);
            }
            else {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Network Request Failed:\n"+ (anResponse != null ? anResponse.getError().getErrorCode() : "Null Request"));
                return null;
            }
        }
        else if (link.contains("jk.php")){
            ANResponse<String> anResponse;
            try {
                anResponse = Util.asyncCall(link, null);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Async Request Failed:\n"+ e.getLocalizedMessage());
                return null;
            }
            //ANResponse<String> anResponse = AndroidNetworking.get(link).build().executeForString();
            if (anResponse != null && anResponse.isSuccess()){
                Document document = Jsoup.parse(anResponse.getResult());
                link = document.select("video[id=jkvideo]").select("source").attr("src");
                return new Pair<>("jkmedia",link);
            }
            else {
                Log.e(TAG, "Link: "+ link);
                Log.e(TAG, "Network Request Failed:\n"+ (anResponse != null ? anResponse.getError().getErrorCode() : "Null Request"));
                return null;
            }
        }
        else {
            Log.e(TAG, link);
            return null;
        }

    }

}

