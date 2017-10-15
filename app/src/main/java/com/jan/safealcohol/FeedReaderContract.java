package com.jan.safealcohol;

import android.provider.BaseColumns;

public final class FeedReaderContract {

    private FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "drinkingHistory";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_AMOUNT = "amount";
        public static final String COLUMN_NAME_UNITS = "units";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";

        public static final String TABLE2_NAME = "userDataNew";
        public static final String COLUMN_NAME_FIRSTNAME = "firstname";
        public static final String COLUMN_NAME_LASTNAME = "lastname";
        public static final String COLUMN_NAME_WEIGHT = "weight";
        public static final String COLUMN_NAME_GENDER = "gender";
        public static final String COLUMN_NAME_HEIGHT = "height";
        public static final String COLUMN_NAME_SIZEOFMEAL = "sizeofmeal";
        public static final String COLUMN_NAME_MEALTIME = "mealtime";
    }
}