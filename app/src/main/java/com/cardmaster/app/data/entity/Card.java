package com.cardmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;

import java.io.Serializable;

@Entity(
    tableName = "cards",
    foreignKeys = @ForeignKey(
        entity = Booster.class,
        parentColumns = "id",
        childColumns = "boosterId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index(value = "boosterId")
)
public class Card implements Serializable {
    @PrimaryKey
    private int id;
    private int boosterId;
    private String name;
    private String imageUrl;
    private String rarity;
    private int number;
    private String description;

    public Card(int id, int boosterId, String name, String imageUrl, String rarity, int number, String description) {
        this.id = id;
        this.boosterId = boosterId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.rarity = rarity;
        this.number = number;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBoosterId() {
        return boosterId;
    }

    public void setBoosterId(int boosterId) {
        this.boosterId = boosterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
