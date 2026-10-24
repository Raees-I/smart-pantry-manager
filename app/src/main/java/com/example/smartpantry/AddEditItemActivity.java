package com.example.smartpantry;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

/** Add / Edit Ingredient screen with input validation. */
public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private DatabaseHelper db;
    private EditText nameInput, quantityInput, expiryInput;
    private Spinner unitSpinner;
    private long itemId = -1; // -1 means "adding a new item"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DatabaseHelper(this);
        nameInput = findViewById(R.id.nameInput);
        quantityInput = findViewById(R.id.quantityInput);
        expiryInput = findViewById(R.id.expiryInput);
        unitSpinner = findViewById(R.id.unitSpinner);

        unitSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNITS));
        expiryInput.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.clearDateButton).setOnClickListener(v -> expiryInput.setText(""));
        findViewById(R.id.saveButton).setOnClickListener(v -> save());

        // Read the Intent extra to decide between Add and Edit mode
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            setTitle("Edit Ingredient");
            PantryItem it = db.getItem(itemId);
            if (it != null) {
                nameInput.setText(it.getName());
                quantityInput.setText(RecipeIngredient.fmt(it.getQuantity()));
                expiryInput.setText(it.getExpiry());
                for (int i = 0; i < UNITS.length; i++) {
                    if (UNITS[i].equals(it.getUnit())) unitSpinner.setSelection(i);
                }
            }
        } else {
            setTitle("Add Ingredient");
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) ->
                expiryInput.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates input, then inserts (new) or updates (existing). */
    private void save() {
        String name = nameInput.getText().toString().trim();
        String qtyText = quantityInput.getText().toString().trim();
        boolean valid = true;

        if (name.isEmpty()) {
            nameInput.setError("Enter an ingredient name");
            valid = false;
        } else if (!name.matches("[\\p{L}][\\p{L} \\-']*")) {
            nameInput.setError("Use letters only (no numbers or symbols)");
            valid = false;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            qty = -1;
        }
        if (qty <= 0) {
            quantityInput.setError("Enter a quantity greater than 0");
            valid = false;
        }
        if (!valid) return;

        String unit = unitSpinner.getSelectedItem().toString();
        String expiry = expiryInput.getText().toString().trim();

        if (itemId == -1) {
            db.insertItem(new PantryItem(0, name, qty, unit, expiry));
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateItem(new PantryItem(itemId, name, qty, unit, expiry));
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
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
