package com.eme22.animeparseres.SitesSync;

import android.util.Log;
import android.util.Pair;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.Model.AnimeError;
import com.eme22.animeparseres.Model.MiniModel;
import com.eme22.animeparseres.Model.Model;

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

public class AnimeJKAnime {

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

        Elements detailtext = document.select("div[class=anime__details__content]");
        String title = detailtext.select("div[class=anime__details__title]").text();
        String image = detailtext.select("div[class=anime__details__pic set-bg]").attr("data-setbg");
        String description = detailtext.select("p").text();
        String[] altern = document.select("div[class*=alternativost]").select("b[class=t]").eachText().toArray(new String[0]);

        Elements typeanddate = detailtext.select("div[class=anime__details__widget]");
        String tipo = typeanddate.select("li").first().text().replace("Tipo: ","");

        ArrayList<Pair<String,String>> categories = new ArrayList<>();
        Elements categories2 = typeanddate.select("li").get(1).select("a");
        for (Element element: categories2) {
            categories.add(new Pair<>(element.text(),element.attr("href")));
        }

        ArrayList<MiniModel> episodes = null;

        int internalid = 0;

        Elements scripts = document.getElementsByTag("script");
        for (Element script: scripts) {
            Pattern pattern = Pattern.compile("ajax\\/pagination_episodes\\/(.*?)\\/");
            Matcher matcher = pattern.matcher(script.data());
            if (matcher.find()){
                internalid = Integer.parseInt(matcher.group(1));
                try {
                    episodes = readEpisodes(title, internalid,url,image);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        data.setInternalID(internalid);
        data.setName(title);
        data.setDetails(description);
        data.setImage(image);
        data.setAlternativeNames(altern);
        data.setUrl(url);
        Log.d(TAG, "type: "+ tipo);

        if (tipo.contains("Serie")) data.setAnimeType(Model.TYPES.ANIME);
        else if (tipo.contains("Pelicula")) data.setAnimeType(Model.TYPES.PELICULA);
        else if (tipo.contains("OVA")) data.setAnimeType(Model.TYPES.OVA);
        else if (tipo.contains("ONA")) data.setAnimeType(Model.TYPES.ONA);
        else if (tipo.contains("Especial")) data.setAnimeType(Model.TYPES.ESPECIAL);
        if (title.contains("Latino")) data.setAnimeType(Model.TYPES.ESPANIOL);

        data.setCategories(categories);
        data.setEpisodes(episodes);


        return data;


    }


    private static ArrayList<MiniModel> readEpisodes(String name, int internalid,String url,String image) throws JSONException {
        ArrayList<MiniModel> episodes = new ArrayList<>();
        int i = 1;
        while (true){
            Log.d(TAG, "Requesting: "+"https://jkanime.net/index.php/ajax/pagination_episodes/"+internalid+"/"+i);

            ANResponse<JSONArray> anResponse = AndroidNetworking.get("https://jkanime.net/index.php/ajax/pagination_episodes/"+internalid+"/"+i).build().executeForJSONArray();

            if (anResponse.isSuccess()) {
                JSONArray response = anResponse.getResult();
                if (response.length() < 1) break;
                for (int j = 0; j < response.length() ; j++) {
                    MiniModel model  = new MiniModel();
                    JSONObject animes = response.getJSONObject(j);
                    if (animes == null) break;
                    int number = Integer.parseInt(animes.getString("number"));

                    model.setTitle(name);
                    model.setEpisode(number);
                    model.setLink(url+number+"/");
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
