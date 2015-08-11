package me.c_o_s.yandexmoneytreetest.database;

import android.provider.BaseColumns;

/**
 * Created by Constantine on 8/11/2015.
 */
public final class YandexMoneyContract {
    public YandexMoneyContract() {}

    public static abstract class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "category";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_YANDEX_ID = "yandexid";
        public static final String COLUMN_NAME_PARENT_ID = "parentid";
    }
}
