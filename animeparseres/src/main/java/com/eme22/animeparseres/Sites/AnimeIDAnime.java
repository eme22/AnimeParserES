package com.eme22.animeparseres.Sites;

import android.util.Log;
import android.util.Pair;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.Model.MiniModel;
import com.eme22.animeparseres.Model.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.eme22.animeparseres.AnimeParserES.TAG;

public class AnimeIDAnime {

    private static final String urlprefix = "https://www.animeid.tv";

    public static void fetch(String url, AnimeParserES.OnTaskCompleted onComplete) {
        Log.d(TAG, "Requesting: "+url);
        AndroidNetworking.get(url).setUserAgent(AnimeParserES.agent).build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Server Response Successful");
                Model model = parse(response, url);
                if (model!=null){
                    onComplete.onTaskCompleted(model,false);
                }else onComplete.onError();
            }

            @Override
            public void onError(ANError anError) {
                Log.e(TAG, anError.getLocalizedMessage());
                onComplete.onError();
            }
        });
    }

    private static Model parse(String response, String url) {
        Model data = new Model();
        Document document = Jsoup.parse(response);

        Elements main = document.select("article[id=anime]");

        String image = main.select("img").attr("src");
        String name = main.select("h1").text();
        String[] alternatives = main.select("h2").text().split(",");
        String description = Parser.unescapeEntities(main.select("p[class=sinopsis]").text(), false);
        String type = document.select("div[class=status-left]").select("span").first().text();


        ArrayList<Pair<String,String>> categories = new ArrayList<>();

        Elements categorias = main.select("ul[class=tags]").select("a");

        for (Element cat: categorias) {
            categories.add(new Pair<>(cat.text(),urlprefix + cat.attr("href")));
        }

        String tempinternalid = document.select("div[data-id]").attr("data-id");
        Log.d(TAG,tempinternalid);
        int internalid = Integer.parseInt(tempinternalid);


        Pattern pattern = Pattern.compile("https:\\/\\/www\\.animeid\\.tv\\/(.*)\\/*");
        Matcher matcher = pattern.matcher(url);

        String prefix = null;
        if (matcher.find()){
            prefix =  matcher.group(1);
        }

        ArrayList<MiniModel> episodes = null;
        try {
            episodes = readEpisodes(name,prefix,image,internalid);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        data.setName(name);
        data.setInternalID(internalid);
        data.setDetails(description);
        data.setImage(image);
        data.setAlternativeNames(alternatives);
        data.setUrl(url);

        if (type.contains("Serie")) data.setAnimeType(Model.TYPES.ANIME);
        else if (type.contains("Pelicula")) data.setAnimeType(Model.TYPES.PELICULA);
        else if (type.contains("OVA") || type.contains("ONA")) data.setAnimeType(Model.TYPES.OVA);
        else if (type.contains("Especial")) data.setAnimeType(Model.TYPES.ESPECIAL);
        if (name.contains("Latino")) data.setAnimeType(Model.TYPES.ESPANIOL);

        data.setCategories(categories);
        data.setEpisodes(episodes);

        return data;

    }

    private static ArrayList<MiniModel> readEpisodes(String name,String prefix,String image,int internalid) throws JSONException {
        ArrayList<MiniModel> episodes = new ArrayList<>();
        int i = 1;
        while (true){
            Log.d(TAG, "Requesting: "+"https://www.animeid.tv/ajax/caps?id="+internalid+"&ord=DESC&pag="+i);

            ANResponse<JSONObject> anResponse = AndroidNetworking.get("https://www.animeid.tv/ajax/caps?id="+internalid+"&ord=DESC&pag="+i).addHeaders("X-Requested-With","XMLHttpRequest").build().executeForJSONObject();

            if (anResponse.isSuccess()) {
                JSONArray response = anResponse.getResult().getJSONArray("list");
                if (response.length() < 1) break;
                for (int j = 0; j < response.length() ; j++) {
                    MiniModel model  = new MiniModel();
                    JSONObject animes = response.getJSONObject(j);
                    if (animes == null) break;
                    int number = Integer.parseInt(animes.getString("numero"));
                    model.setTitle(name);
                    model.setEpisode(number);
                    model.setLink(urlprefix+"/v/"+prefix+"-"+number);
                    model.setImage(image);
                    episodes.add(model);
                }
            }
            else break;


            i++;

        }
        return episodes;
    }
}
