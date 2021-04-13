package com.eme22.animeparseres.Model;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class MiniModel implements Comparable<MiniModel> {

    Model.TYPES type;

    String title,description,link,image;
    int episode = -1;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Model.TYPES getType() {
        return type;
    }

    public void setType(Model.TYPES type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public static DiffUtil.ItemCallback<MiniModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<MiniModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull MiniModel oldItem, @NonNull MiniModel newItem) {
            if (oldItem.title.equals(newItem.title)){
                if (oldItem.type.equals(newItem.type)) return true;
                else return oldItem.episode == newItem.episode;
            }
            else return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MiniModel oldItem, @NonNull MiniModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass())
            return false;

        if (obj == this)
            return true;

        MiniModel obj1 = (MiniModel) obj;

        if (this.title.equals(obj1.title)){
            if (this.type != null){
                return this.type.equals(obj1.type);
            }
            else return this.episode == obj1.episode;
        }
        else return false;

    }


    @Override
    public int compareTo(MiniModel o) {
        if (episode == -1) return 0;
        else return Integer.compare(this.episode, o.episode);
    }
}
