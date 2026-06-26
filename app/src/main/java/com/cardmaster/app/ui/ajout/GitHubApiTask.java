package com.cardmaster.app.ui.ajout;

import com.cardmaster.app.data.entity.GitHubRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubApiTask {

    public interface ZipFilesCallback {
        void onZipFilesFetched(List<String> files);
        void onError(String error);
    }

    public void fetchZipFiles(GitHubRepository repository, ZipFilesCallback callback) {
        new Thread(() -> {
            try {
                String apiUrl = extractGitHubApiUrl(repository.getUrl());
                if (apiUrl == null) {
                    callback.onError("Invalid GitHub URL");
                    return;
                }

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    List<String> zipFiles = parseZipFiles(response.toString());
                    callback.onZipFilesFetched(zipFiles);
                } else {
                    callback.onError("Failed to fetch repository: " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    private String extractGitHubApiUrl(String githubUrl) {
        // Extract owner and repo from URL like https://github.com/owner/repo
        Pattern pattern = Pattern.compile("github\\.com/([^/]+)/([^/]+)");
        Matcher matcher = pattern.matcher(githubUrl);
        if (matcher.find()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2).replace(".git", "");
            return "https://api.github.com/repos/" + owner + "/" + repo + "/contents/";
        }
        return null;
    }

    private List<String> parseZipFiles(String jsonResponse) {
        List<String> zipFiles = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                String name = item.getString("name");
                String type = item.getString("type");
                if (type.equals("file") && name.endsWith(".zip")) {
                    zipFiles.add(name);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return zipFiles;
    }
}
