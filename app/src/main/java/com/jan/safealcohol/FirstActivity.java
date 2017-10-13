package com.jan.safealcohol;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.jan.safealcohol.R.id.custom;
import static com.jan.safealcohol.R.id.spinnerDesign;


public class FirstActivity extends AppCompatActivity implements Serializable {


    private Button secondActivityButton;
    private Context context = this;
    private EditText description;
    private Button addButton;
    private ArrayList<String> itemsList = new ArrayList<>();                // Used to transfer data between activities
    private boolean newItemsAdded = false;
    private Spinner spinner;
    FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(context);
    private Button readDBTest;
    private Cursor cursor;
    List itemIds = new ArrayList<>();
    List name = new ArrayList<>();
    List amount = new ArrayList<>();
    List units = new ArrayList<>();
    List timestamp = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstactivitydesign);


        // Defining the variables
        secondActivityButton = (Button) findViewById(R.id.secondActivity);
        description = (EditText) findViewById(R.id.description);
        addButton = (Button) findViewById(R.id.addButton);

        // Calling event listeners
        secondActivityButton.setOnClickListener(startSecondActivity);
        secondActivityButton.setOnLongClickListener(startSecondActivityLong);
        addButton.setOnClickListener(addNewElement);


        spinner = (Spinner) findViewById(spinnerDesign);
        // Creat2e an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.drinks_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Create DB
        final FeedReaderDbHelper feedReaderDbHelper = new FeedReaderDbHelper(context);

        readDBTest = (Button) findViewById(R.id.readDB);
        readDBTest.setOnClickListener(readDatabase);
        Log.d("debug", "-----> Cursor: " + cursor);
    }

    View.OnClickListener readDatabase = new Button.OnClickListener() {


        @Override
        public void onClick(View v){

            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            Log.d("debug", "DB:" + db);

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    FeedReaderContract.FeedEntry._ID,
                    FeedReaderContract.FeedEntry.COLUMN_NAME_NAME,
                    FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT,
                    FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS,
                    FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP
            };

            // Filter results WHERE "title" = 'My Title'
            //String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " = ";
            //String[] selectionArgs = { "My Title" };

            // How you want the results sorted in the resulting Cursor
            String sortOrder =
                    FeedReaderContract.FeedEntry._ID + " ASC";

            cursor = db.query(
                    FeedReaderContract.FeedEntry.TABLE_NAME,        // The table to query
                    projection,                                     // The columns to return
                    null,                                           // The columns for the WHERE clause
                    null,                                           // The values for the WHERE clause
                    null,                                           // don't group the rows
                    null,                                           // don't filter by row groups
                    sortOrder                                       // The sort order
            );


            while(cursor.moveToNext()) {
                long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID));
                itemIds.add(itemId);

                String nameField = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_NAME));
                name.add(nameField);

                int amountField = cursor.getInt(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT));
                amount.add(amountField);

                int unitField = cursor.getInt(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS));
                units.add(unitField);

                String timestampField = cursor.getString(
                        cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP));
                timestamp.add(timestampField);

            }
            cursor.close();


            Log.d("debug", "Item:   | Name:  | Amount: | Units: | Timestamp: ");
            for(int i = 0; i < itemIds.size(); i++){
                Log.d("debug", "Item: " + itemIds.get(i) + " | " + name.get(i) + " | " + amount.get(i) + " | " + units.get(i) + " | " + timestamp.get(i));
            }
        }
    };

    // Adds new entry to the itemsList
    View.OnClickListener addNewElement = new Button.OnClickListener() {

        @Override
        public void onClick(View v){

            itemsList.add(spinner.getSelectedItem().toString());
            String amount = description.getText().toString();
            if(amount.equals("")) amount = "1";
            itemsList.add(amount);
            newItemsAdded = true;
            //Log.d("debug", "Title:")
            Log.d("debug", "-----> HERE!!!!");


            // TODO Test - put info into the DB

            // Gets the data repository in write mode
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            Log.d("debug", "DB: " + db);

            // Create a new map of values, where column names are the keys
            ContentValues values = new ContentValues();
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_NAME, spinner.getSelectedItem().toString());
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT, Float.parseFloat(amount));
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS, Float.parseFloat(amount)*1.4);
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP, DateFormat.getDateTimeInstance().format(new Date()));

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);

            // TODO Test END

        }
    };

    View.OnClickListener startSecondActivity = new Button.OnClickListener() {

        @Override
        public void onClick(View v){
            if(newItemsAdded){                  // Send the array list with added items to the second activity
                runSecondActivity(true, newItemsAdded, itemsList);

            } else {                            // Start normally
                runSecondActivity(true, newItemsAdded, null);
            }

        }
    };

    View.OnLongClickListener startSecondActivityLong = new Button.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v){
            runSecondActivity(false, false, null);
            return true;
        }
    };

    public void runSecondActivity (boolean b, boolean itemsAdded, ArrayList<String> itemsList) {

        Intent intent = new Intent(context, SecondActivity.class);
        intent.putExtra("flag", b);
        intent.putExtra("itemsAdded", itemsAdded);
        intent.putExtra("newItems", itemsList);

        context.startActivity(intent);
    }


}
