package com.eme22.animeparseresexample.adapters;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.eme22.animeparseres.Model.MiniModel;
import com.eme22.animeparseresexample.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.AnimeAdapterViewHolder>{

    private final ArrayList<MiniModel> animes;
    private final ItemClickListener mClickListener;

    public AnimeAdapter(ArrayList<MiniModel> animes, ItemClickListener mClickListener) {
        this.animes = animes;
        this.mClickListener = mClickListener;
    }

    @NonNull
    @Override
    public AnimeAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.anime,parent,false);
        return new AnimeAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimeAdapter.AnimeAdapterViewHolder holder, int position) {
        MiniModel item = animes.get(position);
        Log.d("image-aaa", item.getImage()+" aaa");
        try {
            Picasso.get().load(item.getImage()).placeholder(new CircularProgressDrawable(holder.image.getContext())).into(holder.image);
        }
        catch (Exception e) {e.printStackTrace();}
        holder.title.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return animes.size();
    }

    public class AnimeAdapterViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView title;

        public AnimeAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_view_title_outside);
            image = itemView.findViewById(R.id.image_view_anime_outside);
            itemView.setOnClickListener(v -> mClickListener.onItemClick(animes.get(getAdapterPosition())));
            itemView.setOnLongClickListener(v -> {
                mClickListener.onLongItemClick(animes.get(getAdapterPosition()).getLink());
                return true;
            });
        }
    }

    public interface ItemClickListener {
        void onItemClick(MiniModel anime);
        void onLongItemClick(String text);
    }

}
