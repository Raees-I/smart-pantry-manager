package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Local SQLite database. Tables:
 *  - pantry              : the user's ingredients (full CRUD)
 *  - recipes             : seeded on first run
 *  - recipe_ingredients  : ingredients required by each recipe (FK -> recipes)
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, ingredient TEXT NOT NULL, quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");
        seedRecipes(db); // runs only the first time the database is created
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    // ---------------------------------------------------------------- PANTRY CRUD

    private ContentValues toValues(PantryItem it) {
        ContentValues v = new ContentValues();
        v.put("name", it.getName());
        v.put("quantity", it.getQuantity());
        v.put("unit", it.getUnit());
        v.put("expiry", it.getExpiry());
        return v;
    }

    /** CREATE */
    public long insertItem(PantryItem it) {
        return getWritableDatabase().insert("pantry", null, toValues(it));
    }

    /** UPDATE */
    public int updateItem(PantryItem it) {
        return getWritableDatabase().update("pantry", toValues(it), "id=?",
                new String[]{String.valueOf(it.getId())});
    }

    /** DELETE */
    public int deleteItem(long id) {
        return getWritableDatabase().delete("pantry", "id=?", new String[]{String.valueOf(id)});
    }

    /** READ (all) */
    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,quantity,unit,expiry FROM pantry ORDER BY name COLLATE NOCASE", null)) {
            while (c.moveToNext()) {
                list.add(new PantryItem(c.getLong(0), c.getString(1), c.getDouble(2),
                        c.getString(3), c.getString(4)));
            }
        }
        return list;
    }

    /** READ (one) */
    public PantryItem getItem(long id) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,quantity,unit,expiry FROM pantry WHERE id=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) {
                return new PantryItem(c.getLong(0), c.getString(1), c.getDouble(2),
                        c.getString(3), c.getString(4));
            }
        }
        return null;
    }

    // -------------------------------------------------------------- RECIPES (read)

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id,name,steps FROM recipes ORDER BY name", null)) {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                list.add(new Recipe(id, c.getString(1), c.getString(2), getIngredients(db, id)));
            }
        }
        return list;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT id,name,steps FROM recipes WHERE id=?",
                new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) {
                return new Recipe(id, c.getString(1), c.getString(2), getIngredients(db, id));
            }
        }
        return null;
    }

    private List<RecipeIngredient> getIngredients(SQLiteDatabase db, long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        try (Cursor c = db.rawQuery("SELECT ingredient,quantity,unit FROM recipe_ingredients "
                + "WHERE recipe_id=?", new String[]{String.valueOf(recipeId)})) {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(c.getString(0), c.getDouble(1), c.getString(2)));
            }
        }
        return list;
    }

    // --------------------------------------------------------------------- SEEDING

    /** Ingredient spec format: "name|quantity|unit". */
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert("recipes", null, rv);
        for (String spec : ingredients) {
            String[] p = spec.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("ingredient", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert("recipe_ingredients", null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs",
                "1. Crack the eggs into a bowl and whisk with the salt.\n2. Melt butter in a pan on low heat.\n3. Pour in eggs and stir gently until just set.",
                "egg|3|pcs", "butter|10|g", "salt|1|tsp");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until al dente.\n2. Fry garlic in olive oil, add chopped tomatoes and simmer 10 min.\n3. Toss pasta through the sauce.",
                "pasta|200|g", "tomato|3|pcs", "garlic|2|pcs", "olive oil|2|tbsp");
        addRecipe(db, "Cheese Omelette",
                "1. Whisk eggs.\n2. Melt butter in a pan and pour in eggs.\n3. Add grated cheese, fold and serve.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g");
        addRecipe(db, "Pancakes",
                "1. Mix flour, sugar, egg and milk into a smooth batter.\n2. Pour small rounds into a hot pan.\n3. Flip when bubbles form and cook until golden.",
                "flour|200|g", "milk|300|ml", "egg|2|pcs", "sugar|2|tbsp");
        addRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry chopped onion, push aside and scramble the eggs.\n3. Add rice and soy sauce and stir-fry for 5 min.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp");
        addRecipe(db, "Cheese Toastie",
                "1. Butter the bread slices.\n2. Place cheese between the slices.\n3. Toast in a pan until golden on both sides.",
                "bread|4|pcs", "cheese|80|g", "butter|10|g");
        addRecipe(db, "French Toast",
                "1. Whisk egg, milk and sugar.\n2. Soak bread slices in the mixture.\n3. Fry until golden on both sides.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp");
        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash.\n3. Stir in butter and warm milk.",
                "potato|500|g", "butter|30|g", "milk|100|ml");
        addRecipe(db, "Tuna Pasta",
                "1. Boil the pasta.\n2. Drain the tuna and warm it in olive oil.\n3. Toss with the pasta.",
                "pasta|200|g", "tuna|1|pcs", "olive oil|1|tbsp");
        addRecipe(db, "Banana Oats",
                "1. Heat the oats with the milk, stirring for 5 min.\n2. Slice the banana on top and serve.",
                "oats|80|g", "milk|250|ml", "banana|1|pcs");
        addRecipe(db, "Garlic Bread",
                "1. Mash crushed garlic into the butter.\n2. Spread over the bread slices.\n3. Bake at 200C for 8 minutes.",
                "bread|4|pcs", "butter|30|g", "garlic|2|pcs");
        addRecipe(db, "Tomato Soup",
                "1. Fry onion and garlic until soft.\n2. Add chopped tomatoes and stock, simmer 20 min.\n3. Blend until smooth.",
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "vegetable stock|500|ml");
        addRecipe(db, "Cheesy Baked Potato",
                "1. Bake the potatoes at 200C for 60 min.\n2. Split open and add butter.\n3. Top with grated cheese and return to the oven for 5 min.",
                "potato|2|pcs", "cheese|60|g", "butter|15|g");
        addRecipe(db, "Egg Noodles",
                "1. Boil the noodles and drain.\n2. Scramble the eggs in a hot pan.\n3. Add noodles and soy sauce and toss.",
                "noodles|200|g", "egg|2|pcs", "soy sauce|2|tbsp");
        addRecipe(db, "Peanut Butter Sandwich",
                "1. Spread peanut butter on a slice of bread.\n2. Top with the second slice and cut in half.",
                "bread|2|pcs", "peanut butter|2|tbsp");
        addRecipe(db, "Lentil Curry",
                "1. Fry onion, add curry powder.\n2. Add lentils and chopped tomatoes with water.\n3. Simmer 25 min until soft.",
                "lentils|200|g", "onion|1|pcs", "tomato|2|pcs", "curry powder|2|tsp");
        addRecipe(db, "Avocado Toast",
                "1. Toast the bread slices.\n2. Mash the avocado with lemon juice and salt.\n3. Spread over toast and serve.",
                "bread|2|pcs", "avocado|1|pcs", "lemon|1|pcs", "salt|1|tsp");
        addRecipe(db, "Fruit Salad",
                "1. Chop banana, apple and orange into bite-sized pieces.\n2. Toss together in a bowl and serve fresh.",
                "banana|2|pcs", "apple|2|pcs", "orange|2|pcs");
        addRecipe(db, "Garlic Butter Rice",
                "1. Melt butter in a pot and sauté minced garlic.\n2. Add rice and water, bring to boil and simmer 15 min.\n3. Fluff with a fork.",
                "rice|200|g", "butter|20|g", "garlic|3|pcs");
        addRecipe(db, "Omelette Roll",
                "1. Whisk eggs with salt.\n2. Pour a thin layer into a pan, add grated cheese and roll up as it cooks.\n3. Slice into pieces.",
                "egg|4|pcs", "cheese|60|g", "salt|1|tsp");
    }
}
