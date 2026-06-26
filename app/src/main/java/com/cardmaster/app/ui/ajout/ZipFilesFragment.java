package com.cardmaster.app.ui.ajout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.CardMasterApplication;
import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.entity.GitHubRepository;

import java.util.ArrayList;
import java.util.List;

public class ZipFilesFragment extends Fragment {

    private static final String ARG_REPOSITORY = "repository";

    private GitHubRepository repository;
    private RecyclerView zipRecyclerView;
    private ZipFileAdapter adapter;
    private List<String> zipFiles;
    private List<String> filteredZipFiles;
    private EditText searchBoostersEditText;
    private com.google.android.material.floatingactionbutton.FloatingActionButton addRandomBoosterButton;
    private java.util.Set<String> downloadedZipFiles;
    private java.util.Set<String> softDeletedZipFiles;
    private java.util.Set<String> hardDeletedZipFiles;

    public static ZipFilesFragment newInstance(GitHubRepository repository) {
        ZipFilesFragment fragment = new ZipFilesFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REPOSITORY, repository);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_zip_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            repository = (GitHubRepository) getArguments().getSerializable(ARG_REPOSITORY);
        }

        zipRecyclerView = view.findViewById(R.id.booster_recycler_view);
        TextView titleTextView = view.findViewById(R.id.title_text);
        searchBoostersEditText = view.findViewById(R.id.search_boosters_edit_text);
        addRandomBoosterButton = view.findViewById(R.id.add_random_booster_button);

        // Display name/repo instead of URL
        String[] userRepo = parseGitHubUrl(repository != null ? repository.getUrl() : "");
        if (userRepo != null) {
            titleTextView.setText(userRepo[0] + "/" + userRepo[1]);
        } else {
            titleTextView.setText(repository != null ? repository.getUrl() : "");
        }
        
        zipFiles = new ArrayList<>();
        filteredZipFiles = new ArrayList<>();
        downloadedZipFiles = new java.util.HashSet<>();
        softDeletedZipFiles = new java.util.HashSet<>();
        hardDeletedZipFiles = new java.util.HashSet<>();
        setupRecyclerView();
        setupSearch();
        setupAddRandomBoosterButton();
        fetchZipFiles();
    }

    private String[] parseGitHubUrl(String url) {
        // Check if it's a valid GitHub URL
        if (url != null && url.contains("github.com/")) {
            try {
                // Extract the part after github.com/
                String[] parts = url.split("github.com/");
                if (parts.length > 1) {
                    String repoPath = parts[1];
                    // Remove any trailing slashes or /main, /master, etc.
                    repoPath = repoPath.replaceAll("/$", "");
                    repoPath = repoPath.replaceAll("/(main|master|tree|blob).*", "");
                    
                    // Split user and repo
                    String[] userRepo = repoPath.split("/");
                    if (userRepo.length >= 2) {
                        String user = userRepo[0];
                        String repo = userRepo[1];
                        return new String[]{user, repo};
                    }
                }
            } catch (Exception e) {
                // If parsing fails, return null
            }
        }
        // If not a valid GitHub URL or parsing failed, return null
        return null;
    }

    private void setupRecyclerView() {
        adapter = new ZipFileAdapter(filteredZipFiles, this::onZipFileClicked, this::onZipFileDelete, downloadedZipFiles, softDeletedZipFiles, hardDeletedZipFiles);
        zipRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        zipRecyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchBoostersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterZipFiles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupAddRandomBoosterButton() {
        addRandomBoosterButton.setOnClickListener(v -> {
            // Find zip files that are not downloaded (not in downloadedZipFiles, not in softDeletedZipFiles, not in hardDeletedZipFiles)
            List<String> availableZipFiles = new ArrayList<>();
            for (String zipFile : zipFiles) {
                if (!downloadedZipFiles.contains(zipFile) && !softDeletedZipFiles.contains(zipFile) && !hardDeletedZipFiles.contains(zipFile)) {
                    availableZipFiles.add(zipFile);
                }
            }

            if (availableZipFiles.isEmpty()) {
                Toast.makeText(getContext(), "Aucun booster disponible à ajouter", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pick random zip file
            java.util.Random random = new java.util.Random();
            String randomZipFile = availableZipFiles.get(random.nextInt(availableZipFiles.size()));

            // Download and extract it
            onZipFileClicked(randomZipFile);
        });
    }

    private void filterZipFiles(String query) {
        filteredZipFiles.clear();
        if (query.isEmpty()) {
            filteredZipFiles.addAll(zipFiles);
        } else {
            String lowerQuery = query.toLowerCase();
            for (String zipFile : zipFiles) {
                if (zipFile.toLowerCase().contains(lowerQuery)) {
                    filteredZipFiles.add(zipFile);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchZipFiles() {
        new GitHubApiTask().fetchZipFiles(repository, new GitHubApiTask.ZipFilesCallback() {
            @Override
            public void onZipFilesFetched(List<String> files) {
                requireActivity().runOnUiThread(() -> {
                    zipFiles.clear();
                    zipFiles.addAll(files);
                    filterZipFiles(searchBoostersEditText.getText().toString());
                    // Load downloaded boosters after zip files are loaded
                    loadDownloadedBoosters();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void onZipFileClicked(String zipFileName) {
        new ZipDownloadTask(requireContext()).downloadAndExtractZip(repository, zipFileName, new ZipDownloadTask.DownloadCallback() {
            @Override
            public void onDownloadComplete(String extractedPath) {
                requireActivity().runOnUiThread(() -> {
                    // Mark as downloaded
                    downloadedZipFiles.add(zipFileName);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), R.string.zip_extracted_successfully, Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void onZipFileDelete(String zipFileName, int position) {
        // Check if images are already deleted (soft delete already done)
        boolean imagesAlreadyDeleted = softDeletedZipFiles.contains(zipFileName);
        
        // Show delete mode dialog
        DeleteModeDialog dialog = DeleteModeDialog.newInstance(zipFileName, imagesAlreadyDeleted);
        dialog.setListener(mode -> {
            if (mode == DeleteModeDialog.DeleteMode.HARD) {
                // Hard delete: mark as hard deleted and delete images
                hardDeletedZipFiles.add(zipFileName);
                downloadedZipFiles.remove(zipFileName);
                softDeletedZipFiles.remove(zipFileName);
                deleteBoosterImages(zipFileName);
                updateBoosterStatus(zipFileName, "hard_deleted");
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Hard delete: " + zipFileName, Toast.LENGTH_SHORT).show();
            } else {
                // Soft delete: remove from downloaded but add to soft deleted
                downloadedZipFiles.remove(zipFileName);
                softDeletedZipFiles.add(zipFileName);
                deleteBoosterImages(zipFileName);
                updateBoosterStatus(zipFileName, "soft_deleted");
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Soft delete: " + zipFileName, Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getChildFragmentManager(), "DeleteModeDialog");
    }

    private void loadDownloadedBoosters() {
        // Load boosters from database to determine which are downloaded
        new Thread(() -> {
            java.util.List<Booster> allBoosters = CardMasterApplication.getInstance().getDatabase().boosterDao().getAllBoostersSync();
            android.util.Log.d("ZipFilesFragment", "Found " + allBoosters.size() + " boosters in database");
            android.util.Log.d("ZipFilesFragment", "Available zip files: " + zipFiles);
            
            for (Booster booster : allBoosters) {
                String status = booster.getStatus();
                String boosterName = booster.getName();
                
                // Try to match booster name with zip file name (handle spaces vs underscores)
                String zipFileName = boosterName + ".zip";
                String zipFileNameWithUnderscore = boosterName.replace(" ", "_") + ".zip";
                String zipFileNameWithSpaces = boosterName.replace("_", " ") + ".zip";
                
                android.util.Log.d("ZipFilesFragment", "Booster: " + boosterName + ", status: " + status);
                android.util.Log.d("ZipFilesFragment", "Trying zip: " + zipFileName + " or " + zipFileNameWithUnderscore + " or " + zipFileNameWithSpaces);
                
                // Check if any version exists in zipFiles
                boolean found = false;
                String matchedZip = null;
                for (String zip : zipFiles) {
                    if (zip.equals(zipFileName) || zip.equals(zipFileNameWithUnderscore) || zip.equals(zipFileNameWithSpaces)) {
                        found = true;
                        matchedZip = zip;
                        android.util.Log.d("ZipFilesFragment", "Matched zip: " + zip);
                        
                        // Add to downloaded if active
                        if ("active".equals(status)) {
                            downloadedZipFiles.add(zip);
                            android.util.Log.d("ZipFilesFragment", "Added to downloaded: " + zip);
                        }
                        
                        // Add to soft deleted if soft deleted
                        if ("soft_deleted".equals(status)) {
                            softDeletedZipFiles.add(zip);
                            android.util.Log.d("ZipFilesFragment", "Added to soft deleted: " + zip);
                        }
                        
                        // Add to hard deleted if hard deleted
                        if ("hard_deleted".equals(status)) {
                            hardDeletedZipFiles.add(zip);
                            android.util.Log.d("ZipFilesFragment", "Added to hard deleted: " + zip);
                        }
                        break;
                    }
                }
                
                if (!found) {
                    android.util.Log.d("ZipFilesFragment", "No matching zip file found for booster: " + boosterName);
                    // Try to find a zip file by ID instead (fallback)
                    String foundZip = findZipByBoosterId(booster.getId());
                    if (foundZip != null) {
                        matchedZip = foundZip;
                        found = true;
                        android.util.Log.d("ZipFilesFragment", "Found zip by ID: " + foundZip);
                        
                        // Update booster name in database to match zip file name
                        String newBoosterName = foundZip.replace(".zip", "");
                        android.util.Log.d("ZipFilesFragment", "Updating booster name from " + boosterName + " to " + newBoosterName);
                        booster.setName(newBoosterName);
                        CardMasterApplication.getInstance().getBoosterRepository().insertBoosterSync(booster);
                        
                        if ("active".equals(status)) {
                            downloadedZipFiles.add(foundZip);
                        }
                        if ("soft_deleted".equals(status)) {
                            softDeletedZipFiles.add(foundZip);
                        }
                        if ("hard_deleted".equals(status)) {
                            hardDeletedZipFiles.add(foundZip);
                        }
                    }
                }
            }
            
            android.util.Log.d("ZipFilesFragment", "Downloaded zip files: " + downloadedZipFiles.size());
            android.util.Log.d("ZipFilesFragment", "Soft deleted zip files: " + softDeletedZipFiles.size());
            android.util.Log.d("ZipFilesFragment", "Hard deleted zip files: " + hardDeletedZipFiles.size());
            
            requireActivity().runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private String findZipByBoosterId(int boosterId) {
        // Try to match zip file by looking at the booster ID in the zip file name
        // This is a fallback when name matching fails
        for (String zip : zipFiles) {
            // Extract the ID from the zip file name if it follows a pattern
            // For now, just return null as we don't have a clear pattern
        }
        return null;
    }

    private void deleteBoosterImages(String zipFileName) {
        // Delete image files for this booster
        // Extract booster name from zip file name (e.g., "booster_001.zip" -> "booster_001")
        String boosterName = zipFileName.replace(".zip", "");
        String boosterPath = requireContext().getFilesDir() + "/booster_000/" + boosterName;
        
        java.io.File boosterDir = new java.io.File(boosterPath);
        if (boosterDir.exists()) {
            deleteDirectory(boosterDir);
        }
    }

    private void deleteDirectory(java.io.File directory) {
        if (directory.isDirectory()) {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    private void updateBoosterStatus(String zipFileName, String status) {
        // Find booster by name and update status
        new Thread(() -> {
            String boosterName = zipFileName.replace(".zip", "");
            // Get all boosters and find matching one by name
            java.util.List<Booster> allBoosters = CardMasterApplication.getInstance().getDatabase().boosterDao().getAllBoostersSync();
            for (Booster booster : allBoosters) {
                if (booster.getName().equals(boosterName)) {
                    CardMasterApplication.getInstance().getBoosterRepository().updateStatus(booster.getId(), status);
                    break;
                }
            }
        }).start();
    }
}
