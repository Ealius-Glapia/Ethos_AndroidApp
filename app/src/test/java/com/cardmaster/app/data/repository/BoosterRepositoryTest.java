package com.cardmaster.app.data.repository;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.cardmaster.app.data.dao.BoosterDao;
import com.cardmaster.app.data.entity.Booster;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BoosterRepositoryTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private BoosterDao boosterDao;

    private BoosterRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new BoosterRepository(boosterDao);
    }

    @After
    public void tearDown() {
        repository.shutdown();
    }

    @Test
    public void testGetAllBoosters() throws InterruptedException {
        List<Booster> boosters = Arrays.asList(
                new Booster(1, "Pack 1", "url1", 70, "2024-01-01"),
                new Booster(2, "Pack 2", "url2", 80, "2024-02-01")
        );

        LiveData<List<Booster>> liveData = new androidx.lifecycle.MutableLiveData<>(boosters);
        when(boosterDao.getAllBoosters()).thenReturn(liveData);

        LiveData<List<Booster>> result = repository.getAllBoosters();

        CountDownLatch latch = new CountDownLatch(1);
        result.observeForever(new Observer<List<Booster>>() {
            @Override
            public void onChanged(List<Booster> boosterList) {
                latch.countDown();
            }
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(boosterDao).getAllBoosters();
    }

    @Test
    public void testInsertBooster() {
        Booster booster = new Booster(1, "Pack 1", "url1", 70, "2024-01-01");
        repository.insertBooster(booster);
        verify(boosterDao).insert(booster);
    }
}
