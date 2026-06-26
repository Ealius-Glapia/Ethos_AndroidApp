package com.cardmaster.app.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.repository.BoosterRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private final BoosterRepository boosterRepository;
    private final LiveData<List<Booster>> activeBoosters;

    public HomeViewModel(BoosterRepository boosterRepository) {
        this.boosterRepository = boosterRepository;
        this.activeBoosters = Transformations.map(boosterRepository.getAllBoosters(), boosters -> {
            List<Booster> filtered = new ArrayList<>();
            if (boosters != null) {
                for (Booster booster : boosters) {
                    if ("active".equals(booster.getStatus())) {
                        filtered.add(booster);
                    }
                }
            }
            return filtered;
        });
    }

    public LiveData<List<Booster>> getBoosters() {
        return activeBoosters;
    }
}
