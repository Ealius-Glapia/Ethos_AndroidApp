package com.cardmaster.app.ui.cardviewer;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.entity.Card;
import com.cardmaster.app.data.repository.CardRepository;

public class CardViewerViewModel extends ViewModel {
    private final CardRepository cardRepository;
    private int cardId;

    public CardViewerViewModel(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public int getCardId() {
        return cardId;
    }

    public LiveData<Card> getCard() {
        return cardRepository.getCardById(cardId);
    }
}
