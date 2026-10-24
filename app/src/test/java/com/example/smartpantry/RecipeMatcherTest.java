package com.example.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Unit tests for the strict-matching engine (RecipeMatcher).
 */
public class RecipeMatcherTest {

    @Test
    public void testStrictMatching_AllIngredientsPresent_IncludesRecipe() {
        Recipe recipe = new Recipe(1, "Scrambled Eggs", "Cook eggs", List.of(
                new RecipeIngredient("egg", 2, "pcs"),
                new RecipeIngredient("butter", 10, "g")
        ));

        List<PantryItem> pantry = List.of(
                new PantryItem(1, "egg", 3, "pcs", ""),
                new PantryItem(2, "butter", 20, "g", "")
        );

        List<RecipeMatcher.Result> results = RecipeMatcher.evaluate(List.of(recipe), pantry);

        assertEquals(1, results.size());
        assertTrue(results.get(0).isStrictMatch());
        assertTrue(results.get(0).missing.isEmpty());
    }

    @Test
    public void testStrictMatching_MissingOneIngredient_ExcludesRecipe() {
        Recipe recipe = new Recipe(1, "Tomato Pasta", "Boil pasta", List.of(
                new RecipeIngredient("pasta", 200, "g"),
                new RecipeIngredient("tomato", 3, "pcs"),
                new RecipeIngredient("garlic", 2, "pcs")
        ));

        List<PantryItem> pantry = List.of(
                new PantryItem(1, "pasta", 500, "g", ""),
                new PantryItem(2, "tomato", 5, "pcs", "")
                // missing garlic
        );

        List<RecipeMatcher.Result> results = RecipeMatcher.evaluate(List.of(recipe), pantry);

        assertEquals(1, results.size());
        assertFalse(results.get(0).isStrictMatch());
        assertEquals(1, results.get(0).missing.size());
        assertEquals("garlic", results.get(0).missing.get(0));
    }

    @Test
    public void testStrictMatching_InsufficientQuantity_ExcludesRecipe() {
        Recipe recipe = new Recipe(1, "Pancakes", "Make batter", List.of(
                new RecipeIngredient("flour", 200, "g"),
                new RecipeIngredient("milk", 300, "ml"),
                new RecipeIngredient("egg", 2, "pcs")
        ));

        List<PantryItem> pantry = List.of(
                new PantryItem(1, "flour", 200, "g", ""),
                new PantryItem(2, "milk", 300, "ml", ""),
                new PantryItem(3, "egg", 1, "pcs", "") // Needs 2, only has 1
        );

        List<RecipeMatcher.Result> results = RecipeMatcher.evaluate(List.of(recipe), pantry);

        assertEquals(1, results.size());
        assertFalse(results.get(0).isStrictMatch());
        assertEquals("egg", results.get(0).missing.get(0));
    }

    @Test
    public void testNormalization_PluralsAndCaseInsensitivity() {
        Recipe recipe = new Recipe(1, "Omelette", "Cook", List.of(
                new RecipeIngredient("tomato", 2, "pcs"),
                new RecipeIngredient("egg", 3, "pcs")
        ));

        List<PantryItem> pantry = List.of(
                new PantryItem(1, "  Tomatoes ", 5, "pcs", ""),
                new PantryItem(2, "EGGS", 4, "pcs", "")
        );

        List<RecipeMatcher.Result> results = RecipeMatcher.evaluate(List.of(recipe), pantry);

        assertEquals(1, results.size());
        assertTrue(results.get(0).isStrictMatch());
    }

    @Test
    public void testUnitConversion_KilogramsToGramsAndLitresToMillilitres() {
        Recipe recipe = new Recipe(1, "Pancakes", "Cook", List.of(
                new RecipeIngredient("flour", 200, "g"),
                new RecipeIngredient("milk", 250, "ml")
        ));

        List<PantryItem> pantry = List.of(
                new PantryItem(1, "flour", 1.0, "kg", ""), // 1000g >= 200g
                new PantryItem(2, "milk", 0.5, "l", "")    // 500ml >= 250ml
        );

        List<RecipeMatcher.Result> results = RecipeMatcher.evaluate(List.of(recipe), pantry);

        assertEquals(1, results.size());
        assertTrue(results.get(0).isStrictMatch());
    }
}
