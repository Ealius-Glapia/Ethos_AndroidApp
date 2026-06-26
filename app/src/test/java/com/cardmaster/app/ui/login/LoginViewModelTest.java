package com.cardmaster.app.ui.login;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.cardmaster.app.data.preferences.UserPreferencesManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LoginViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private UserPreferencesManager preferencesManager;

    private LoginViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new LoginViewModel(preferencesManager);
    }

    @Test
    public void testLoginSuccess() {
        viewModel.login("testuser");
        verify(preferencesManager).saveUsername("testuser");
        assertTrue(viewModel.getNavigateToMain().getValue());
    }

    @Test
    public void testLoginEmptyUsername() {
        viewModel.login("");
        verify(preferencesManager, never()).saveUsername(anyString());
        assertNotNull(viewModel.getErrorMessage().getValue());
    }

    @Test
    public void testLoginNullUsername() {
        viewModel.login(null);
        verify(preferencesManager, never()).saveUsername(anyString());
        assertNotNull(viewModel.getErrorMessage().getValue());
    }
}
