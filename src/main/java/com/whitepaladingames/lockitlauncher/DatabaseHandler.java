package com.whitepaladingames.lockitlauncher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "postsDatabase";
    private static final int DATABASE_VERSION = 5;

    // Table Names
    private static final String TABLE_APPS = "apps";

    // Apps Table Columns
    private static final String KEY_APP_PACKAGE = "appPackage";
    private static final String KEY_APP_NAME = "appName";
    private static final String KEY_APP_TYPE = "appType";

    private static DatabaseHandler sInstance;

    public static synchronized DatabaseHandler getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHandler(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_APPS +
                "(" +
                KEY_APP_PACKAGE + " TEXT PRIMARY KEY," +
                KEY_APP_NAME + " TEXT," +
                KEY_APP_TYPE + " TEXT" +
                ")";

        db.execSQL(CREATE_USERS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            //List<AppDetail> apps = getAllApps();
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPS);
            onCreate(db);

            //for(int x = 0; x < apps.size(); x++) {
             //   addorUpdateUser(apps.get(x));
            //}
        }
    }

    // Insert or update an app in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
    public long addorUpdateUser(AppDetail detail) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_APP_PACKAGE, detail.name);
            values.put(KEY_APP_NAME, detail.label);
            values.put(KEY_APP_TYPE, detail.type);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = db.update(TABLE_APPS, values, KEY_APP_PACKAGE + "= ?", new String[]{detail.name.toString()});

            // Check if update succeeded
            if (rows == 1) {
            } else {
                // user with this userName did not already exist, so insert new user
                userId = db.insertOrThrow(TABLE_APPS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d("test", e.toString());
        } finally {
            db.endTransaction();
        }
        return userId;
    }

    // Get all posts in the database
    public List<AppDetail> getAllApps() {
        List<AppDetail> posts = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String POSTS_SELECT_QUERY = String.format("SELECT * FROM %s", TABLE_APPS);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    AppDetail app = new AppDetail();

                    app.name = cursor.getString(cursor.getColumnIndex(KEY_APP_PACKAGE));
                    app.label = cursor.getString(cursor.getColumnIndex(KEY_APP_NAME));
                    app.type = cursor.getString(cursor.getColumnIndex(KEY_APP_TYPE));
                    posts.add(app);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("test", e.toString());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return posts;
    }

    public boolean deleteApp(String name) {
        boolean result = true;
        String DELETE_QUERY = String.format("DELETE FROM %s WHERE %s = '%s'", TABLE_APPS, KEY_APP_PACKAGE, name);
        String FIND_QUERY = String.format("SELECT * FROM %s WHERE %s = '%s'", TABLE_APPS, KEY_APP_PACKAGE, name);
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL(DELETE_QUERY);
        Cursor cursor = db.rawQuery(FIND_QUERY, null);
        try {
            if (cursor.getCount() > 0) {
                result = false;
            }
        } catch (Exception e) {
            result = false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }
}
