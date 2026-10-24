package com.example.smartpantry;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Custom RecyclerView adapter that binds pantry rows from the database to item_pantry.xml. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private final Listener listener;
    private List<PantryItem> items = new ArrayList<>();
    private boolean highlightExpiring;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> items, boolean highlightExpiring) {
        this.items = items;
        this.highlightExpiring = highlightExpiring;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PantryItem item = items.get(position);
        h.name.setText(item.getName());
        h.detail.setText(RecipeIngredient.fmt(item.getQuantity()) + " " + item.getUnit());

        if (item.getExpiry().isEmpty()) {
            h.expiry.setVisibility(View.GONE);
        } else {
            h.expiry.setVisibility(View.VISIBLE);
            boolean soon = highlightExpiring && item.isExpiringSoon(3);
            h.expiry.setText("Expires: " + item.getExpiry() + (soon ? "  (expiring soon!)" : ""));
            // always set the colour explicitly because views are recycled
            if (soon) h.expiry.setTextColor(Color.RED);
            else h.expiry.setTextColor(h.defaultExpiryColors);
        }

        h.itemView.setOnClickListener(v -> listener.onEdit(item));
        h.delete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, detail, expiry;
        final ImageButton delete;
        final ColorStateList defaultExpiryColors;

        VH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.itemName);
            detail = v.findViewById(R.id.itemDetail);
            expiry = v.findViewById(R.id.itemExpiry);
            delete = v.findViewById(R.id.deleteButton);
            defaultExpiryColors = expiry.getTextColors();
        }
    }
}
