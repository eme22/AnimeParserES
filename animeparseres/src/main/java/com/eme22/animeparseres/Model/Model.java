package com.eme22.animeparseres.Model;

import android.util.Pair;

import java.util.ArrayList;

public class Model{

    public enum TYPES {
        ANIME, OVA, ESPECIAL, PELICULA, ONA, ESPANIOL
    }

    public enum SERVER {
        ANIMEFLV, JKANIME, ANIMEID, TIOANIME, ANIMEFLVRU
    }

    // ANIME
    int internalID = 0;
    int malid = 0;
    String name;
    String[] alternativeNames;
    String image;
    String details;
    TYPES animeType;
    String url;
    ArrayList<MiniModel> episodes;
    ArrayList<Pair<String,String>> categories;
    double punctuation = 0;

    //EPISODE

    int episode = -1;
    ArrayList<Pair<String,String>> episodeLinks;

    //COMMON

    String socialURL;
    SERVER server;


    public int getInternalID() {
        return internalID;
    }

    public void setInternalID(int internalID) {
        this.internalID = internalID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getAlternativeNames() {
        return alternativeNames;
    }

    public void setAlternativeNames(String[] alternativeNames) {
        this.alternativeNames = alternativeNames;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public TYPES getAnimeType() {
        return animeType;
    }

    public void setAnimeType(TYPES animeType) {
        this.animeType = animeType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<MiniModel> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(ArrayList<MiniModel> episodes) {
        this.episodes = episodes;
    }

    public ArrayList<Pair<String, String>> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Pair<String, String>> categories) {
        this.categories = categories;
    }

    public Double getPunctuation() {
        return punctuation;
    }

    public void setPunctuation(double punctuation) {
        this.punctuation = punctuation;
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public ArrayList<Pair<String, String>> getEpisodeLinks() {
        return episodeLinks;
    }

    public void setEpisodeLinks(ArrayList<Pair<String, String>> episodeLinks) {
        this.episodeLinks = episodeLinks;
    }

    public String getSocialURL() {
        return socialURL;
    }

    public void setSocialURL(String socialURL) {
        this.socialURL = socialURL;
    }

    public SERVER getServer() { return server; }

    public void setServer(SERVER server) { this.server = server; }

    public int getMalid() {
        return malid;
    }

    public void setMalid(int malid) {
        this.malid = malid;
    }
}
