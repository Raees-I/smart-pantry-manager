package com.example.smartpantry;

/** One ingredient line inside a recipe (e.g. 2 tbsp olive oil). */
public class RecipeIngredient {
    private final String name;
    private final double quantity;
    private final String unit;

    public RecipeIngredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    /** Formats 2.0 as "2" and 0.5 as "0.5". */
    public static String fmt(double q) {
        return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q);
    }

    @Override
    public String toString() {
        return fmt(quantity) + " " + unit + " " + name;
    }
}
