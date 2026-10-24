package com.example.smartpantry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Model for one ingredient the user currently has at home. */
public class PantryItem {
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private final long id;
    private final String name;
    private final double quantity;
    private final String unit;
    private final String expiry; // optional, "" when not set

    public PantryItem(long id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry == null ? "" : expiry;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiry() { return expiry; }

    /** True if the expiry date is today, in the past, or within the next {@code days} days. */
    public boolean isExpiringSoon(int days) {
        if (expiry.isEmpty()) return false;
        try {
            Date d = new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(expiry);
            long diffDays = (d.getTime() - System.currentTimeMillis()) / (24L * 60 * 60 * 1000);
            return diffDays <= days;
        } catch (ParseException e) {
            return false;
        }
    }
}
