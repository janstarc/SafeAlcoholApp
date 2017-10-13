package com.jan.safealcohol;

import android.provider.BaseColumns;

public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "drinkingHistory";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_AMOUNT = "amount";
        public static final String COLUMN_NAME_UNITS = "units";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";

    }
}