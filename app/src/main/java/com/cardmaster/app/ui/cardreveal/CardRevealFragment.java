package com.cardmaster.app.ui.cardreveal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cardmaster.app.R;
import com.cardmaster.app.data.entity.Card;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardRevealFragment extends Fragment {
    private static final String ARG_CARDS = "cards";
    private static final String ARG_TOKEN_GAINS = "token_gains";

    private RecyclerView cardsRecyclerView;
    private CardRevealAdapter adapter;
    private List<Card> cards;
    private Map<Integer, Integer> tokenGains; // cardId -> token gain

    public interface CardRevealListener {
        void onContinue(List<Card> cards);
    }

    public static CardRevealFragment newInstance(List<Card> cards, Map<Integer, Integer> tokenGains) {
        CardRevealFragment fragment = new CardRevealFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CARDS, new java.util.ArrayList<>(cards));
        args.putSerializable(ARG_TOKEN_GAINS, new java.util.HashMap<>(tokenGains));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_reveal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            cards = (List<Card>) getArguments().getSerializable(ARG_CARDS);
            tokenGains = (Map<Integer, Integer>) getArguments().getSerializable(ARG_TOKEN_GAINS);
        }

        cardsRecyclerView = view.findViewById(R.id.cards_recycler_view);
        TextView titleTextView = view.findViewById(R.id.title_text);
        Button continueButton = view.findViewById(R.id.continue_button);

        titleTextView.setText(getString(R.string.card_reveal_title));

        setupRecyclerView();

        continueButton.setOnClickListener(v -> {
            if (getActivity() instanceof CardRevealListener) {
                ((CardRevealListener) getActivity()).onContinue(cards);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CardRevealAdapter(cards, tokenGains, this);
        cardsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        cardsRecyclerView.setAdapter(adapter);
    }
}
