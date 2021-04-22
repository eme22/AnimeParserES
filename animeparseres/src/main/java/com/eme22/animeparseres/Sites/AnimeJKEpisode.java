package com.eme22.animeparseres.Sites;

import android.util.Log;
import android.util.Pair;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANResponse;
import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.Model.AnimeError;
import com.eme22.animeparseres.Model.Model;
import com.eme22.animeparseres.Util.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeJKEpisode {

    private static final String urlprefix = "https://jkanime.net/";

    @SuppressWarnings("unchecked")
    public static Model fetch(String url) throws AnimeError {
        Log.d(TAG, "Requesting: "+url);

        ANResponse<String> response = AndroidNetworking.get(url).setUserAgent(AnimeParserES.agent).build().executeForString();

        if (response.isSuccess()){
            Log.d(TAG, "Server Response Successful");
            return parse(response.getResult(), url);
        }else {
            throw new AnimeError(response.getError().getErrorCode());
        }

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
        data.setEpisodeLinks(links);



        return data;

    }

    private static Pair<String, String> decodeJK(String link) {
        if (link.contains("mega.nz")) return new Pair<>("mega",link);
        else if (link.contains("zippyshare.com")) return new Pair<>("zippyshare",link);
        else if (link.contains("um.php")){
            ANResponse<String> anResponse = AndroidNetworking.get(link).build().executeForString();
            if (anResponse.isSuccess()){
                Pattern pattern = Pattern.compile("swarmId: '(.*?)'");
                Matcher matcher = pattern.matcher(anResponse.getResult());
                if (matcher.find()){
                    link = matcher.group(1);
                    return new Pair<>("um",link);
                }
            }

        }
        else if (link.contains("um2.php")){
            ANResponse<String> anResponse = AndroidNetworking.get(link).addHeaders("Referer", urlprefix).build().executeForString();
            if (anResponse.isSuccess()){

                link = anResponse.getOkHttpResponse().request().url().toString();
                //Log.d(TAG, "URL: "+link);
                return new Pair<>("um2", link);

            }
        }

        else if (link.contains("jkfembed.php")){
            ANResponse<String> anResponse = AndroidNetworking.get(link).build().executeForString();
            if (anResponse.isSuccess()){
                Document document = Jsoup.parse(anResponse.getResult());
                link = document.select("iframe").first().attr("src");
                return new Pair<>("fembed",link);
            }
        }
        else if (link.contains("jkokru.php")){
            ANResponse<String> anResponse = AndroidNetworking.get(link).build().executeForString();
            if (anResponse.isSuccess()){
                Document document = Jsoup.parse(anResponse.getResult());
                link = document.select("iframe").first().attr("src");
                return new Pair<>("okru",link);
            }
        }
        else if (link.contains("jkvmixdrop.php")){
            ANResponse<String> anResponse = AndroidNetworking.get(link).build().executeForString();
            if (anResponse.isSuccess()){
                Document document = Jsoup.parse(anResponse.getResult());
                link = document.select("iframe").first().attr("src");
                return new Pair<>("mixdrop",link);
            }
        }
        else if (link.contains("jk.php")){
            ANResponse<String> anResponse = AndroidNetworking.get(link).build().executeForString();
            if (anResponse.isSuccess()){
                Document document = Jsoup.parse(anResponse.getResult());
                link = document.select("video[id=jkvideo]").select("source").attr("src");
                return new Pair<>("jkmedia",link);
            }
        }

        return null;
    }

}

