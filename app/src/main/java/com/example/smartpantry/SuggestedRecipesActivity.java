package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Suggested Recipes screen: runs the strict-matching rule and shows only full matches. */
public class SuggestedRecipesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        setTitle("Suggested Recipes");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DatabaseHelper db = new DatabaseHelper(this);
        List<RecipeMatcher.Result> all =
                RecipeMatcher.evaluate(db.getAllRecipes(), db.getAllItems());

        List<RecipeMatcher.Result> strict = new ArrayList<>();
        List<RecipeMatcher.Result> almost = new ArrayList<>();
        for (RecipeMatcher.Result r : all) {
            if (r.isStrictMatch()) strict.add(r);          // every ingredient present
            else if (r.missing.size() == 1) almost.add(r); // bonus list, kept separate
        }

        RecipeAdapter.Listener open = recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(i);
        };

        RecyclerView strictList = findViewById(R.id.strictRecycler);
        strictList.setLayoutManager(new LinearLayoutManager(this));
        strictList.setAdapter(new RecipeAdapter(strict, false, open));

        // Feedback when nothing matches instead of a blank screen
        TextView emptyView = findViewById(R.id.emptyStrict);
        emptyView.setVisibility(strict.isEmpty() ? View.VISIBLE : View.GONE);
        strictList.setVisibility(strict.isEmpty() ? View.GONE : View.VISIBLE);

        boolean showAlmost = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE)
                .getBoolean(SettingsActivity.KEY_ALMOST, false);
        View header = findViewById(R.id.almostHeader);
        RecyclerView almostList = findViewById(R.id.almostRecycler);
        if (showAlmost) {
            almostList.setLayoutManager(new LinearLayoutManager(this));
            almostList.setAdapter(new RecipeAdapter(almost, true, open));
            header.setVisibility(View.VISIBLE);
            almostList.setVisibility(View.VISIBLE);
        } else {
            header.setVisibility(View.GONE);
            almostList.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
