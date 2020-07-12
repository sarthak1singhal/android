package com.elysion.elysion;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {
    private List<String> languageList;
    private OnLanguageSelectionListener onLanguageSelectionListener;
    private int selectedPosition = -1;

    public LanguageAdapter(List<String> languageList, OnLanguageSelectionListener onLanguageSelectionListener) {
        this.languageList = languageList;
        this.onLanguageSelectionListener = onLanguageSelectionListener;
    }

    @Override
    public LanguageViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
        // create a new view
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_language_select, parent, false);

        LanguageViewHolder vh = new LanguageViewHolder(v);
        return vh;
    }

    public void setSelection(String selectedLanguage) {
        for (int i = 0; i < languageList.size(); i++) {
            if (selectedLanguage.equalsIgnoreCase(languageList.get(i))) {
                selectedPosition = i;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(LanguageViewHolder holder, int position) {
        holder.textView.setText(languageList.get(position));
        if (selectedPosition == position) {
            holder.textView.setBackgroundColor(Color.CYAN);
        } else {
            holder.textView.setBackgroundColor(Color.WHITE);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return languageList.size();
    }

    interface OnLanguageSelectionListener {
        void onLanguageSelected(String language, int position);
    }

    public class LanguageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;

        public LanguageViewHolder(TextView v) {
            super(v);
            textView = v;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            selectedPosition = position;
            onLanguageSelectionListener.onLanguageSelected(languageList.get(position), position);
            notifyDataSetChanged();

        }
    }
}