package com.jan.safealcohol;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FeedReaderDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedReaderContract.FeedEntry.TABLE_NAME + " (" +
                    FeedReaderContract.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_NAME + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT + " REAL," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS + " REAL," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP + " TEXT )";

    private static final String SQL_CREATE_ENTRIES2 =
            "CREATE TABLE " + FeedReaderContract.FeedEntry.TABLE2_NAME + " (" +
                    FeedReaderContract.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_FIRSTNAME + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_LASTNAME + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_WEIGHT + " INTEGER," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_GENDER + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_HEIGHT + " INTEGER," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_SIZEOFMEAL + " INTEGER," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_MEALTIME + " TEXT )";


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedReaderContract.FeedEntry.TABLE_NAME;


    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "FeedReader.db";

    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES2);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}