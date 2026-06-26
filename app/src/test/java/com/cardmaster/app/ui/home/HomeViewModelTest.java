package com.cardmaster.app.ui.home;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.repository.BoosterRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;

public class HomeViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private BoosterRepository boosterRepository;

    private HomeViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new HomeViewModel(boosterRepository);
    }

    @Test
    public void testGetBoosters() {
        viewModel.getBoosters();
        verify(boosterRepository).getAllBoosters();
    }
}
