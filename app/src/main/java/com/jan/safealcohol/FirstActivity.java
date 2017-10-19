package com.jan.safealcohol;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_NAME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS;
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
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    public static final String MY_PREFS_FILE = "MyPrefsFile";

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

        // OK - new implementation
        Date myDate = new Date();
        String date = dateFormat.format(myDate);
        customDateTime.setText(date);
        // OK End
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
        float units = (float) (amount * 1.4);
        values.put(COLUMN_NAME_UNITS, amount * 1.4);              // TODO To add the units - new DB for drinks

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        float unitsOld = prefs.getFloat("units", (float) 0.0);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        editor.putFloat("units", units + unitsOld);
        editor.apply();

        /**
         * 1.) Ko se drink doda, se level alkohola itak dvigne za vrednost dodatka
         * 2.) Vpise se datum zadnjega izracuna --> IZRACUN: Potreben datum zadnjega izracuna + time difference do trenutnega casa
         *      --> Faktor upadanja levela na minuto
         *  // http://www.izberisam.org/alkopedija/alko-osnove/izracun-alkohola-v-krvi/
         */

        // Custom timestamp
        if(!customDateTimeCheckBox.isChecked()){
            Date myDate = new Date();
            String date = dateFormat.format(myDate);
            values.put(COLUMN_NAME_TIMESTAMP, date);
        } else {
            values.put(COLUMN_NAME_TIMESTAMP, customDateTime.getText().toString());
        }


        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_NAME, null, values);
    }

    // Welcome message and last meal message
    public void updateUserMessages(){

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        String fn = prefs.getString("firstname", null);
        String ln = prefs.getString("lastname", null);

        welcomeMessage.setText("Welcome, " + fn + " " + ln);

        int sizeOfMeal = prefs.getInt("sizeofmeal", 1);

        String mealType = "";
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

        String mt = prefs.getString("timeofmeal", null);
        Date myDate = new Date();
        String date = dateFormat.format(myDate);
        mealtime.setText(date);
        lastMeal.setText("Your last meal was a " + mealType + " on " + mt);

    }

    // Adds last meal to the db
    public void addMealToDB(){

        // Gets the data repository in write mode
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();


        int radioButton = 0;
        if(mealSize1Radio.isChecked()) radioButton = 1;
        else if(mealSize2Radio.isChecked()) radioButton = 2;
        else radioButton = 3;

        editor.putInt("sizeofmeal", radioButton);
        Date myDate = new Date();
        String date = dateFormat.format(myDate);
        editor.putString("timeofmeal", date);
        editor.apply();
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