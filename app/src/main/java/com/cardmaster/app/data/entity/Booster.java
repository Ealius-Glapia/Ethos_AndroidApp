package com.cardmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "boosters")
public class Booster implements Serializable {
    @PrimaryKey
    private int id;
    private String name;
    private String artworkUrl;
    private int totalCards;
    private String releaseDate;
    private String status; // "active", "soft_deleted", "hard_deleted"

    public Booster(int id, String name, String artworkUrl, int totalCards, String releaseDate) {
        this.id = id;
        this.name = name;
        this.artworkUrl = artworkUrl;
        this.totalCards = totalCards;
        this.releaseDate = releaseDate;
        this.status = "active";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public int getTotalCards() {
        return totalCards;
    }

    public void setTotalCards(int totalCards) {
        this.totalCards = totalCards;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
