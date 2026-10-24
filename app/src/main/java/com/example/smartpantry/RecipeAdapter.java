package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** RecyclerView adapter for recipe lists (strict suggestions and the "Almost There" list). */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.VH> {

    public interface Listener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<RecipeMatcher.Result> results;
    private final boolean showMissing;
    private final Listener listener;

    public RecipeAdapter(List<RecipeMatcher.Result> results, boolean showMissing, Listener listener) {
        this.results = results;
        this.showMissing = showMissing;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        RecipeMatcher.Result r = results.get(position);
        h.title.setText(r.recipe.getName());
        if (showMissing) {
            h.subtitle.setText("Missing: " + android.text.TextUtils.join(", ", r.missing));
        } else {
            h.subtitle.setText(r.recipe.getIngredients().size() + " ingredients - you have them all");
        }
        h.itemView.setOnClickListener(v -> listener.onRecipeClick(r.recipe));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, subtitle;

        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.recipeName);
            subtitle = v.findViewById(R.id.recipeSubtitle);
        }
    }
}
