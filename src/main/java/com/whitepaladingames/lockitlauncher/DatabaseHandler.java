package com.whitepaladingames.lockitlauncher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "postsDatabase";
    private static final int DATABASE_VERSION = 15;

    // Table Names
    private static final String TABLE_APPS = "apps";
    private static final String TABLE_APP_INFO = "appInfo";
    private static final String TABLE_TIMER = "appTimer";

    // Table Columns
    private static final String KEY_APP_PACKAGE = "appPackage";
    private static final String KEY_APP_NAME = "appName";
    private static final String KEY_APP_TYPE = "appType";
    private static final String FIRST_TIME_COLUMN = "firstTime";
    private static final String PASSWORD_COLUMN = "password";
    private static final String ADMIN_EMAIL_COLUMN = "adminEmail";
    private static final String DEVICE_NAME_COLUMN = "deviceName";
    private static final String ADMIN_SCREENTIMEOUT = "screenTimeout";
    private static final String ADMIN_USE_TIMEOUT = "useTimeout";
    private static final String ADMIN_WALLPAPER = "wallpaper";
    private static final String TIMER_TIME = "totalTime";
    private static final String TIMER_USER = "timerUser";
    private static final String TIMER_DATE = "timerDate";

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

        String CREATE_APP_TABLE = "CREATE TABLE " + TABLE_APP_INFO + " (" +
                FIRST_TIME_COLUMN + " INTEGER, " +
                PASSWORD_COLUMN + " TEXT," +
                DEVICE_NAME_COLUMN + " TEXT," +
                ADMIN_EMAIL_COLUMN + " TEXT," +
                ADMIN_SCREENTIMEOUT + " INTEGER," +
                ADMIN_USE_TIMEOUT + " TEXT," +
                ADMIN_WALLPAPER + " TEXT" +
                ")";

        try {
            db.execSQL(CREATE_APP_TABLE);
        } catch (Exception e) {
            Log.d("db", e.toString());
        }

        createTimerTable(db);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            List<AppDetail> apps = getAllApps(db);
            AppInfo appInfo = getAppInfo(db);
            AppTimer appTimer = getTimer(db, AppConstants.DEFAULT_USER);

            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APP_INFO);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMER);
            onCreate(db);

            for(int x = 0; x < apps.size(); x++) {
                addorUpdateApp(apps.get(x), db);
            }

            updateAppInfo(appInfo, db);
            saveTimer(db, appTimer);
        }
    }

    private void createTimerTable(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_TIMER +
                "(" +
                TIMER_USER + " TEXT PRIMARY KEY," +
                TIMER_DATE + " TEXT," +
                TIMER_TIME + " TEXT" +
                ")";

        db.execSQL(CREATE_USERS_TABLE);
    }

    public long addorUpdateApp(AppDetail app) {
        return addorUpdateApp(app, getWritableDatabase());
    }
    // Insert or update an app in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the user does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
    public long addorUpdateApp(AppDetail detail, SQLiteDatabase db) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        long userId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_APP_PACKAGE, detail.name);
            values.put(KEY_APP_NAME, detail.label);
            values.put(KEY_APP_TYPE, detail.type);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            deleteApp(detail.name);
            // user with this userName did not already exist, so insert new user
            userId = db.insertOrThrow(TABLE_APPS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("test", e.toString());
        } finally {
            db.endTransaction();
        }
        return userId;
    }

    public List<AppDetail> getAllApps() {
        return getAllApps(getReadableDatabase());
    }

    public List<AppDetail> getAllApps(SQLiteDatabase db) {
        List<AppDetail> posts = new ArrayList<>();

        String POSTS_SELECT_QUERY = String.format("SELECT * FROM %s", TABLE_APPS);

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

    public boolean updateAppInfo(AppInfo appInfo) {
        return updateAppInfo(appInfo, getWritableDatabase());
    }

    public boolean updateAppInfo(AppInfo appInfo, SQLiteDatabase db) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(FIRST_TIME_COLUMN, appInfo.firstTime);
            values.put(PASSWORD_COLUMN, appInfo.password);
            values.put(ADMIN_EMAIL_COLUMN, appInfo.adminEmail);
            values.put(DEVICE_NAME_COLUMN, appInfo.deviceName);
            if (appInfo.useTimout) values.put(ADMIN_USE_TIMEOUT, AppConstants.TRUE);
            else values.put(ADMIN_USE_TIMEOUT, AppConstants.FALSE);
            values.put(ADMIN_SCREENTIMEOUT, AppConstants.DEFAULT_SCREEN_TIMEOUT);
            values.put(ADMIN_WALLPAPER, appInfo.wallpaper);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            //int rows = db.update(TABLE_APP_INFO, values, null, null);
            try {
                db.execSQL("DELETE FROM " + TABLE_APP_INFO);
            } catch (Exception ue) {
                Log.d("db", ue.toString());
            }
            db.insertOrThrow(TABLE_APP_INFO, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("test", e.toString());
        } finally {
            db.endTransaction();
        }

        return true;
    }

    public AppInfo getAppInfo() {
        return getAppInfo(getReadableDatabase());
    }

    public AppInfo getAppInfo(SQLiteDatabase db) {
        AppInfo result = new AppInfo();
        result.firstTime = AppConstants.FIRST_TIME;
        result.password = AppConstants.EMPTY_STRING;
        result.adminEmail = AppConstants.EMPTY_STRING;
        result.deviceName = AppConstants.EMPTY_STRING;
        result.lastUser = AppConstants.DEFAULT_USER;
        result.totalTime = AppConstants.DEFAULT_SCREEN_TIMEOUT;
        result.useTimout = false;
        result.wallpaper = AppConstants.EMPTY_STRING;

        String POSTS_SELECT_QUERY = String.format("SELECT * FROM %s", TABLE_APP_INFO);
        try {
            Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
            try {

                if (cursor.moveToFirst()) {
                    do {
                        int col = cursor.getColumnIndex(FIRST_TIME_COLUMN);
                        if (col > -1)
                            result.firstTime = cursor.getInt(col);
                        col = cursor.getColumnIndex(PASSWORD_COLUMN);
                        if (col > -1)
                            result.password = cursor.getString(col);
                        col = cursor.getColumnIndex(ADMIN_EMAIL_COLUMN);
                        if (col > -1)
                            result.adminEmail = cursor.getString(col);
                        col = cursor.getColumnIndex(DEVICE_NAME_COLUMN);
                        if (col > -1)
                            result.deviceName = cursor.getString(col);
                        col = cursor.getColumnIndex(ADMIN_SCREENTIMEOUT);
                        if (col > -1)
                            result.totalTime = cursor.getInt(col);
                        col = cursor.getColumnIndex(ADMIN_USE_TIMEOUT);
                        if (col > -1) {
                            String s = cursor.getString(col);
                            if (s.equals(AppConstants.TRUE))
                                result.useTimout = false;
                        }
                        col = cursor.getColumnIndex(ADMIN_WALLPAPER);
                        if (col > -1)
                            result.wallpaper = cursor.getString(col);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                Log.d("test", e.toString());
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.d("test", e.toString());
        }
        return result;
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

    public AppTimer getTimer(SQLiteDatabase db, String user) {
        AppTimer result = new AppTimer();
        result._totalTime = AppConstants.DEFAULT_SCREEN_TIMEOUT;
        result._time = 0;
        result._userId = user;
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        result._date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        String POSTS_SELECT_QUERY = String.format("SELECT * FROM %s WHERE %s = '%s'", TABLE_TIMER, TIMER_USER, user);
        try {
            Cursor cursor = db.rawQuery(POSTS_SELECT_QUERY, null);
            try {
                if (cursor.moveToFirst()) {
                    int col = cursor.getColumnIndex(TIMER_DATE);
                    if (col > -1) {
                        String d = cursor.getString(col);
                        if (d.equals(result._date)) {
                            col = cursor.getColumnIndex(TIMER_TIME);
                            if (col > -1)
                                result._time = cursor.getInt(col);
                        }
                    }
                    col = cursor.getColumnIndex(TIMER_USER);
                    if (col > -1)
                        result._userId = cursor.getString(col);
                }
            } catch (Exception e) {
                Log.d("test", e.toString());
            } finally {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.d("test", e.toString());
        }

        return result;
    }

    public AppTimer getTimer(String user) {
        return getTimer(getReadableDatabase(), user);
    }

    public boolean saveTimer(AppTimer appTimer) {
        return saveTimer(getReadableDatabase(), appTimer);
    }

    public boolean saveTimer(SQLiteDatabase db, AppTimer appTimer) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(TIMER_USER, appTimer._userId);
            values.put(TIMER_DATE, appTimer._date);
            values.put(TIMER_TIME, appTimer._time);

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            //int rows = db.update(TABLE_APP_INFO, values, null, null);
            try {
                db.execSQL("DELETE FROM " + TABLE_TIMER + " WHERE " + TIMER_USER + " = '" + appTimer._userId + "'");
            } catch (Exception ue) {
                Log.d("db", ue.toString());
            }
            db.insertOrThrow(TABLE_TIMER, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("test", e.toString());
        } finally {
            db.endTransaction();
        }

        return true;
    }
}
