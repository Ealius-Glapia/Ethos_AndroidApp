package com.cardmaster.app.ui.reveal;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.entity.Card;
import com.cardmaster.app.data.entity.OwnedCard;
import com.cardmaster.app.data.repository.OwnedCardRepository;

import java.util.ArrayList;
import java.util.List;

public class RevealViewModel extends ViewModel {
    private final OwnedCardRepository ownedCardRepository;
    private final MutableLiveData<List<Card>> cards = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentCardIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> allRevealed = new MutableLiveData<>(false);
    private List<Boolean> revealedStates;

    public RevealViewModel(OwnedCardRepository ownedCardRepository) {
        this.ownedCardRepository = ownedCardRepository;
    }

    public void setCards(List<Card> cards) {
        this.cards.setValue(cards);
        revealedStates = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            revealedStates.add(false);
        }
    }

    public LiveData<List<Card>> getCards() {
        return cards;
    }

    public LiveData<Integer> getCurrentCardIndex() {
        return currentCardIndex;
    }

    public LiveData<Boolean> getAllRevealed() {
        return allRevealed;
    }

    public void revealCard(int index) {
        if (index < revealedStates.size()) {
            revealedStates.set(index, true);
            currentCardIndex.setValue(index);
            
            List<Card> cardList = cards.getValue();
            if (cardList != null && index == cardList.size() - 1) {
                allRevealed.setValue(true);
                saveCards(cardList);
            }
        }
    }

    private void saveCards(List<Card> cards) {
        for (Card card : cards) {
            OwnedCard ownedCard = new OwnedCard(card.getId(), System.currentTimeMillis(), 1);
            ownedCardRepository.insertOwnedCard(ownedCard);
        }
    }
}
