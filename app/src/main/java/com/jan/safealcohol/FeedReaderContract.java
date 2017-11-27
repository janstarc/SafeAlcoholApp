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
        public static final String COLUMN_NAME_PERCENTAGE = "percentage";

        /*
        public static final String TABLE2_NAME = "drinks";
        public static final String COLUMN_NAME_DRINKNAME = "drinkname";
        public static final String COLUMN_NAME_PERCENTAGE = "percentage";
        public static final String COLUMN_NAME_IMAGE = "image";
        public static final String COLUMN_NAME_DRINKAMOUNT = "drinkamount";
        */

    }
}