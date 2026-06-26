package com.cardmaster.app.data.preferences;

import android.content.Context;

import com.cardmaster.app.data.entity.GitHubRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserPreferencesManager {

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserPreferencesManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void saveUsername(String username) {
        executor.execute(() -> {
            try {
                // For Java, we'll use SharedPreferences as fallback since DataStore requires Kotlin coroutines
                android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                prefs.edit().putString("username", username).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getUsername(UsernameCallback callback) {
        executor.execute(() -> {
            try {
                android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String username = prefs.getString("username", null);
                callback.onUsernameLoaded(username);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onUsernameLoaded(null);
            }
        });
    }

    public boolean hasUsername() {
        android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        return prefs.contains("username");
    }

    public void saveGitHubRepositories(List<GitHubRepository> repositories) {
        executor.execute(() -> {
            try {
                android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                JSONArray jsonArray = new JSONArray();
                for (GitHubRepository repo : repositories) {
                    JSONObject obj = new JSONObject();
                    obj.put("url", repo.getUrl());
                    obj.put("password", repo.getPassword());
                    jsonArray.put(obj);
                }
                prefs.edit().putString("github_repositories", jsonArray.toString()).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getGitHubRepositories(RepositoriesCallback callback) {
        executor.execute(() -> {
            try {
                android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String json = prefs.getString("github_repositories", null);
                List<GitHubRepository> repositories = new ArrayList<>();
                if (json != null) {
                    JSONArray jsonArray = new JSONArray(json);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String url = obj.getString("url");
                        String password = obj.getString("password");
                        repositories.add(new GitHubRepository(url, password));
                    }
                }
                callback.onRepositoriesLoaded(repositories);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onRepositoriesLoaded(new ArrayList<>());
            }
        });
    }

    public interface UsernameCallback {
        void onUsernameLoaded(String username);
    }

    public interface RepositoriesCallback {
        void onRepositoriesLoaded(List<GitHubRepository> repositories);
    }

    public void saveLanguage(String language) {
        executor.execute(() -> {
            try {
                android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                prefs.edit().putString("language", language).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void getLanguage(LanguageCallback callback) {
        executor.execute(() -> {
            try {
                android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String language = prefs.getString("language", "en");
                callback.onLanguageLoaded(language);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onLanguageLoaded("en");
            }
        });
    }

    public interface LanguageCallback {
        void onLanguageLoaded(String language);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
