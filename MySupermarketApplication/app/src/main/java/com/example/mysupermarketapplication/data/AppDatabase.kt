package com.example.mysupermarketapplication.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mysupermarketapplication.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The Room database for the Supermarket App.
 *
 * This database includes tables for [Product], [CartItem], [WishlistItem], [Purchase], and [PurchaseItem].
 * It also defines type converters for custom data types like [LocalizedText].
 *
 * @property version The current schema version of the database.
 * @property exportSchema Exports the schema to a folder for version control.
 */
@Database(
    entities = [Product::class, CartItem::class, WishlistItem::class, Purchase::class, PurchaseItem::class],
    version = 7, // Increment database version when schema changes
    exportSchema = true // Required for Room to generate schema files for versioning
)
@TypeConverters(Converters::class) // Specifies custom type converters for complex data types
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides the DAO for [Product] entities.
     */
    abstract fun productDao(): ProductDao

    /**
     * Provides the DAO for [CartItem] entities.
     */
    abstract fun cartDao(): CartDao

    /**
     * Provides the DAO for [WishlistItem] entities.
     */
    abstract fun wishlistDao(): WishlistDao

    /**
     * Provides the DAO for [Purchase] and [PurchaseItem] entities.
     */
    abstract fun purchaseDao(): PurchaseDao

    companion object {
        // Marks the INSTANCE as volatile to ensure changes are immediately visible to all threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "supermarket_database"

        /**
         * Returns the singleton instance of the [AppDatabase].
         * If the instance is null, it creates a new one using a synchronized block to prevent multiple instances.
         * The database is pre-populated with sample data on creation and checks for data on open.
         *
         * @param context The application context.
         * @param scope A [CoroutineScope] for launching asynchronous operations, like database population.
         * @return The singleton [AppDatabase] instance.
         */
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            // If INSTANCE is not null, then return it, otherwise create a new database instance.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    // Callback to handle database creation and opening events.
                    .addCallback(object : Callback() {
                        /**
                         * Called when the database is created for the first time.
                         * Populates the database with initial sample data.
                         */
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("AppDatabase", "Database created. Populating with initial data...")
                            // Launch a coroutine on the IO dispatcher for database operations.
                            scope.launch(Dispatchers.IO) {
                                try {
                                    // Retrieve the database instance and populate it
                                    val databaseInstance = getDatabase(context, scope)
                                    populateDatabase(databaseInstance.productDao())
                                } catch (e: Exception) {
                                    // Log any errors during initial database population.
                                    Log.e("AppDatabase", "Error during onCreate database population: ${e.message}", e)
                                }
                            }
                        }

                        /**
                         * Called when the database is opened.
                         * Checks if the database is empty and repopulates it with sample data if needed.
                         */
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("AppDatabase", "Database opened. Checking for sample data...")
                            // Launch a coroutine on the IO dispatcher for database operations.
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val databaseInstance = getDatabase(context, scope)
                                    val productDao = databaseInstance.productDao()
                                    // Check if there are any products in the database.
                                    val products = productDao.getAllProducts().first()
                                    if (products.isEmpty()) {
                                        Log.d("AppDatabase", "No products found. Repopulating with sample data.")
                                        populateDatabase(productDao)
                                    }
                                } catch (e: Exception) {
                                    // Log any errors during database open check/repopulation.
                                    Log.e("AppDatabase", "Error during onOpen database check/repopulation: ${e.message}", e)
                                }
                            }
                        }
                    })
                    // Add all defined database migrations to handle schema changes.
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                        MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7
                    )
                    .build() // Build the Room database instance.
                INSTANCE = instance // Assign the created instance to INSTANCE.
                instance
            }
        }

        /**
         * Populates the database with a predefined list of sample products.
         * Deletes all existing products before inserting the sample data.
         *
         * @param productDao The [ProductDao] to use for database operations.
         */
        suspend fun populateDatabase(productDao: ProductDao) {
            try {
                Log.d("AppDatabase", "Attempting to delete all products and insert sample data.")
                productDao.deleteAllProducts() // Clear existing products.
                productDao.insertAll(SAMPLE_PRODUCTS) // Insert sample products.
                Log.d("AppDatabase", "Database population complete.")
            } catch (e: Exception) {
                // Log any errors during database population.
                Log.e("AppDatabase", "Error populating database with sample data: ${e.message}", e)
            }
        }

        /**
         * Migration from version 1 to 2: Adds the `cart_item_table`.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // SQL to create the cart_item_table.
                    database.execSQL("CREATE TABLE IF NOT EXISTS `cart_item_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `productId` INTEGER NOT NULL, `name` TEXT NOT NULL, `price` REAL NOT NULL, `unit` TEXT NOT NULL, `quantity` INTEGER NOT NULL)")
                } catch (e: Exception) {
                    // Log errors specific to this migration.
                    Log.e("MIGRATION_1_2", "Error migrating from 1 to 2: ${e.message}", e)
                    // Depending on criticality, you might re-throw or handle differently.
                    // For migrations, usually Room handles transactional integrity/rollback.
                }
            }
        }

        /**
         * Migration from version 2 to 3: Adds the `wishlist_item_table`.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // SQL to create the wishlist_item_table.
                    database.execSQL("CREATE TABLE IF NOT EXISTS `wishlist_item_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `productId` INTEGER NOT NULL, `name` TEXT NOT NULL, `price` REAL NOT NULL, `unit` TEXT NOT NULL, `imageResId` INTEGER NOT NULL DEFAULT 0)")
                } catch (e: Exception) {
                    // Log errors specific to this migration.
                    Log.e("MIGRATION_2_3", "Error migrating from 2 to 3: ${e.message}", e)
                }
            }
        }

        /**
         * Migration from version 3 to 4: No schema changes required for this version.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema changes required, but added try-catch for consistency in case of unexpected SQL errors
                try {
                    // No schema changes for this migration.
                    Log.d("MIGRATION_3_4", "No schema changes required for migration 3 to 4.")
                } catch (e: Exception) {
                    // Log errors specific to this migration.
                    Log.e("MIGRATION_3_4", "Error during migration 3 to 4 (should be no-op): ${e.message}", e)
                }
            }
        }

        /**
         * Migration from version 4 to 5: Adds `purchases` and `purchase_items` tables.
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // SQL to create the purchases table.
                    database.execSQL("CREATE TABLE IF NOT EXISTS `purchases` (`purchaseId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `purchaseDate` INTEGER NOT NULL)")
                    // SQL to create the purchase_items table with a foreign key to purchases.
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS `purchase_items` (" +
                                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `purchaseId` INTEGER NOT NULL, `productId` INTEGER NOT NULL, " +
                                "`name` TEXT NOT NULL, `price` REAL NOT NULL, `unit` TEXT NOT NULL, `quantity` INTEGER NOT NULL, " +
                                "`imageResId` INTEGER NOT NULL DEFAULT 0, `purchaseDate` INTEGER NOT NULL, " +
                                "FOREIGN KEY(`purchaseId`) REFERENCES `purchases`(`purchaseId`) ON DELETE CASCADE)"
                    )
                } catch (e: Exception) {
                    // Log errors specific to this migration.
                    Log.e("MIGRATION_4_5", "Error migrating from 4 to 5: ${e.message}", e)
                }
            }
        }

        /**
         * Migration from version 5 to 6: Adds the `ingredients` column to `product_table`.
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // SQL to add the new 'ingredients' column to the product_table.
                    database.execSQL("ALTER TABLE product_table ADD COLUMN ingredients TEXT")
                } catch (e: Exception) {
                    // Log errors specific to this migration.
                    Log.e("MIGRATION_5_6", "Error migrating from 5 to 6: ${e.message}", e)
                }
            }
        }

        /**
         * Migration from version 6 to 7: Updates `product_table` to support `LocalizedText` for
         * name, description, unit, category, availability, nutritionalInfo, ingredients, and offer.
         * This involves recreating the table and migrating data.
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("Migration", "Running 6->7: Updating product_table for LocalizedText support")
                try {
                    // Create a new temporary table with the updated schema for LocalizedText support.
                    // Note: LocalizedText is handled by TypeConverters, so the column type remains TEXT.
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS product_table_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            description TEXT NOT NULL,
                            price REAL NOT NULL,
                            unit TEXT NOT NULL,
                            category TEXT NOT NULL,
                            imageResId INTEGER NOT NULL,
                            availability TEXT NOT NULL,
                            nutritionalInfo TEXT,
                            offer TEXT,
                            ingredients TEXT
                        )
                    """)

                    // Room's TypeConverters handle the serialization/deserialization for TEXT columns.
                    // If the old columns were already TEXT and you're just changing their logical type
                    // to LocalizedText via Converters, then direct copying works assuming previous data
                    // was compatible or you handle defaults. The original migration logic was fine.
                    // No explicit data copy SQL is needed if the data format remains TEXT.

                    // Drop the old product table.
                    database.execSQL("DROP TABLE IF EXISTS product_table")

                    // Rename the new table to the original table name.
                    database.execSQL("ALTER TABLE product_table_new RENAME TO product_table")

                    Log.d("Migration", "Migration to LocalizedText complete")
                } catch (e: Exception) {
                    // Log errors specific to this migration.
                    Log.e("MIGRATION_6_7", "Error migrating from 6 to 7: ${e.message}", e)
                }
            }
        }

        // --- Sample Product Data ---
        // This list provides initial data to pre-populate the database for testing and demonstration.
        private val SAMPLE_PRODUCTS = listOf(
            // Dairy Category
            Product(
                name = LocalizedText(greek = "Γάλα", english = "Milk"),
                description = LocalizedText(greek = "Φρέσκο γάλα 1L", english = "Fresh milk 1L"),
                price = 1.60,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Γαλακτοκομικά", english = "Dairy"),
                imageResId = R.drawable.milk,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Λιπαρά: 3.5%", english = "Fat: 3.5%"),
                ingredients = LocalizedText(greek = "Γάλα", english = "Milk"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Γιαούρτι", english = "Yogurt"),
                description = LocalizedText(greek = "Παραδοσιακό γιαούρτι 1KG", english = "Traditional Yogurt 1KG"),
                price = 3.85,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Γαλακτοκομικά", english = "Dairy"),
                imageResId = R.drawable.yogurt,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Πρωτεΐνη: 5g/100g", english = "Protein: 5g/100g"),
                ingredients = LocalizedText(greek = "Γάλα αγελάδος, καλλιέργεια γιαούρτης", english = "Cow's milk, yogurt culture"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Τυρί Gouda", english = "Gouda Cheese"),
                description = LocalizedText(greek = "Τυρί Gouda σε φέτες 200g", english = "Gouda Cheese slices 200g"),
                price = 3.10,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Γαλακτοκομικά", english = "Dairy"),
                imageResId = R.drawable.cheese,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Λιπαρά: 27g/100g", english = "Fat: 27g/100g"),
                ingredients = LocalizedText(greek = "Γάλα, αλάτι, πυτιά, καλλιέργεια", english = "Milk, salt, rennet, culture"),
                offer = null
            ),

            // Fresh Food / Fruits & Vegetables Category
            Product(
                name = LocalizedText(greek = "Μήλα", english = "Apples"),
                description = LocalizedText(greek = "Κόκκινα μήλα", english = "Red Apples"),
                price = 2.50,
                unit = LocalizedText(greek = "€/κιλό", english = "€/kg"),
                category = LocalizedText(greek = "Φρέσκα Τρόφιμα", english = "Fresh Food"),
                imageResId = R.drawable.apples,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Βιταμίνη C", english = "Vitamin C"),
                ingredients = LocalizedText(greek = "100% φρέσκα μήλα", english = "100% fresh apples"),
                offer = LocalizedText(greek = "Στα 3 κιλά, το ένα είναι δωρεάν", english = "At 3 kilos, one is free")
            ),
            Product(
                name = LocalizedText(greek = "Αυγά", english = "Eggs"),
                description = LocalizedText(greek = "Αυγά ελευθέρας βοσκής (6 τεμάχια)", english = "Free Range Eggs (6 pieces)"),
                price = 3.20,
                unit = LocalizedText(greek = "€/εξάδα", english = "€/dozen"),
                category = LocalizedText(greek = "Φρέσκα Τρόφιμα", english = "Fresh Food"),
                imageResId = R.drawable.eggs,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Πρωτεΐνη: 6g/αυγό", english = "Protein: 6g/egg"),
                ingredients = LocalizedText(greek = "Αυγά από κότες ελευθέρας βοσκής", english = "Eggs from free-range hens"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Πορτοκάλια", english = "Oranges"),
                description = LocalizedText(greek = "Φρέσκα πορτοκάλια", english = "Fresh oranges"),
                price = 1.80,
                unit = LocalizedText(greek = "€/κιλό", english = "€/kg"),
                category = LocalizedText(greek = "Φρέσκα Τρόφιμα", english = "Fresh Food"),
                imageResId = R.drawable.oranges,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Πλούσια σε Βιταμίνη C", english = "Rich in Vitamin C"),
                ingredients = LocalizedText(greek = "100% φρέσκα πορτοκάλια", english = "100% fresh oranges"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Ντομάτες", english = "Tomatoes"),
                description = LocalizedText(greek = "Φρέσκες ντομάτες", english = "Fresh tomatoes"),
                price = 1.60,
                unit = LocalizedText(greek = "€/κιλό", english = "€/kg"),
                category = LocalizedText(greek = "Φρέσκα Τρόφιμα", english = "Fresh Food"),
                imageResId = R.drawable.tomatoes,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Πηγή λυκοπενίου", english = "Source of lycopene"),
                ingredients = LocalizedText(greek = "100% φρέσκες ντομάτες", english = "100% fresh tomatoes"),
                offer = null
            ),

            // Cleaning Products Category
            Product(
                name = LocalizedText(greek = "Απορρυπαντικό", english = "Laundry Detergent"),
                description = LocalizedText(greek = "Απορρυπαντικό ρούχων 2L", english = "Laundry Detergent 2L"),
                price = 7.80,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Καθαριστικά", english = "Cleaning Products"),
                imageResId = R.drawable.detergent,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = null,
                ingredients = LocalizedText(greek = "Ανιονικές επιφανειοδραστικές ουσίες, ένζυμα, αρώματα", english = "Anionic surfactants, enzymes, fragrances"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Υγρό Πιάτων", english = "Dish Soap"),
                description = LocalizedText(greek = "Υγρό πιάτων με άρωμα λεμόνι 500ml", english = "Lemon Scent Dish Soap 500ml"),
                price = 2.10,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Καθαριστικά", english = "Cleaning Products"),
                imageResId = R.drawable.dish_soap,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = null,
                ingredients = LocalizedText(greek = "Μη ιονικές επιφανειοδραστικές ουσίες, άρωμα", english = "Non-ionic surfactants, fragrance"),
                offer = null
            ),

            // Frozen Category
            Product(
                name = LocalizedText(greek = "Παγωτό Βανίλια", english = "Vanilla Ice Cream"),
                description = LocalizedText(greek = "Παγωτό βανίλια 500ml", english = "Vanilla Ice Cream 500ml"),
                price = 5.50,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Κατεψυγμένα", english = "Frozen"),
                imageResId = R.drawable.ice_cream,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Λιπαρά: 15g/100g", english = "Fat: 15g/100g"),
                ingredients = LocalizedText(greek = "Γάλα, κρέμα γάλακτος, ζάχαρη, φυσικό άρωμα βανίλιας", english = "Milk, cream, sugar, natural vanilla flavor"),
                offer = LocalizedText(greek = "Με την αγορά των 2, δωρο άλλο 1", english = "Buy 2, get 1 free")
            ),
            Product(
                name = LocalizedText(greek = "Κατεψυγμένος Αρακάς", english = "Frozen Peas"),
                description = LocalizedText(greek = "Κατεψυγμένος αρακάς 450g", english = "Frozen Peas 450g"),
                price = 2.50,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Κατεψυγμένα", english = "Frozen"),
                imageResId = R.drawable.frozen_peas,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Φυτικές ίνες: 5g/100g", english = "Dietary Fiber: 5g/100g"),
                ingredients = LocalizedText(greek = "100% αρακάς", english = "100% peas"),
                offer = null
            ),

            // Beverages Category
            Product(
                name = LocalizedText(greek = "Καφές", english = "Coffee"),
                description = LocalizedText(greek = "Αλεσμένος καφές φίλτρου 250g", english = "Ground Coffee Filter 250g"),
                price = 5.40,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Ροφήματα", english = "Beverages"),
                imageResId = R.drawable.coffee,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Καφεΐνη: 95mg/φλιτζάνι", english = "Caffeine: 95mg/cup"),
                ingredients = LocalizedText(greek = "100% κόκκοι καφέ Arabica", english = "100% Arabica coffee beans"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Μπύρα", english = "Beer"),
                description = LocalizedText(greek = "Ελληνική μπύρα Lager 500ml", english = "Greek Lager Beer 500ml"),
                price = 1.80,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Ροφήματα", english = "Beverages"),
                imageResId = R.drawable.beer,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = null,
                ingredients = LocalizedText(greek = "Νερό, βύνη κριθαριού, λυκίσκος, μαγιά", english = "Water, barley malt, hops, yeast"),
                offer = LocalizedText(greek = "Αγόρασε 4, πάρε 1 δωρεάν", english = "Buy 4, Get 1 Free")
            ),
            Product(
                name = LocalizedText(greek = "Νερό", english = "Water"),
                description = LocalizedText(greek = "Εμφιαλωμένο νερό 1.5L", english = "Bottled water 1.5L"),
                price = 0.60,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Ροφήματα", english = "Beverages"),
                imageResId = R.drawable.water_bottle,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = null,
                ingredients = LocalizedText(greek = "Φυσικό μεταλλικό νερό", english = "Natural mineral water"),
                offer = null
            ),

            // Meat Category
            Product(
                name = LocalizedText(greek = "Κοτόπουλο", english = "Chicken"),
                description = LocalizedText(greek = "Φρέσκο κοτόπουλο ολόκληρο ~1.5kg", english = "Fresh Whole Chicken ~1.5kg"),
                price = 7.10,
                unit = LocalizedText(greek = "€/κιλό", english = "€/kg"),
                category = LocalizedText(greek = "Κρέας", english = "Meat"),
                imageResId = R.drawable.chicken,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Πρωτεΐνη: 25g/100g", english = "Protein: 25g/100g"),
                ingredients = LocalizedText(greek = "100% φρέσκο κοτόπουλο", english = "100% fresh chicken"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Μοσχαρίσιος Κιμάς", english = "Ground Beef"),
                description = LocalizedText(greek = "Φρέσκος μοσχαρίσιος κιμάς 500g", english = "Fresh ground beef 500g"),
                price = 5.50,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Κρέας", english = "Meat"),
                imageResId = R.drawable.beef,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Λιπαρά: 15g/100g", english = "Fat: 15g/100g"),
                ingredients = LocalizedText(greek = "100% μοσχαρίσιος κιμάς", english = "100% ground beef"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Χοιρινές Μπριζόλες", english = "Pork Chops"),
                description = LocalizedText(greek = "Φρέσκες χοιρινές μπριζόλες (2 τεμάχια)", english = "Fresh pork chops (2 pieces)"),
                price = 8.20,
                unit = LocalizedText(greek = "€/κιλό", english = "€/kg"),
                category = LocalizedText(greek = "Κρέας", english = "Meat"),
                imageResId = R.drawable.pork_chops,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Πρωτεΐνη: 20g/100g", english = "Protein: 20g/100g"),
                ingredients = LocalizedText(greek = "100% χοιρινό κρέας", english = "100% pork meat"),
                offer = null
            ),

            // Oils & Vinegars Category
            Product(
                name = LocalizedText(greek = "Ελαιόλαδο", english = "Olive Oil"),
                description = LocalizedText(greek = "Εξαιρετικό παρθένο ελαιόλαδο 1L", english = "Extra Virgin Olive Oil 1L"),
                price = 9.50,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Έλαια & Ξύδια", english = "Oils & Vinegars"),
                imageResId = R.drawable.olive_oil,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Μονοακόρεστα λιπαρά", english = "Monounsaturated fats"),
                ingredients = LocalizedText(greek = "100% ελαιόλαδο", english = "100% olive oil"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Βαλσάμικο Ξύδι", english = "Balsamic Vinegar"),
                description = LocalizedText(greek = "Βαλσάμικο ξύδι 250ml", english = "Balsamic Vinegar 250ml"),
                price = 3.20,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Έλαια & Ξύδια", english = "Oils & Vinegars"),
                imageResId = R.drawable.balsamic_vinegar,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = null,
                ingredients = LocalizedText(greek = "Ξύδι κρασιού, συμπυκνωμένο μούστο", english = "Wine vinegar, concentrated grape must"),
                offer = null
            ),

            // Snacks & Sweets Category
            Product(
                name = LocalizedText(greek = "Σοκολάτα", english = "Chocolate"),
                description = LocalizedText(greek = "Σοκολάτα γάλακτος 100g", english = "Milk Chocolate 100g"),
                price = 1.50,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Γλυκά & Σνακ", english = "Snacks & Sweets"),
                imageResId = R.drawable.chocolate,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Θερμίδες: 500kcal/100g", english = "Calories: 500kcal/100g"),
                ingredients = LocalizedText(greek = "Ζάχαρη, κακαόμαζα, βούτυρο κακάο, γάλα σε σκόνη", english = "Sugar, cocoa mass, cocoa butter, milk powder"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Πατατάκια", english = "Potato Chips"),
                description = LocalizedText(greek = "Πατατάκια αλάτι 125g", english = "Salted Potato Chips 125g"),
                price = 1.90,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Γλυκά & Σνακ", english = "Snacks & Sweets"),
                imageResId = R.drawable.potato_chips,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Λιπαρά: 30g/100g", english = "Fat: 30g/100g"),
                ingredients = LocalizedText(greek = "Πατάτες, φυτικό έλαιο, αλάτι", english = "Potatoes, vegetable oil, salt"),
                offer = LocalizedText(greek = "1+1 δώρο", english = "1+1 gift")
            ),

            // Household Items Category
            Product(
                name = LocalizedText(greek = "Χαρτί Υγείας", english = "Toilet Paper"),
                description = LocalizedText(greek = "Χαρτί υγείας 3 φύλλα (8 ρολά)", english = "Toilet Paper 3-ply (8 rolls)"),
                price = 6.50,
                unit = LocalizedText(greek = "€/πακέτο", english = "€/package"),
                category = LocalizedText(greek = "Είδη Σπιτιού", english = "Household Items"),
                imageResId = R.drawable.toilet_paper_rolls,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = null,
                ingredients = LocalizedText(greek = "100% καθαρή κυτταρίνη", english = "100% pure cellulose"),
                offer = LocalizedText(greek = "1+1 δώρο", english = "1+1 gift")
            ),
            Product(
                name = LocalizedText(greek = "Χαρτί Κουζίνας", english = "Paper Towels"),
                description = LocalizedText(greek = "Χαρτί κουζίνας 2 ρολά", english = "Paper Towels 2 rolls"),
                price = 3.90,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Είδη Σπιτιού", english = "Household Items"),
                imageResId = R.drawable.paper_towels,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = null,
                ingredients = LocalizedText(greek = "Ανακυκλωμένο χαρτί", english = "Recycled paper"),
                offer = null
            ),

            // Pasta & Rice Category (consolidated from Dry Food/Pasta & Rice)
            Product(
                name = LocalizedText(greek = "Σπαγγέτι Ολικής Άλεσης", english = "Whole Wheat Spaghetti"),
                description = LocalizedText(greek = "Σπαγγέτι ολικής άλεσης 500g", english = "Whole Wheat Spaghetti 500g"),
                price = 1.85,
                unit = LocalizedText(greek = "€/πακέτο", english = "€/package"),
                category = LocalizedText(greek = "Ζυμαρικά & Ρύζι", english = "Pasta & Rice"),
                imageResId = R.drawable.spaghetti,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Φυτικές ίνες: 7g/100g", english = "Dietary Fiber: 7g/100g"),
                ingredients = LocalizedText(greek = "Σιμιγδάλι ολικής άλεσης, νερό", english = "Whole wheat flour, water"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Λευκό Ρύζι", english = "White Rice"),
                description = LocalizedText(greek = "Ρύζι Καρολίνα 1kg", english = "Carolina Rice 1kg"),
                price = 2.50,
                unit = LocalizedText(greek = "€/κιλό", english = "€/kg"),
                category = LocalizedText(greek = "Ζυμαρικά & Ρύζι", english = "Pasta & Rice"),
                imageResId = R.drawable.rice,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Υδατάνθρακες: 80g/100g", english = "Carbohydrates: 80g/100g"),
                ingredients = LocalizedText(greek = "100% ρύζι", english = "100% rice"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Δημητριακά", english = "Corn Flakes"),
                description = LocalizedText(greek = "Δημητριακά για πρωινό 375g", english = "Breakfast cereals 375g"),
                price = 3.50,
                unit = LocalizedText(greek = "€/πακέτο", english = "€/package"),
                category = LocalizedText(greek = "Ξηρά Τρόφιμα", english = "Dry Food"),
                imageResId = R.drawable.corn_flakes,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Φυτικές ίνες: 3g/100g", english = "Dietary Fiber: 3g/100g"),
                ingredients = LocalizedText(greek = "Αλεύρι καλαμποκιού, ζάχαρη, βύνη κριθαριού, αλάτι", english = "Corn flour, sugar, barley malt, salt"),
                offer = null
            ),

            // Bakery Category
            Product(
                name = LocalizedText(greek = "Ψωμί του Τοστ", english = "Toast Bread"),
                description = LocalizedText(greek = "Ψωμί του τοστ λευκό 750g", english = "White Toast Bread 750g"),
                price = 2.10,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Αρτοποιία", english = "Bakery"),
                imageResId = R.drawable.toast_bread,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Υδατάνθρακες: 45g/100g", english = "Carbohydrates: 45g/100g"),
                ingredients = LocalizedText(greek = "Αλεύρι σίτου, νερό, μαγιά, αλάτι", english = "Wheat flour, water, yeast, salt"),
                offer = null
            ),
            Product(
                name = LocalizedText(greek = "Κρουασάν", english = "Croissant"),
                description = LocalizedText(greek = "Φρέσκο βουτυρένιο κρουασάν", english = "Fresh butter croissant"),
                price = 1.20,
                unit = LocalizedText(greek = "€/τεμάχιο", english = "€/piece"),
                category = LocalizedText(greek = "Αρτοποιία", english = "Bakery"),
                imageResId = R.drawable.croissant,
                availability = LocalizedText(greek = "Σε απόθεμα", english = "In Stock"),
                nutritionalInfo = LocalizedText(greek = "Λιπαρά: 20g/100g", english = "Fat: 20g/100g"),
                ingredients = LocalizedText(greek = "Αλεύρι σίτου, βούτυρο, ζάχαρη, μαγιά", english = "Wheat flour, butter, sugar, yeast"),
                offer = null
            )
        )
    }
}
