package com.cardmaster.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
    tableName = "owned_cards",
    foreignKeys = @ForeignKey(
        entity = Card.class,
        parentColumns = "id",
        childColumns = "cardId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("cardId")
)
public class OwnedCard {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private int cardId;
    private long obtainedAt;
    private int quantity;

    public OwnedCard(int cardId, long obtainedAt, int quantity) {
        this.cardId = cardId;
        this.obtainedAt = obtainedAt;
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public long getObtainedAt() {
        return obtainedAt;
    }

    public void setObtainedAt(long obtainedAt) {
        this.obtainedAt = obtainedAt;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
