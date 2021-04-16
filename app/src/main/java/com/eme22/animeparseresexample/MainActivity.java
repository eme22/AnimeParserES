package com.eme22.animeparseresexample;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.Model.AnimeError;
import com.eme22.animeparseres.Model.MiniModel;
import com.eme22.animeparseres.Model.Model;
import com.eme22.animeparseres.Model.WebModel;
import com.eme22.animeparseresexample.adapters.AnimeAdapter;
import com.eme22.animeparseresexample.adapters.CategoriesAdapter;
import com.eme22.animeparseresexample.adapters.EpisodeAdapter;
import com.eme22.animeparseresexample.adapters.LinksAdapter;
import com.eme22.animeparseresexample.util.CheckInternet;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    AnimeParserES parserES;
    EditText edit_query;

    View progressBar;
    View anime;
    View multiple;

    Snackbar bypassSnack = null;

    ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        progressBar = findViewById(R.id.loading);
        anime = findViewById(R.id.model);
        multiple = findViewById(R.id.animelist);
        parserES = AnimeParserES.getInstance();
        edit_query = findViewById(R.id.edit_query);

    }

    private void makesnack(String bypass, boolean b) {
        if (bypassSnack != null) {
            bypassSnack.dismiss();
            bypassSnack=null;
        }
        bypassSnack = Snackbar.make( findViewById(R.id.main),bypass, b ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_SHORT);
        bypassSnack.show();
    }

    public void fetch(View view) {
        String url = edit_query.getText().toString();
        Log.d("APP:", url);
        edit_query.setText(null);
        letGo(url);
    }

    public void flvan(View view){

        letGo("https://www3.animeflv.net/");



    }

    public void jkan(View view){
        letGo("https://jkanime.net/");
    }

    public void idan(View view){

        letGo("https://www.animeid.tv/");
    }


    private void letGo(String url) {
        if (checkInternet()) {
            progressBar.setVisibility(View.VISIBLE);
            if (multiple.getVisibility() == View.VISIBLE) multiple.setVisibility(View.GONE);
            if (anime.getVisibility() == View.VISIBLE) anime.setVisibility(View.GONE);
            //parserES.load(url);


            parserES.getAsync(url, new AnimeParserES.OnTaskCompleted() {
                @Override
                public void onTaskCompleted(Object animes) {
                    if (animes instanceof WebModel){
                        loadlist((WebModel) animes);
                    }
                    else loadanime((Model) animes , ((Model) animes).getCategories() == null);
                }

                @Override
                public void onError(AnimeError error) {
                    error.printStackTrace();
                }
            });


            /*
            executor.submit(() -> {
                parserES.setBypassWebView(findViewById(R.id.aaatest));
                Object animes = null;
                try {
                    animes = parserES.getSync(url);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (AnimeError error) {
                    error.printStackTrace();
                }
                if (animes instanceof WebModel){
                    Object finalAnimes = animes;
                    MainActivity.this.runOnUiThread(() -> loadlist((WebModel) finalAnimes));
                }
                else {
                    Object finalAnimes1 = animes;
                    MainActivity.this.runOnUiThread(() -> loadanime((Model) finalAnimes1, ((Model) finalAnimes1).getCategories() == null));
                }
            });

             */


        }
    }

    public boolean checkInternet() {
        return new CheckInternet(this).isInternetOn();
    }

    private void loadanime(Model vidURL, boolean episode) {

        anime.invalidate();

        TextView title = anime.findViewById(R.id.titleText);
        TextView titletext = anime.findViewById(R.id.titletext);
        TextView alt = anime.findViewById(R.id.altTitleText);
        TextView alttext = anime.findViewById(R.id.alternativenamestext);
        TextView desc = anime.findViewById(R.id.descText);
        TextView desctext = anime.findViewById(R.id.desctext);
        TextView linktext = anime.findViewById(R.id.linktext);
        TextView typetext = anime.findViewById(R.id.typetext);
        TextView type = anime.findViewById(R.id.typeText);
        TextView puntext = anime.findViewById(R.id.punctuationtext);
        TextView pun = anime.findViewById(R.id.puntuacionText);
        TextView epstext = anime.findViewById(R.id.episodetext);
        TextView eps = anime.findViewById(R.id.episodioText);
        TextView epsltext = anime.findViewById(R.id.episodelinkstext);
        TextView catstext = anime.findViewById(R.id.categoriestext);

        ImageView image = findViewById(R.id.animePhoto);

        EditText link = findViewById(R.id.linkText);

        RecyclerView categories = findViewById(R.id.categoriesRecycler);
        RecyclerView episodes = findViewById(R.id.episodesRecycler);

        try {
            Picasso.get().load(vidURL.getImage()).placeholder(new CircularProgressDrawable(MainActivity.this)).into(image);
        }
        catch (Exception e){
            e.printStackTrace();
            image.setVisibility(View.GONE);
        }

        try {
            title.setText(vidURL.getName());
        }
        catch (Exception e) {
            title.setVisibility(View.GONE);
            titletext.setVisibility(View.GONE);
        }

        try {
            alt.setText(TextUtils.join(", ",vidURL.getAlternativeNames()));
        }
        catch (Exception e){
            alttext.setVisibility(View.GONE);
            alt.setVisibility(View.GONE);
        }

        try {
            desc.setText(vidURL.getDetails());
        }
        catch (Exception e){
            desctext.setVisibility(View.GONE);
            desc.setVisibility(View.GONE);
        }

        try {
            link.setText(vidURL.getUrl());
        }
        catch (Exception e){
            link.setVisibility(View.GONE);
            linktext.setVisibility(View.GONE);
        }

        try {
            type.setText(vidURL.getAnimeType().name());
        }
        catch (Exception e){
            type.setVisibility(View.GONE);
            typetext.setVisibility(View.GONE);
        }

        try {
            Double punc = vidURL.getPunctuation();
            if (punc == null) throw new Exception();
            else pun.setText(String.valueOf(punc));
        } catch (Exception e){
            puntext.setVisibility(View.GONE);
            pun.setVisibility(View.GONE);
        }

        try {
            epstext.setText(episode ? "Episodio" : "Episodios");
            if (episode) eps.setText(String.valueOf(vidURL.getEpisode()));
            else eps.setText(String.valueOf(vidURL.getEpisodes().size()));
        } catch (Exception e){
            eps.setVisibility(View.GONE);
            epstext.setVisibility(View.GONE);
        }

        try {
            catstext.setText(episode ? "Links" : "Categorias");
            if (episode){
                categories.setAdapter(new LinksAdapter(vidURL.getEpisodeLinks(), category -> Toast.makeText(MainActivity.this, "Link: "+ category.second, Toast.LENGTH_SHORT).show()));
            }
            else {
                categories.setAdapter(new CategoriesAdapter(vidURL.getCategories(), new CategoriesAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(Pair<String, String> category) {
                        Toast.makeText(MainActivity.this, "Link: "+category.second+"\n Click Largo para copiar", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLongItemClick(String text) {
                        copy(text);
                    }
                }));


            }
        } catch (Exception e){
            catstext.setVisibility(View.GONE);
            categories.setVisibility(View.GONE);
        }

        try {
            episodes.setAdapter(new EpisodeAdapter(vidURL.getEpisodes(), new EpisodeAdapter.ItemClickListener() {
                @Override
                public void onItemClick(MiniModel episode) {
                    Toast.makeText(MainActivity.this, "Link: "+episode.getLink()+"\n Click Largo para copiar", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onLongItemClick(String text) {
                    copy(text);
                }
            }));
        } catch (Exception e){
            epsltext.setVisibility(View.GONE);
            episodes.setVisibility(View.GONE);
        }

        progressBar.setVisibility(View.GONE);
        multiple.setVisibility(View.GONE);
        anime.setVisibility(View.VISIBLE);
    }

    private void loadlist(WebModel animes) {

        multiple.invalidate();

        TextView emptyeps =  multiple.findViewById(R.id.episodeIndicator2);
        TextView emptyanime = multiple.findViewById(R.id.animeindicator);

        RecyclerView multipleEps = multiple.findViewById(R.id.episodeRecycler);
        RecyclerView multipleAnime = multiple.findViewById(R.id.animeRecycler);

        ArrayList<MiniModel> animelist = animes.getAnimes();
        ArrayList<MiniModel> epslist = animes.getEpisodes();

        emptyeps.setVisibility(epslist.isEmpty() ? View.VISIBLE : View.GONE);
        emptyanime.setVisibility(animelist.isEmpty() ? View.VISIBLE : View.GONE);

        multipleAnime.setAdapter(new AnimeAdapter(animelist, new AnimeAdapter.ItemClickListener() {
            @Override
            public void onItemClick(MiniModel anime) {
                Toast.makeText(MainActivity.this, "Descripcion: "+anime.getDescription()+"\nLink: "+anime.getLink()+"\n Click Largo para copiar", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLongItemClick(String text) {
                copy(text);
            }
        }));

        multipleEps.setAdapter(new EpisodeAdapter(epslist, new EpisodeAdapter.ItemClickListener() {
            @Override
            public void onItemClick(MiniModel episode) {
                Toast.makeText(MainActivity.this, "Link: "+episode.getLink()+"\n Click Largo para copiar", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onLongItemClick(String text) {
                    copy(text);
                }
        }));

        progressBar.setVisibility(View.GONE);
        anime.setVisibility(View.GONE);
        multiple.setVisibility(View.VISIBLE);
    }

    private void copy (String text){
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(text, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "Copiado!", Toast.LENGTH_SHORT).show();

    }
}