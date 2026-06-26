package com.cardmaster.app.ui.ajout;

import android.content.Context;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.entity.Card;
import com.cardmaster.app.data.entity.GitHubRepository;

import net.lingala.zip4j.ZipFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ZipDownloadTask {

    private final Context context;

    public ZipDownloadTask(Context context) {
        this.context = context;
    }

    public interface DownloadCallback {
        void onDownloadComplete(String extractedPath);
        void onError(String error);
    }

    public void downloadAndExtractZip(GitHubRepository repository, String zipFileName, DownloadCallback callback) {
        new Thread(() -> {
            try {
                String downloadUrl = extractDownloadUrl(repository.getUrl(), zipFileName);
                if (downloadUrl == null) {
                    callback.onError("Invalid GitHub URL");
                    return;
                }

                // Download zip file
                File tempZipFile = new File(context.getCacheDir(), zipFileName);
                downloadFile(downloadUrl, tempZipFile);

                // Extract zip file with password using Zip4j
                File extractDir = new File(context.getFilesDir(), zipFileName.replace(".zip", ""));
                extractZipFile(tempZipFile, extractDir, repository.getPassword());

                // Process extracted files
                processExtractedFiles(extractDir, zipFileName);

                // Clean up
                tempZipFile.delete();

                callback.onDownloadComplete(extractDir.getAbsolutePath());
            } catch (Exception e) {
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    private String extractDownloadUrl(String githubUrl, String zipFileName) {
        // Convert GitHub URL to raw content URL
        // https://github.com/owner/repo -> https://raw.githubusercontent.com/owner/repo/main/zipFileName
        String[] parts = githubUrl.split("github\\.com/");
        if (parts.length < 2) return null;

        String repoPath = parts[1].replace(".git", "");
        String[] repoParts = repoPath.split("/");
        if (repoParts.length < 2) return null;

        String owner = repoParts[0];
        String repo = repoParts[1];
        return "https://raw.githubusercontent.com/" + owner + "/" + repo + "/main/" + zipFileName;
    }

    private void downloadFile(String urlString, File outputFile) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        InputStream inputStream = connection.getInputStream();
        OutputStream outputStream = new FileOutputStream(outputFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();
        connection.disconnect();
    }

    private void extractZipFile(File zipFile, File destDir, String password) throws Exception {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        ZipFile zipFileObj = new ZipFile(zipFile);
        
        if (password != null && !password.isEmpty()) {
            zipFileObj.setPassword(password.toCharArray());
        }
        
        zipFileObj.extractAll(destDir.getAbsolutePath());
    }

    private void processExtractedFiles(File extractDir, String zipFileName) {
        try {
            android.util.Log.d("ZipDownload", "Processing extracted files from: " + extractDir.getAbsolutePath());
            
            // Read manifest.json
            File manifestFile = new File(extractDir, "manifest.json");
            if (!manifestFile.exists()) {
                android.util.Log.e("ZipDownload", "manifest.json not found in: " + extractDir.getAbsolutePath());
                return;
            }

            String manifestContent = readFileContent(manifestFile);
            JSONObject manifest = new JSONObject(manifestContent);

            // Extract booster information
            int boosterId = manifest.getInt("booster_id");
            String nameFromManifest = manifest.getString("name");
            int totalCards = manifest.getInt("total_cards");
            
            // Use zip file name (without .zip) as booster name to match with GitHub
            String name = zipFileName.replace(".zip", "");
            
            android.util.Log.d("ZipDownload", "Booster ID: " + boosterId + ", Name from manifest: " + nameFromManifest + ", Name from zip: " + name + ", Total Cards: " + totalCards);
            
            // Copy booster.webp to app's files directory and get absolute path
            File boosterImageSource = new File(extractDir, "booster.webp");
            File boosterImagesDir = new File(context.getFilesDir(), "boosters");
            if (!boosterImagesDir.exists()) {
                boosterImagesDir.mkdirs();
            }
            File boosterImageDest = new File(boosterImagesDir, "booster_" + boosterId + ".webp");
            
            if (boosterImageSource.exists()) {
                copyFile(boosterImageSource, boosterImageDest);
                android.util.Log.d("ZipDownload", "Copied booster image to: " + boosterImageDest.getAbsolutePath());
            } else {
                android.util.Log.w("ZipDownload", "booster.webp not found");
            }
            
            String artworkUrl = boosterImageDest.getAbsolutePath();

            // Create booster entity
            Booster booster = new Booster(boosterId, name, artworkUrl, totalCards, "");

            // Extract cards information
            JSONArray cardsArray = manifest.getJSONArray("cards");
            List<Card> cards = new ArrayList<>();
            
            // Create cards directory
            File cardsDir = new File(context.getFilesDir(), "cards");
            if (!cardsDir.exists()) {
                cardsDir.mkdirs();
            }
            android.util.Log.d("ZipDownload", "Cards directory: " + cardsDir.getAbsolutePath() + ", exists: " + cardsDir.exists());

            android.util.Log.d("ZipDownload", "Found " + cardsArray.length() + " cards in manifest");

            for (int i = 0; i < cardsArray.length(); i++) {
                JSONObject cardJson = cardsArray.getJSONObject(i);
                int cardId = cardJson.getInt("card_id");
                String cardName = cardJson.getString("card_name");
                String personName = cardJson.getString("person_name");
                int rarity = cardJson.getInt("rarity");
                int number = cardJson.getInt("upgrade");
                String image = cardJson.getString("image");

                // Copy card image to files directory
                File cardImageSource = new File(extractDir, image);
                File cardImageDest = new File(cardsDir, "card_" + cardId + "_" + boosterId + ".webp");

                android.util.Log.d("ZipDownload", "Card image source: " + cardImageSource.getAbsolutePath() + ", exists: " + cardImageSource.exists());
                android.util.Log.d("ZipDownload", "Card image dest: " + cardImageDest.getAbsolutePath());

                if (cardImageSource.exists()) {
                    copyFile(cardImageSource, cardImageDest);
                    android.util.Log.d("ZipDownload", "Copied card image to: " + cardImageDest.getAbsolutePath() + ", exists after copy: " + cardImageDest.exists());
                } else {
                    android.util.Log.w("ZipDownload", "Card image not found: " + cardImageSource.getAbsolutePath());
                }
                
                Card card = new Card(cardId, boosterId, cardName, cardImageDest.getAbsolutePath(), String.valueOf(rarity), number, personName);
                cards.add(card);
                android.util.Log.d("ZipDownload", "Card: " + cardId + ", " + cardName + ", rarity: " + rarity + ", upgrade: " + number);
            }

            // Insert into database
            CardMasterApplication app = CardMasterApplication.getInstance();
            android.util.Log.d("ZipDownload", "Inserting booster and " + cards.size() + " cards into database");
            
            // Insert booster first synchronously to ensure foreign key constraint
            // Using IGNORE to preserve existing data
            app.getBoosterRepository().insertBoosterSync(booster);
            
            // Update booster status to active if it was hard deleted
            app.getBoosterRepository().updateStatus(boosterId, "active");
            
            app.getCardRepository().insertCards(cards);
            android.util.Log.d("ZipDownload", "Insertion complete");

        } catch (Exception e) {
            android.util.Log.e("ZipDownload", "Error processing extracted files", e);
            e.printStackTrace();
        }
    }

    private void copyFile(File source, File dest) throws Exception {
        java.io.FileInputStream fis = new java.io.FileInputStream(source);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(dest);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }
        fis.close();
        fos.close();
    }

    private String readFileContent(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return new String(data, "UTF-8");
    }
}
