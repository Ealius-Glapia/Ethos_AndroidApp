package com.cardmaster.app.ui.ajout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.GitHubRepository;

import java.util.List;

public class RepositoryAdapter extends RecyclerView.Adapter<RepositoryAdapter.ViewHolder> {

    private List<GitHubRepository> repositories;
    private final OnRepositoryClickListener onRepositoryClickListener;
    private final OnDeleteClickListener onDeleteClickListener;

    public interface OnRepositoryClickListener {
        void onRepositoryClick(GitHubRepository repository);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(GitHubRepository repository, int position);
    }

    public RepositoryAdapter(List<GitHubRepository> repositories, 
                             OnRepositoryClickListener onRepositoryClickListener,
                             OnDeleteClickListener onDeleteClickListener) {
        this.repositories = repositories;
        this.onRepositoryClickListener = onRepositoryClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_repository, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GitHubRepository repository = repositories.get(position);
        String url = repository.getUrl();
        
        // Parse GitHub URL to extract user and repo
        String[] userRepo = parseGitHubUrl(url);
        
        if (userRepo != null) {
            holder.userTextView.setText(userRepo[0]);
            holder.repoTextView.setText(userRepo[1]);
        } else {
            // If not a valid GitHub URL, display the full URL in user text
            holder.userTextView.setText(url);
            holder.repoTextView.setText("");
        }
        
        holder.itemView.setOnClickListener(v -> onRepositoryClickListener.onRepositoryClick(repository));
        holder.deleteButton.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(repository, position));
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

    @Override
    public int getItemCount() {
        return repositories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userTextView;
        TextView repoTextView;
        ImageView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userTextView = itemView.findViewById(R.id.repository_user_text);
            repoTextView = itemView.findViewById(R.id.repository_repo_text);
            deleteButton = itemView.findViewById(R.id.delete_repository_button);
        }
    }
}
