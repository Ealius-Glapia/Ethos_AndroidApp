package com.cardmaster.app.ui.ajout;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;
import com.cardmaster.app.data.dao.BoosterDao;
import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.entity.GitHubRepository;
import com.cardmaster.app.data.preferences.UserPreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class AjoutFragment extends Fragment {

    private RecyclerView repositoryRecyclerView;
    private RepositoryAdapter adapter;
    private UserPreferencesManager preferencesManager;
    private List<GitHubRepository> repositories;
    private List<GitHubRepository> filteredRepositories;
    private EditText searchEditText;
    private BoosterDao boosterDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ajout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!isNetworkAvailable()) {
            Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            return;
        }

        CardMasterApplication app = CardMasterApplication.getInstance();
        preferencesManager = new UserPreferencesManager(requireContext());
        boosterDao = app.getDatabase().boosterDao();

        repositoryRecyclerView = view.findViewById(R.id.repository_recycler_view);
        Button addButton = view.findViewById(R.id.add_repository_button);
        searchEditText = view.findViewById(R.id.search_edit_text);

        repositories = new ArrayList<>();
        filteredRepositories = new ArrayList<>();
        setupRecyclerView();
        loadRepositories();
        setupSearch();

        addButton.setOnClickListener(v -> showAddRepositoryDialog());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void setupRecyclerView() {
        adapter = new RepositoryAdapter(filteredRepositories, this::onRepositoryClicked, this::onDeleteClicked);
        repositoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        repositoryRecyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRepositories(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterRepositories(String query) {
        filteredRepositories.clear();
        if (query.isEmpty()) {
            filteredRepositories.addAll(repositories);
        } else {
            String lowerQuery = query.toLowerCase();
            for (GitHubRepository repo : repositories) {
                if (repo.getUrl().toLowerCase().contains(lowerQuery)) {
                    filteredRepositories.add(repo);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadRepositories() {
        preferencesManager.getGitHubRepositories(repositories -> {
            this.repositories.clear();
            this.repositories.addAll(repositories);
            filterRepositories(searchEditText.getText().toString());
        });
    }

    private void showAddRepositoryDialog() {
        AddRepositoryDialog dialog = new AddRepositoryDialog();
        dialog.setListener((url, password) -> {
            if (isValidUrl(url) && !password.isEmpty()) {
                GitHubRepository repository = new GitHubRepository(url, password);
                repositories.add(repository);
                filterRepositories(searchEditText.getText().toString());
                saveRepositories();
            } else {
                Toast.makeText(getContext(), R.string.invalid_repository_url, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getChildFragmentManager(), "AddRepositoryDialog");
    }

    private boolean isValidUrl(String url) {
        return url.contains("https") && url.contains("github");
    }

    private void saveRepositories() {
        preferencesManager.saveGitHubRepositories(repositories);
    }

    private void onRepositoryClicked(GitHubRepository repository) {
        if (isNetworkAvailable()) {
            ZipFilesFragment fragment = ZipFilesFragment.newInstance(repository);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    private void onDeleteClicked(GitHubRepository repository, int position) {
        // Check if repository has boosters that are not hard deleted
        new Thread(() -> {
            List<Booster> allBoosters = boosterDao.getAllBoostersSync();
            final boolean hasActiveBoosters = checkForActiveBoosters(allBoosters);
            
            requireActivity().runOnUiThread(() -> {
                if (hasActiveBoosters) {
                    // Show error message
                    Toast.makeText(getContext(), R.string.cannot_delete_link, Toast.LENGTH_LONG).show();
                } else {
                    // Show delete confirmation dialog
                    DeleteConfirmationDialog dialog = DeleteConfirmationDialog.newInstance(repository.getUrl());
                    dialog.setListener(() -> {
                        int actualPosition = repositories.indexOf(repository);
                        if (actualPosition >= 0) {
                            repositories.remove(actualPosition);
                            filterRepositories(searchEditText.getText().toString());
                            saveRepositories();
                        }
                    });
                    dialog.show(getChildFragmentManager(), "DeleteConfirmationDialog");
                }
            });
        }).start();
    }

    private boolean checkForActiveBoosters(List<Booster> boosters) {
        for (Booster booster : boosters) {
            String status = booster.getStatus();
            if (status == null || "active".equals(status)) {
                return true;
            }
        }
        return false;
    }
}
