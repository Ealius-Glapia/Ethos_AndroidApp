package com.cardmaster.app.data.repository;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.cardmaster.app.data.dao.CardDao;
import com.cardmaster.app.data.entity.Card;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class CardRepositoryTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private CardDao cardDao;

    private CardRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new CardRepository(cardDao);
    }

    @After
    public void tearDown() {
        repository.shutdown();
    }

    @Test
    public void testGetCardById() {
        repository.getCardById(1);
        verify(cardDao).getCardById(1);
    }

    @Test
    public void testInsertCard() {
        Card card = new Card(1, 1, "Card 1", "url", "Common", 1, "Description");
        repository.insertCard(card);
        verify(cardDao).insert(card);
    }
}
