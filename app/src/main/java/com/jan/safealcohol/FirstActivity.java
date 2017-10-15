package com.jan.safealcohol;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_FIRSTNAME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_LASTNAME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_MEALTIME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_NAME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_SIZEOFMEAL;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.TABLE2_NAME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.TABLE_NAME;
import static com.jan.safealcohol.R.id.spinnerDesign;

public class FirstActivity extends AppCompatActivity implements Serializable {

    private Button secondActivityButton;
    private Context context = this;
    private EditText amount;
    private Button addButton;
    private Spinner spinner;
    FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(context);
    private EditText customDateTime;
    private CheckBox customDateTimeCheckBox;
    private EditText mealtime;
    private Button saveButton;
    private Button userDataButton;
    Cursor cursor;
    private TextView welcomeMessage;
    private RadioButton mealSize1Radio;
    private RadioButton mealSize2Radio;
    private RadioButton mealSize3Radio;
    private TextView lastMeal;

    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstactivitydesign);
        // Create DB if not created
        new FeedReaderDbHelper(context);

        defineVariables();
        callEventListeners();
        createDropdownMenu();
        updateUserMessages();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        updateUserMessages();
    }

    public void defineVariables(){
        secondActivityButton = (Button) findViewById(R.id.secondActivity);
        amount = (EditText) findViewById(R.id.amount);
        addButton = (Button) findViewById(R.id.addButton);
        spinner = (Spinner) findViewById(spinnerDesign);
        customDateTime = (EditText) findViewById(R.id.customDateTime);
        customDateTime.setText(DateFormat.getDateTimeInstance().format(new Date()));
        customDateTimeCheckBox = (CheckBox) findViewById(R.id.checkBox);
        mealtime = (EditText) findViewById(R.id.mealTimeET);
        saveButton = (Button) findViewById(R.id.updateDbButton);
        userDataButton = (Button) findViewById(R.id.userDataButton);
        welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        mealSize1Radio = (RadioButton) findViewById(R.id.mealSize1);
        mealSize1Radio.setChecked(true);
        mealSize2Radio = (RadioButton) findViewById(R.id.mealSize2);
        mealSize3Radio = (RadioButton) findViewById(R.id.mealSize3);
        lastMeal = (TextView) findViewById(R.id.lastMeal);
    }

    /**
     *  [START] Event listeners
     */

    public void callEventListeners(){
        secondActivityButton.setOnClickListener(startSecondActivityListener);
        addButton.setOnClickListener(addDrinkListener);
        saveButton.setOnClickListener(addMealToDBListener);
        userDataButton.setOnClickListener(startUserDataListener);
    }

    // Starts UserDataActivity
    View.OnClickListener startUserDataListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            runUserDataActivity();
        }
    };

    // Adds new meal to DB
    View.OnClickListener addMealToDBListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            addMealToDB();
        }
    };

    // Adds new drink to DB
    View.OnClickListener addDrinkListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v){
            addDrinkToDB();
        }
    };

    // Starts Drink History activity
    View.OnClickListener startSecondActivityListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v){
            runSecondActivity();
        }
    };

    /**
     *  [END] Event listeners
     */

    /**
     * [START] Functions
     */

    public void createDropdownMenu(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.drinks_array, android.R.layout.simple_spinner_item);    // Create an ArrayAdapter using the string array and a default spinner layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);             // Specify the layout to use when the list of choices appears
        spinner.setAdapter(adapter);                // Apply the adapter to the spinner
    }

    public void addDrinkToDB(){

        String amountText = amount.getText().toString();
        if(amountText.equals("")) amountText = "1.0";               // If the field is empty
        float amount = Float.parseFloat(amountText);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NAME, spinner.getSelectedItem().toString());
        values.put(COLUMN_NAME_AMOUNT, amount);
        values.put(COLUMN_NAME_UNITS, amount * 1.4);              // TODO To add the units - new DB for drinks

        // Custom timestamp
        if(!customDateTimeCheckBox.isChecked()){
            values.put(COLUMN_NAME_TIMESTAMP, DateFormat.getDateTimeInstance().format(new Date()));
        } else {
            values.put(COLUMN_NAME_TIMESTAMP, customDateTime.getText().toString());
        }

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_NAME, null, values);
    }

    // Welcome message and last meal message
    public void updateUserMessages(){

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Welcome message
        cursor = db.rawQuery("SELECT * FROM " + TABLE2_NAME, null);
        cursor.moveToNext();

        String fn = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_FIRSTNAME));
        String ln = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LASTNAME));

        welcomeMessage.setText("Welcome, " + fn + " " + ln);

        // Last meal message
        String mealType = "";

        int sizeOfMeal = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_SIZEOFMEAL)));
        switch(sizeOfMeal) {
            case 1:
                mealType = "snack";
                break;
            case 2:
                mealType = "mid-size meal";
                break;
            case 3:
                mealType = "full meal";
                break;
        }

        String mt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_MEALTIME));
        mealtime.setText(customDateTime.getText().toString());
        lastMeal.setText("Your last meal was a " + mealType + " on " + mt);
    }

    // Adds last meal to the db
    public void addMealToDB(){

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int radioButton = 0;
        if(mealSize1Radio.isChecked()) radioButton = 1;
        else if(mealSize2Radio.isChecked()) radioButton = 2;
        else radioButton = 3;

        String query = "UPDATE userDataNew SET " +
                "sizeofmeal = '" + radioButton + "', " +
                "mealtime = '" + mealtime.getText().toString() + "' " +
                "WHERE _id = '1'";

        Log.d("db", "QUERY: " + query);
        db.execSQL(query);
        updateUserMessages();

    }

    /**
     *  [END] Functions
     */

    /**
     *  [START] Other activities
     */

    public void runSecondActivity () {
        Intent intent = new Intent(context, SecondActivity.class);
        context.startActivity(intent);
    }

    // Starts UserDataActivity
    public void runUserDataActivity(){
        Intent intent = new Intent(context, UserDataActivity.class);
        context.startActivity(intent);
    }

    /**
     *  [END] Other activities
     */
}