package com.udacity.stockhawk.data;


import android.net.Uri;
import android.provider.BaseColumns;

import com.google.common.collect.ImmutableList;

public final class Contract {

    static final String AUTHORITY = "com.udacity.stockhawk";
    static final String PATH_QUOTE = "quote";
    static final String PATH_QUOTE_WITH_SYMBOL = "quote/*";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    private Contract() {
    }

    @SuppressWarnings("unused")
    public static final class Quote implements BaseColumns {

        public static final Uri URI = BASE_URI.buildUpon().appendPath(PATH_QUOTE).build();
        public static final String COLUMN_SYMBOL = "symbol";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_ABSOLUTE_CHANGE = "absolute_change";
        public static final String COLUMN_PERCENTAGE_CHANGE = "percentage_change";
        public static final String COLUMN_HISTORY_1_WEEK = "history_1_week";
        public static final String COLUMN_HISTORY_1_MONTH = "history_1_month";
        public static final String COLUMN_HISTORY_6_MONTH = "history_6_month";
        public static final String COLUMN_HISTORY_1_YEAR = "history_1_year";
        public static final String COLUMN_HISTORY_2_YEAR = "history_2_year";

        public static final int POSITION_ID = 0;
        public static final int POSITION_SYMBOL = 1;
        public static final int POSITION_PRICE = 2;
        public static final int POSITION_ABSOLUTE_CHANGE = 3;
        public static final int POSITION_PERCENTAGE_CHANGE = 4;
        public static final int POSITION_HISTORY_1_WEEK = 5;
        public static final int POSITION_HISTORY_1_MONTH = 6;
        public static final int POSITION_HISTORY_6_MONTH = 7;
        public static final int POSITION_HISTORY_1_YEAR = 8;
        public static final int POSITION_HISTORY_2_YEAR = 9;



        public static final ImmutableList<String> QUOTE_COLUMNS = ImmutableList.of(
                _ID,
                COLUMN_SYMBOL,
                COLUMN_PRICE,
                COLUMN_ABSOLUTE_CHANGE,
                COLUMN_PERCENTAGE_CHANGE,
                COLUMN_HISTORY_1_WEEK,
                COLUMN_HISTORY_1_MONTH,
                COLUMN_HISTORY_6_MONTH,
                COLUMN_HISTORY_1_YEAR,
                COLUMN_HISTORY_2_YEAR
                );
        public static final String[] HISTORY_COLUMNS = {
                COLUMN_HISTORY_1_WEEK,
                COLUMN_HISTORY_1_MONTH,
                COLUMN_HISTORY_6_MONTH,
                COLUMN_HISTORY_1_YEAR,
                COLUMN_HISTORY_2_YEAR
        };
        static final String TABLE_NAME = "quotes";

        public static Uri makeUriForStock(String symbol) {
            return URI.buildUpon().appendPath(symbol).build();
        }

        static String getStockFromUri(Uri queryUri) {
            return queryUri.getLastPathSegment();
        }


    }

}
