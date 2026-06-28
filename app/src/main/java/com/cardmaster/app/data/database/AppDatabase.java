package com.cardmaster.app.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.cardmaster.app.data.dao.BoosterChargeDao;
import com.cardmaster.app.data.dao.BoosterDao;
import com.cardmaster.app.data.dao.CardDao;
import com.cardmaster.app.data.dao.OwnedCardDao;
import com.cardmaster.app.data.dao.UserCurrencyDao;
import com.cardmaster.app.data.entity.Booster;
import com.cardmaster.app.data.entity.BoosterCharge;
import com.cardmaster.app.data.entity.Card;
import com.cardmaster.app.data.entity.OwnedCard;
import com.cardmaster.app.data.entity.UserCurrency;

@Database(
    entities = {Booster.class, Card.class, OwnedCard.class, UserCurrency.class, BoosterCharge.class},
    version = 6,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract BoosterDao boosterDao();
    public abstract CardDao cardDao();
    public abstract OwnedCardDao ownedCardDao();
    public abstract UserCurrencyDao userCurrencyDao();
    public abstract BoosterChargeDao boosterChargeDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "cardmaster_database"
            ).allowMainThreadQueries()
             .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
             .build();
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add status column with default value "active" (nullable)
            database.execSQL("ALTER TABLE boosters ADD COLUMN status TEXT DEFAULT 'active'");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Update default tokens from 9,000,000 to 1,000
            database.execSQL("UPDATE user_currency SET tokens = 1000 WHERE id = 1");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create new table with orderIndex column as NOT NULL
            database.execSQL("CREATE TABLE boosters_new (id INTEGER PRIMARY KEY NOT NULL, name TEXT, artworkUrl TEXT, totalCards INTEGER NOT NULL, releaseDate TEXT, status TEXT, orderIndex INTEGER NOT NULL DEFAULT 0)");
            
            // Copy data from old table to new table, initializing orderIndex based on releaseDate
            database.execSQL("INSERT INTO boosters_new (id, name, artworkUrl, totalCards, releaseDate, status, orderIndex) " +
                           "SELECT id, name, artworkUrl, totalCards, releaseDate, status, " +
                           "(SELECT COUNT(*) FROM boosters b2 WHERE b2.releaseDate > boosters.releaseDate) " +
                           "FROM boosters");
            
            // Drop old table
            database.execSQL("DROP TABLE boosters");
            
            // Rename new table to old name
            database.execSQL("ALTER TABLE boosters_new RENAME TO boosters");
        }
    };

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
