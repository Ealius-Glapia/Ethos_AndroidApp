package com.cardmaster.app.ui.ajout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.R;

import java.util.List;
import java.util.Set;

public class ZipFileAdapter extends RecyclerView.Adapter<ZipFileAdapter.ViewHolder> {

    private List<String> zipFiles;
    private final OnZipFileClickListener listener;
    private final OnZipFileDeleteListener deleteListener;
    private final Set<String> downloadedZipFiles;
    private final Set<String> softDeletedZipFiles;
    private final Set<String> hardDeletedZipFiles;

    public interface OnZipFileClickListener {
        void onZipFileClick(String zipFileName);
    }

    public interface OnZipFileDeleteListener {
        void onZipFileDelete(String zipFileName, int position);
    }

    public ZipFileAdapter(List<String> zipFiles, OnZipFileClickListener listener, OnZipFileDeleteListener deleteListener, 
                         Set<String> downloadedZipFiles, Set<String> softDeletedZipFiles, Set<String> hardDeletedZipFiles) {
        this.zipFiles = zipFiles;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.downloadedZipFiles = downloadedZipFiles;
        this.softDeletedZipFiles = softDeletedZipFiles;
        this.hardDeletedZipFiles = hardDeletedZipFiles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_zip_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String zipFile = zipFiles.get(position);
        // Remove .zip extension and replace underscores with spaces
        String displayName = zipFile.replace(".zip", "").replace("_", " ");
        holder.textView.setText(displayName);
        holder.itemView.setOnClickListener(v -> listener.onZipFileClick(zipFile));
        
        // Show delete button only if downloaded (active or soft deleted) and not hard deleted
        boolean isDownloaded = downloadedZipFiles.contains(zipFile) || softDeletedZipFiles.contains(zipFile);
        boolean isHardDeleted = hardDeletedZipFiles.contains(zipFile);
        
        // Only show delete button if booster is downloaded (active or soft deleted) and not hard deleted
        if (isDownloaded && !isHardDeleted) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> deleteListener.onZipFileDelete(zipFile, position));
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return zipFiles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.zip_file_name);
            deleteButton = itemView.findViewById(R.id.delete_zip_button);
        }
    }
}
