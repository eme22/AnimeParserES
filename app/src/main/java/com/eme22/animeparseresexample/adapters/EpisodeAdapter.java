package com.eme22.animeparseresexample.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.eme22.animeparseres.AnimeParserES;
import com.eme22.animeparseres.Model.MiniModel;
import com.eme22.animeparseresexample.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeAdapterViewHolder>{

    private final ArrayList<MiniModel> episodes;
    private final ItemClickListener mClickListener;

    public EpisodeAdapter(ArrayList<MiniModel> episodes, ItemClickListener mClickListener) {
        Log.d("Episodes", String.valueOf(episodes.size()));
        this.episodes = episodes;
        this.mClickListener = mClickListener;
    }

    @NonNull
    @Override
    public EpisodeAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.episode,parent,false);
        return new EpisodeAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeAdapter.EpisodeAdapterViewHolder holder, int position) {
        MiniModel item = episodes.get(position);
        Log.d("image", item.getImage());

        /*
        String cookies = AnimeParserES.getInstance().getFlvCookies();
        if (cookies != null){
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("cookie", cookies)
                                .build();
                        return chain.proceed(newRequest);
                    }).build();

            Picasso picasso = new Picasso.Builder(holder.image.getContext())
                    .downloader(new OkHttp3Downloader(client))
                    .build();
            try {
                picasso.load(item.getImage()).placeholder(new CircularProgressDrawable(holder.image.getContext())).into(holder.image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("BYPASS:?", e.getLocalizedMessage());
                    }
                });
            }
            catch (Exception e) {e.printStackTrace();}
        }
        else {

            try {
                Picasso.get().load(item.getImage()).placeholder(new CircularProgressDrawable(holder.image.getContext())).into(holder.image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("BYPASS:?", e.getLocalizedMessage());
                    }
                });
            }
            catch (Exception e) {e.printStackTrace();}
        }
        */


        Picasso.get().load(item.getImage()).placeholder(new CircularProgressDrawable(holder.image.getContext())).into(holder.image);
        holder.title.setText(item.getTitle()+" - Episodio: "+ item.getEpisode());
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    public class EpisodeAdapterViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView title;

        public EpisodeAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_view_title_outside2);
            image = itemView.findViewById(R.id.image_view_anime_outside2);
            itemView.setOnClickListener(v -> mClickListener.onItemClick(episodes.get(getAdapterPosition())));
            itemView.setOnLongClickListener(v -> {
                mClickListener.onLongItemClick(episodes.get(getAdapterPosition()).getLink());
                return true;
            });
        }
    }

    public interface ItemClickListener {
        void onItemClick(MiniModel episode);
        void onLongItemClick(String text);
    }
}
