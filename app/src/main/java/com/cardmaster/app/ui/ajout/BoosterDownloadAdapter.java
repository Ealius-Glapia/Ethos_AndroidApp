package com.cardmaster.app.ui.ajout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Booster;

import java.util.List;

public class BoosterDownloadAdapter extends RecyclerView.Adapter<BoosterDownloadAdapter.ViewHolder> {

    private List<Booster> boosters;
    private final OnBoosterClickListener onBoosterClickListener;
    private final OnDeleteClickListener onDeleteClickListener;
    private final OnAddClickListener onAddClickListener;

    public interface OnBoosterClickListener {
        void onBoosterClick(Booster booster);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Booster booster, int position);
    }

    public interface OnAddClickListener {
        void onAddClick(Booster booster, int position);
    }

    public BoosterDownloadAdapter(List<Booster> boosters, 
                                   OnBoosterClickListener onBoosterClickListener,
                                   OnDeleteClickListener onDeleteClickListener,
                                   OnAddClickListener onAddClickListener) {
        this.boosters = boosters;
        this.onBoosterClickListener = onBoosterClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onAddClickListener = onAddClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booster_download, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booster booster = boosters.get(position);
        
        holder.nameTextView.setText(booster.getName());
        
        // Booster is active, show delete button
        holder.statusTextView.setText("Téléchargé");
        holder.deleteButton.setVisibility(View.VISIBLE);
        holder.addButton.setVisibility(View.GONE);
        
        holder.itemView.setOnClickListener(v -> onBoosterClickListener.onBoosterClick(booster));
        holder.deleteButton.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(booster, position));
        holder.addButton.setOnClickListener(v -> onAddClickListener.onAddClick(booster, position));
    }

    @Override
    public int getItemCount() {
        return boosters.size();
    }

    public void setBoosterHardDeleted(int position, boolean isHardDeleted) {
        if (position >= 0 && position < boosters.size()) {
            notifyItemChanged(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView statusTextView;
        ImageView deleteButton;
        Button addButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.booster_name_text);
            statusTextView = itemView.findViewById(R.id.booster_status_text);
            deleteButton = itemView.findViewById(R.id.delete_booster_button);
            addButton = itemView.findViewById(R.id.add_booster_button);
        }
    }
}
