package com.cardmaster.app.ui.boosterselection;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;

import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.entity.OwnedCard;
import com.cardmaster.app.data.repository.BoosterRepository;
import com.cardmaster.app.data.repository.OwnedCardRepository;

import java.util.List;

public class BoosterSelectionViewModel extends ViewModel {
    private final BoosterRepository boosterRepository;
    private final OwnedCardRepository ownedCardRepository;
    private int boosterId;

    public BoosterSelectionViewModel(BoosterRepository boosterRepository, OwnedCardRepository ownedCardRepository) {
        this.boosterRepository = boosterRepository;
        this.ownedCardRepository = ownedCardRepository;
    }

    public void setBoosterId(int boosterId) {
        this.boosterId = boosterId;
    }

    public int getBoosterId() {
        return boosterId;
    }

    public LiveData<Booster> getBooster() {
        return boosterRepository.getBoosterById(boosterId);
    }

    public LiveData<Integer> getTotalUniqueCards() {
        return ownedCardRepository.getTotalUniqueCards();
    }
}
