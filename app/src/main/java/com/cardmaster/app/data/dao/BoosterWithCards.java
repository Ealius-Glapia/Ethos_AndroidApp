package com.cardmaster.app.data.dao;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Relation;

import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.entity.Card;

import java.util.List;

public class BoosterWithCards {
    @Embedded
    public Booster booster;

    @Relation(parentColumn = "id", entityColumn = "boosterId")
    public List<Card> cards;

    public BoosterWithCards() {
    }

    @Ignore
    public BoosterWithCards(Booster booster, List<Card> cards) {
        this.booster = booster;
        this.cards = cards;
    }
}
