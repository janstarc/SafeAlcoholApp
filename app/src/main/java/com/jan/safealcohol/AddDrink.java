package com.jan.safealcohol;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_NAME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_PERCENTAGE;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.TABLE_NAME;

public class AddDrink extends AppCompatActivity implements View.OnClickListener {


    private Context context = this;
    private EditText amount;
    private EditText percent;
    private Button addButton;
    FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(context);
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    public static final String MY_PREFS_FILE = "MyPrefsFile";
    private HashMap<String, Float> percentMap;
    private HashMap<String, Float> amountMap;
    private HashMap<String, Float> levelMap;
    private DecimalFormat myFormat = new DecimalFormat("0.0");
    private Button radlerButton;
    private Button beerButton;
    private Button liquorButton;
    private Button wineButton;
    private Button distilledButton;
    private Button customButton;
    private Button selectedButton;
    private HashMap<String, String> buttonDefault;
    private HashMap<String, String> buttonPressed;
    private HashMap<String, String> DBNamesMap;
    private AssetManager am;
    private Typeface typeface;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_drink_design);

        // Create DB if not created
        new FeedReaderDbHelper(context);
        defineVariables();
        percentMap = HashMaps.createPercentageMap();
        amountMap = HashMaps.createAmountMap();
        levelMap = HashMaps.createLevelMap();
        buttonDefault = HashMaps.createButtonDefaultMap();
        buttonPressed = HashMaps.createButtonPressedMap();
        DBNamesMap = HashMaps.createDBNamesMap();
        am = context.getApplicationContext().getAssets();
        typeface = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "roboto_light.ttf"));
        callEventListeners();
        drawButtons();
        selectedButton = beerButton;                // selectedButton has global reach --> Accessible from functions
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        /*
        try {
            //updateUnits((float) 0.0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        */
    }

    // Overrides "extends" class method!
    @Override
    public void onClick(View v) {

        Log.d("focusButtons", "HERE123");

        switch (v.getId()){
            case R.id.radlerButton:
                selectedButton = setFocus(radlerButton);
                break;

            case R.id.beerButton :
                selectedButton = setFocus(beerButton);
                break;

            case R.id.liquorButton :
                selectedButton = setFocus(liquorButton);
                break;

            case R.id.wineButton :
                selectedButton = setFocus(wineButton);
                break;

            case R.id.distilledButton :
                selectedButton = setFocus(distilledButton);
                break;

            case R.id.customButton :
                prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
                selectedButton = setFocus(customButton);
                break;
        }

        fillAmountPercent(selectedButton);
    }

    // Radio button logic on the entire AddDrink screen
    private Button setFocus(Button focus){

        drawButtons();
        String pressedKey = getResources().getResourceEntryName(focus.getId());
        String pressedFile = buttonPressed.get(pressedKey);
        Log.d("focusButtons", "Pressed file name: " + pressedFile);
        int idFocus = context.getResources().getIdentifier(pressedFile, "drawable", context.getPackageName());
        focus.setBackgroundResource(idFocus);

        return focus;
    }

    // Initialization of GUI at when the activity is started
    public void drawButtons(){
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        radlerButton.setBackgroundResource(R.drawable.radler_default);
        beerButton.setBackgroundResource(R.drawable.beer_default);
        liquorButton.setBackgroundResource(R.drawable.liquor_default);
        wineButton.setBackgroundResource(R.drawable.wine_default);
        distilledButton.setBackgroundResource(R.drawable.distilled_default);
        addButton.setBackgroundResource(R.drawable.add_default);
        customButton.setBackgroundResource(R.drawable.custom_default);
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        customButton.setText(prefs.getString("customDrinkName", "Custom Drink"));
        customButton.setTypeface(typeface);
    }

    // Define all variables
    public void defineVariables() {
        amount = (EditText) findViewById(R.id.amountET2);
        percent = (EditText) findViewById(R.id.percentET2);
        addButton = (Button) findViewById(R.id.addButton2);
        radlerButton = (Button) findViewById(R.id.radlerButton);
        beerButton = (Button) findViewById(R.id.beerButton);
        liquorButton = (Button) findViewById(R.id.liquorButton);
        wineButton = (Button) findViewById(R.id.wineButton);
        distilledButton = (Button) findViewById(R.id.distilledButton);
        customButton = (Button) findViewById(R.id.customButton);
    }

    /**
     * [START] Event listeners
     */

    public void callEventListeners() {
        addButton.setOnClickListener(addDrinkListener);
        radlerButton.setOnClickListener(this);
        beerButton.setOnClickListener(this);
        liquorButton.setOnClickListener(this);
        wineButton.setOnClickListener(this);
        distilledButton.setOnClickListener(this);
        customButton.setOnClickListener(this);
        customButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                setCustomDrinkName();
                return true;
            }
        });
        percent.setOnFocusChangeListener(etFocusListenerPercent);
        amount.setOnFocusChangeListener(etFocusListenerAmount);
    }

    // Adds new drink to DB
    View.OnClickListener addDrinkListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            try {
                addDrinkToDB();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };

    View.OnFocusChangeListener etFocusListenerPercent = new EditText.OnFocusChangeListener(){

        @Override
        public void onFocusChange(View v, boolean hasFocus){
            percent.setBackgroundResource(R.drawable.input_active);
            amount.setBackgroundResource(R.drawable.input);
        }
    };

    View.OnFocusChangeListener etFocusListenerAmount = new EditText.OnFocusChangeListener(){

        @Override
        public void onFocusChange(View v, boolean hasFocus){
            amount.setBackgroundResource(R.drawable.input_active);
            percent.setBackgroundResource(R.drawable.input);
        }
    };

    /**
     *  [END] Event listeners
     */

    /**
     * [START] Functions
     */

    public void setCustomDrinkName(){

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        final EditText et = new EditText(context);

        et.setInputType(8192);                                                 // Capitalize the first character of every word
        et.setText(prefs.getString("customDrinkName", ""));      // Enter the custom drink name
        et.setSelection(et.getText().length());                                // Put cursor to the end

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(et);

        // set dialog message
        alertDialogBuilder.setCancelable(true).setPositiveButton("Add custom drink name", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d("popup", "Here? --> ETtext: " + et.getText().toString());
                String drinkName = et.getText().toString();
                if(drinkName.length() <= 10 && drinkName.length() > 1){
                    editor.putString("customDrinkName", et.getText().toString());
                    editor.apply();
                    customButton.setText(et.getText().toString());
                    customButton.setTypeface(typeface);
                } else if(drinkName.length() > 10) {
                    Toast.makeText(getApplicationContext(), "Drink name shouldn't be longer than 10 letters", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Drink name should be at least 2 letters long", Toast.LENGTH_LONG).show();
                }
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    // Fill textbox
    public void fillAmountPercent(Button selectedButton) {

        String key = selectedButton.getResources().getResourceEntryName(selectedButton.getId());
        String percentText = percentMap.get(key).toString();
        String amountText = amountMap.get(key).toString();
        if(percentText.equals("0.0")) percentText = "";
        if(amountText.equals("0.0")) amountText = "";
        percent.setText(percentText);
        amount.setText(amountText);
        Log.d("selectedButton", "Selected button: " + key);

        if(key.equals("customButton")) {
            int customDrinkPercent = prefs.getInt("customDrinkPercent", 0);
            float customDrinkAmount = prefs.getFloat("customDrinkAmount", 0f);
            Log.d("textFields", "Custom drink amount: " + customDrinkAmount + " Cpp: " + customDrinkPercent);
            if (customDrinkAmount != 0) {
                Log.d("textFields", "Here?");
                amount.setText(Float.toString(customDrinkAmount));
                percent.setText(Integer.toString(customDrinkPercent));
            }
        }
    }

    // Adding selected drink to the DB
    public void addDrinkToDB() throws ParseException {

        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        String amountText = amount.getText().toString();
        String percentText = percent.getText().toString();
        if (amountText.equals("") || percentText.equals("")) {
            Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_LONG).show();
        } else if (Float.valueOf(amountText) > 10.1f || Float.valueOf(amountText) < 0.1f) {
            Toast.makeText(getApplicationContext(), "Please check the amount again", Toast.LENGTH_LONG).show();
        } else if (Float.valueOf(percentText) > 100 || Float.valueOf(percentText) < 0) {
            Toast.makeText(getApplicationContext(), "Please check the percentage again", Toast.LENGTH_LONG).show();
        } else {

            // Gets the data repository in write mode
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            String drinkKey = getResources().getResourceEntryName(selectedButton.getId());

            Log.d("custom", "Drink Key: " + drinkKey);
            if(drinkKey.equals("customButton")){
                    Log.d("custom", "Custom button here?");
                    editor.putInt("customDrinkPercent", Integer.parseInt(percentText));
                    editor.putFloat("customDrinkAmount", Float.valueOf(amountText));
                    editor.apply();
            }

            String selectedDrink;
            Log.d("DrinkKey", "DrinkKey: " + drinkKey);
            if(!drinkKey.equals("customButton")){
                selectedDrink = DBNamesMap.get(drinkKey);
            } else {
                selectedDrink = customButton.getText().toString();
            }

            float amount  = Float.valueOf(amountText);
            float percent = Float.valueOf(percentText);
            float newDrinkUnits = ((amount * percent) / 100) / 0.125f;
            Log.d("drinkCalc", "newDrinkUnits: " + newDrinkUnits);
            Log.d("selectedDrink", "Selected: " + selectedDrink);

            // Put new info to values --> Values get put in the DB
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_NAME, selectedDrink);
            values.put(COLUMN_NAME_AMOUNT, amount);
            values.put(COLUMN_NAME_UNITS, newDrinkUnits);
            values.put(COLUMN_NAME_PERCENTAGE, percent);

            // Custom timestamp
            Date myDate = new Date();
            String date = dateFormat.format(myDate);
            values.put(COLUMN_NAME_TIMESTAMP, date);
            Log.d("unitsTest", "ADDED - New drinks units: " + newDrinkUnits);
            editor.putFloat("newDrinkUnits", newDrinkUnits);
            editor.putString("newDrinkTimestamp", date);
            editor.apply();

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(TABLE_NAME, null, values);

            runFirstActivity();
        }
    }

    public void runFirstActivity(){
        Intent intent = new Intent(context, FirstActivity.class);
        intent.putExtra("toast", "New drink added successfully");
        context.startActivity(intent);
    }
}

    /**
     *  [END] Functions
     */