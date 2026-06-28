package com.cardmaster.app.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateChecker {
    private static final String GITHUB_API_URL = "https://api.github.com/repos/Ealius-Glapia/Ethos_AndroidApp/releases/latest";
    private static final String TAG = "UpdateChecker";

    public interface UpdateCheckCallback {
        void onUpdateAvailable(String latestVersion, String downloadUrl);
        void onNoUpdateAvailable();
        void onError(String error);
    }

    private final ExecutorService executor;
    private final Handler mainHandler;

    public UpdateChecker() {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void checkForUpdates(String currentVersion, UpdateCheckCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(GITHUB_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject releaseJson = new JSONObject(response.toString());
                    String latestVersion = releaseJson.getString("tag_name");
                    String downloadUrl = null;

                    // Get the APK download URL from assets
                    JSONArray assets = releaseJson.getJSONArray("assets");
                    if (assets.length() > 0) {
                        JSONObject firstAsset = assets.getJSONObject(0);
                        downloadUrl = firstAsset.getString("browser_download_url");
                    }

                    Log.d(TAG, "Current version: " + currentVersion + ", Latest version: " + latestVersion);

                    // Compare versions (remove 'v' prefix if present)
                    String cleanLatest = latestVersion.replace("v", "").trim();
                    String cleanCurrent = currentVersion.trim();

                    if (!cleanLatest.equals(cleanCurrent)) {
                        // Update available
                        String finalDownloadUrl = downloadUrl;
                        mainHandler.post(() -> callback.onUpdateAvailable(latestVersion, finalDownloadUrl));
                    } else {
                        // No update available
                        mainHandler.post(() -> callback.onNoUpdateAvailable());
                    }
                } else {
                    Log.e(TAG, "HTTP error: " + responseCode);
                    mainHandler.post(() -> callback.onError("HTTP error: " + responseCode));
                }
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error checking for updates", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
