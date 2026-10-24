# Smart Pantry Manager

A native Java Android application that tracks leftover ingredients in a user's home pantry and suggests recipes they can cook **strictly** using those available ingredients—helping reduce food waste without requiring an extra shopping trip.

---

## 📌 Project Overview & Concept

The **Smart Pantry Manager** is designed to eliminate food waste by answering the daily question: *"What can I cook with what I already have in my pantry?"*

The central rule of the app is its **Strict-Matching Rule**:
- A recipe is **only** suggested if every single required ingredient is present in the user's pantry in equal or greater quantity.
- Partial matches or recipes requiring missing ingredients are excluded from the main suggestion list to ensure 100% feasibility.
- The matching engine incorporates intelligent normalisation (handling case insensitivity, singular/plural variations like "tomatoes" vs. "tomato", and automatic unit conversions between mass/volume dimensions such as kg → g, l → ml, tbsp → ml).

---

## 🛠️ Tech Stack & Architecture

- **Language:** 100% Java (Android SDK)
- **Minimum SDK:** API Level 24 (Android 7.0) / Target SDK: API Level 34 (Android 14)
- **UI Architecture:** Activity-based architecture, ConstraintLayout, RelativeLayout, Material Design 3, RecyclerView with custom Adapters
- **Database Engine:** SQLite local persistence via `SQLiteOpenHelper`
- **Unit Testing:** JUnit 4 for core matching logic (`RecipeMatcherTest`)

---

## 💾 Database Justification (Why SQLite?)

We chose **SQLite** (via Android's native `SQLiteOpenHelper`) for the following reasons:
1. **Offline & Privacy-First:** Pantry inventory management is a personal, high-frequency task that should work instantly offline without needing a network connection or cloud subscription.
2. **ACID Compliance & Zero Latency:** SQLite provides atomic operations, zero network latency, and reliable data persistence directly on device memory.
3. **Relational Data Integrity:** The relational model natively handles foreign key constraints (`ON DELETE CASCADE`) between `recipes` and `recipe_ingredients`, ensuring data consistency.
4. **Lightweight Footprint:** Zero external heavy library dependencies required, resulting in a fast, lightweight application bundle.

---

## 📱 Features & Core Functionality

1. **Pantry Management (Full CRUD):**
   - **Create:** Add new ingredients with name, quantity, unit, and optional expiry date.
   - **Read:** Display ingredients in a clean `RecyclerView` sorted alphabetically.
   - **Update:** Tap any item to edit name, quantity, unit, or expiry date.
   - **Delete:** Remove items with a confirmation dialog.
2. **Input Validation:**
   - Strict validation preventing empty names, invalid characters/symbols, or negative/zero quantities.
3. **Strict Recipe Suggestion Engine:**
   - Evaluates all seeded database recipes against current pantry stock in real-time.
   - Displays clear user feedback ("No recipes match your pantry yet") when 0 recipes match.
4. **Recipe Detail View:**
   - Displays step-by-step cooking instructions and full ingredient measurements.
5. **Settings & Customization:**
   - Toggle optional expiring-soon highlights (red alert text for items expiring within 3 days).
   - Toggle optional "Almost There" list showing recipes missing exactly 1 ingredient (kept strictly separate from guaranteed recipes).

---

## 🚀 How to Build and Run

1. **Clone the Repository:**
   ```bash
   git clone <YOUR_GITHUB_REPOSITORY_URL>
   ```
2. **Open in Android Studio:**
   - Open Android Studio (Ladybug / Jellyfish or newer).
   - Select **Open an existing project** and navigate to the cloned project folder.
3. **Gradle Sync & Build:**
   - Allow Gradle to sync dependencies automatically.
   - Run the unit tests: `./gradlew test` (or via Android Studio test runner).
4. **Deploy & Launch:**
   - Connect an Android device or start an Android Emulator (API 24+).
   - Click **Run 'app'** (or `Shift + F10`).
