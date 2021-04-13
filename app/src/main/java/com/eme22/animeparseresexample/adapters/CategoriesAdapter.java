package com.eme22.animeparseresexample.adapters;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eme22.animeparseresexample.R;

import java.util.ArrayList;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesAdapterViewHolder> {

    private final ArrayList<Pair<String,String>> categories;
    private final ItemClickListener mClickListener;

    public CategoriesAdapter(ArrayList<Pair<String, String>> categories, ItemClickListener mClickListener) {

        this.categories = categories;
        this.mClickListener = mClickListener;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return categories.size();
    }

    @NonNull
    @Override
    public CategoriesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category,parent,false);
        return new CategoriesAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesAdapter.CategoriesAdapterViewHolder holder, int position) {
        holder.title.setText(categories.get(position).first);
    }

    public class CategoriesAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public CategoriesAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.catname);
            itemView.setOnClickListener(v -> mClickListener.onItemClick(categories.get(getAdapterPosition())));
            itemView.setOnLongClickListener(v -> {
                mClickListener.onLongItemClick(categories.get(getAdapterPosition()).second);
                return true;
            });
        }
    }

    public interface ItemClickListener {
        void onItemClick(Pair<String, String> category);
        void onLongItemClick(String text);
    }

}
