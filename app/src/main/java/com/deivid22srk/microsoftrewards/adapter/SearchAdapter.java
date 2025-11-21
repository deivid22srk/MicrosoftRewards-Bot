package com.deivid22srk.microsoftrewards.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.deivid22srk.microsoftrewards.R;
import com.deivid22srk.microsoftrewards.model.SearchItem;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    
    private List<SearchItem> searchItems;

    public SearchAdapter(List<SearchItem> searchItems) {
        this.searchItems = searchItems;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchItem item = searchItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return searchItems.size();
    }

    public void updateItem(int position, SearchItem item) {
        if (position >= 0 && position < searchItems.size()) {
            searchItems.set(position, item);
            notifyItemChanged(position);
        }
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        
        private TextView searchText;
        private TextView statusText;
        private TextView indexText;
        private ImageView statusIcon;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            searchText = itemView.findViewById(R.id.searchText);
            statusText = itemView.findViewById(R.id.statusText);
            indexText = itemView.findViewById(R.id.indexText);
            statusIcon = itemView.findViewById(R.id.statusIcon);
        }

        public void bind(SearchItem item) {
            searchText.setText(item.getSearchText());
            statusText.setText(item.getStatus().getDisplayName());
            indexText.setText(String.valueOf(item.getIndex()));
            
            // Atualizar ícone e cores baseado no status
            updateStatusAppearance(item.getStatus());
        }
        
        private void updateStatusAppearance(SearchItem.SearchStatus status) {
            int iconRes;
            int colorRes;
            
            switch (status) {
                case PENDING:
                    iconRes = R.drawable.ic_pending;
                    colorRes = R.color.microsoft_blue;
                    break;
                case IN_PROGRESS:
                    iconRes = R.drawable.ic_progress;
                    colorRes = R.color.microsoft_orange;
                    break;
                case COMPLETED:
                    iconRes = R.drawable.ic_check_circle;
                    colorRes = R.color.microsoft_green;
                    break;
                case FAILED:
                    iconRes = R.drawable.ic_error;
                    colorRes = R.color.microsoft_red;
                    break;
                default:
                    iconRes = R.drawable.ic_pending;
                    colorRes = R.color.microsoft_blue;
                    break;
            }
            
            statusIcon.setImageResource(iconRes);
            statusIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), colorRes));
            statusText.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
        }
    }
}