package me.c_o_s.yandexmoneytreetest.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Constantine on 8/11/2015.
 */
public class YandexMoneyDbHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + YandexMoneyContract.CategoryEntry.TABLE_NAME + " (" +
                    YandexMoneyContract.CategoryEntry._ID + " INTEGER PRIMARY KEY," +
                    YandexMoneyContract.CategoryEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    YandexMoneyContract.CategoryEntry.COLUMN_NAME_YANDEX_ID + INTEGER_TYPE + COMMA_SEP +
                    YandexMoneyContract.CategoryEntry.COLUMN_NAME_PARENT_ID + INTEGER_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + YandexMoneyContract.CategoryEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "YandexMoneyTree.db";

    public YandexMoneyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
