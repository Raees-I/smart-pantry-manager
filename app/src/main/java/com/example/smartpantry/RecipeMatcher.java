package com.example.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CORE BUSINESS LOGIC - the strict-matching rule.
 *
 * A recipe is a strict match ONLY IF every one of its ingredients exists in the pantry
 * in at least the required quantity. Robustness features:
 *  - names are normalised (case, whitespace, simple plural -> singular: tomatoes == tomato)
 *  - units are converted to a base unit within the same dimension (kg->g, l->ml, tbsp->ml ...)
 *  - duplicate pantry entries of the same ingredient are added together
 *  - units from different dimensions (e.g. grams vs pieces) are NOT guessed -> treated as missing
 */
public final class RecipeMatcher {
    private RecipeMatcher() { }

    /** Outcome of testing one recipe against the pantry. */
    public static class Result {
        public final Recipe recipe;
        public final List<String> missing; // names of ingredients the pantry lacks

        Result(Recipe recipe, List<String> missing) {
            this.recipe = recipe;
            this.missing = missing;
        }

        public boolean isStrictMatch() { return missing.isEmpty(); }
    }

    /** Tests every recipe against the pantry. */
    public static List<Result> evaluate(List<Recipe> recipes, List<PantryItem> pantry) {
        // 1. Build stock: "normalisedName|dimension" -> total quantity in base units
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem p : pantry) {
            String key = key(p.getName(), p.getUnit());
            double qty = toBase(p.getQuantity(), p.getUnit());
            Double existing = stock.get(key);
            stock.put(key, (existing == null ? 0 : existing) + qty);
        }

        // 2. Check each recipe ingredient against the stock
        List<Result> results = new ArrayList<>();
        for (Recipe r : recipes) {
            List<String> missing = new ArrayList<>();
            for (RecipeIngredient ing : r.getIngredients()) {
                Double have = stock.get(key(ing.getName(), ing.getUnit()));
                double need = toBase(ing.getQuantity(), ing.getUnit());
                if (have == null || have + 1e-9 < need) {
                    missing.add(ing.getName());
                }
            }
            results.add(new Result(r, missing));
        }
        return results;
    }

    // ------------------------------------------------------------ normalisation

    static String key(String name, String unit) {
        return normalizeName(name) + "|" + dimension(unit);
    }

    /** "  Tomatoes " -> "tomato", "Olive  Oil" -> "olive oil". Only the last word is singularised. */
    static String normalizeName(String raw) {
        String s = raw.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
        int idx = s.lastIndexOf(' ');
        String head = idx < 0 ? "" : s.substring(0, idx + 1);
        String last = idx < 0 ? s : s.substring(idx + 1);
        return head + singular(last);
    }

    private static String singular(String w) {
        int n = w.length();
        if (n > 3 && w.endsWith("ies")) return w.substring(0, n - 3) + "y"; // berries -> berry
        if (n > 3 && w.endsWith("oes")) return w.substring(0, n - 2);       // tomatoes -> tomato
        if (n > 3 && w.endsWith("ss")) return w;                            // grass stays
        if (n > 3 && w.endsWith("s")) return w.substring(0, n - 1);         // eggs -> egg
        return w;
    }

    static String normalizeUnit(String raw) {
        String u = raw.toLowerCase(Locale.ROOT).trim();
        switch (u) {
            case "gram": case "grams": return "g";
            case "kilogram": case "kilograms": case "kgs": return "kg";
            case "litre": case "litres": case "liter": case "liters": return "l";
            case "teaspoon": case "teaspoons": return "tsp";
            case "tablespoon": case "tablespoons": return "tbsp";
            case "cups": return "cup";
            case "piece": case "pieces": case "pc": case "pcs": case "unit": case "units": case "whole":
                return "pcs";
            default: return u;
        }
    }

    static String dimension(String unit) {
        switch (normalizeUnit(unit)) {
            case "g": case "kg": return "mass";
            case "ml": case "l": case "tsp": case "tbsp": case "cup": return "volume";
            default: return "count";
        }
    }

    /** Converts to g (mass), ml (volume) or pieces (count). */
    static double toBase(double qty, String unit) {
        switch (normalizeUnit(unit)) {
            case "kg": case "l": return qty * 1000;
            case "tsp": return qty * 5;
            case "tbsp": return qty * 15;
            case "cup": return qty * 240;
            default: return qty; // g, ml, pcs
        }
    }
}
