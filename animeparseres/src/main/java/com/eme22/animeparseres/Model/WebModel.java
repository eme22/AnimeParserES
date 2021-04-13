package com.eme22.animeparseres.Model;

import java.util.ArrayList;

public class WebModel {

    Model.SERVER server;
    String name;
    ArrayList<MiniModel> animes;
    ArrayList<MiniModel> episodes;

    public Model.SERVER getServer() { return server; }

    public void setServer(Model.SERVER server) { this.server = server; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<MiniModel> getAnimes() {
        return animes;
    }

    public void setAnimes(ArrayList<MiniModel> animes) {
        this.animes = animes;
    }

    public ArrayList<MiniModel> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(ArrayList<MiniModel> episodes) {
        this.episodes = episodes;
    }
}
