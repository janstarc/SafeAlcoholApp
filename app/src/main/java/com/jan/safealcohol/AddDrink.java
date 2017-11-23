package com.jan.safealcohol;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_NAME;
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
        callEventListeners();
        drawButtons();
        //showCustomDrinkName();

        try {
            updateUnits((float) 0.0);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        selectedButton = beerButton;                // selectedButton has global reach --> Accessible from functions
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        try {
            updateUnits((float) 0.0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        radlerButton.setBackgroundResource(R.drawable.radler_default);
        beerButton.setBackgroundResource(R.drawable.beer_default);
        liquorButton.setBackgroundResource(R.drawable.liquor_default);
        wineButton.setBackgroundResource(R.drawable.wine_default);
        distilledButton.setBackgroundResource(R.drawable.distilled_default);
        customButton.setBackgroundResource(R.drawable.custom_default);
        addButton.setBackgroundResource(R.drawable.add_default);
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
                    editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
                    editor.putInt("customDrinkPercent", Integer.parseInt(percentText));
                    editor.putFloat("customDrinkAmount", Float.valueOf(amountText));
                    editor.apply();
            }


            String selectedDrink = DBNamesMap.get(drinkKey);

            float amount  = Float.valueOf(amountText);
            float percent = Float.valueOf(percentText);
            Float newDrinkUnits = (amount * (percent) / 100) / 0.125f;
            Log.d("drinkCalc", "newDrinkUnits: " + newDrinkUnits);
            Log.d("selectedDrink", "Selected: " + selectedDrink);

            // Put new info to values --> Values get put in the DB
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_NAME, selectedDrink);
            values.put(COLUMN_NAME_AMOUNT, amount);
            values.put(COLUMN_NAME_UNITS, newDrinkUnits);                        // TODO To add the units - new DB for drinks
            updateUnits(newDrinkUnits);                                         // Recalculate the units, put on screen, update SharedPref file!

            // Custom timestamp
            Date myDate = new Date();
            String date = dateFormat.format(myDate);
            values.put(COLUMN_NAME_TIMESTAMP, date);

            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(TABLE_NAME, null, values);

            runFirstActivity();
        }
    }

    /**
     * 1.) Ko se drink doda, se level alkohola itak dvigne za vrednost dodatka
     * 2.) Vpise se datum zadnjega izracuna --> IZRACUN: Potreben datum zadnjega izracuna + time difference do trenutnega casa
     * --> Faktor upadanja levela na minuto
     * // http://www.izberisam.org/alkopedija/alko-osnove/izracun-alkohola-v-krvi/
     * <p>
     * Alcohol level reduces for 1 unit/hour (male) and 0.5 unit/hour (female)
     */

    public void updateUnits(float newDrinkUnits) throws ParseException {

        // Get current date
        Date currentTimestamp = new Date();

        // Get the unitsOld and unitsTimestamp from the SharedPref
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        float unitsOld = prefs.getFloat("units", (float) 0.0);                                      // Gets info from the SharedPref
        String unitsTimestampString = prefs.getString("unitsTimestamp", dateFormat.format(currentTimestamp));        // Gets the timestamp from the DB

        // Convert unistTimestamp from SharedPref to Date + Calculate timeDiff
        Date unitsTimestamp = dateFormat.parse(unitsTimestampString);
        long timeDifferenceMin = calculateTimeDifference(currentTimestamp, unitsTimestamp);

        // To prevent changing timeStamp, without changing units. | newDrinkUnits == 0.0 --> Only update for onCreate and onUpdate, nothing new added!
        if (timeDifferenceMin > 0 || newDrinkUnits != 0.0) {

            float unitsMinus;
            if (prefs.getString("gender", null).equals("M"))
                unitsMinus = (float) (timeDifferenceMin * 0.0167);
            else unitsMinus = (float) (timeDifferenceMin * (0.0167 / 2));

            float unitsNew = (newDrinkUnits + unitsOld - unitsMinus);               // units --> Current drink | unitsOld --> Prev units from SharedPref | unitsMinus --> timeDiff * decreaseOnMin
            if (unitsNew < 0) unitsNew = 0;                                         // To avoid neg. units --> You can be max sober
            calculateAlcoLevel(unitsNew);
            editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
            editor.putFloat("units", unitsNew);                                     // Put new info to the SharedPref
            String newTimestamp = dateFormat.format(currentTimestamp);              // Update last calculation value for units in
            editor.putString("unitsTimestamp", newTimestamp);                       // shared pref file!
            editor.apply();
        }

        Float currentUnits = prefs.getFloat("units", (float) 0.0);
        Float alcoLevel = prefs.getFloat("alcoLevel", (float) 0.0);
        //unitsTextView.setText("Current units: " + myFormat.format(currentUnits) + "  |  AlcoLevel: " + myFormat.format(alcoLevel) + "\n");
        String limitInCountry = prefs.getString("country", null);

        if (limitInCountry != null) {
            Log.d("limitString", "Here1");
            Log.d("limitString", "Level: " + myFormat.format(levelMap.get(prefs.getString("country", null)) * 10));
            //unitsTextView.append("Limit in your country: " + myFormat.format(levelMap.get(prefs.getString("country", null)) * 10));
        }
    }

    /*
    c = m / (TT x r)

    Pri tem je:
        c = koncentracija alkohola v krvi (g etanola na kg krvi ali promili)
        m = masa popitega čistega alkohola izražena v gramih
        TT = telesna masa izražena v kilogramih
        r = porazdelitveni faktor (za moške 0,7 in za ženske 0,6)
     */

    public void calculateAlcoLevel(float units) {

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        int weight = prefs.getInt("weight", 50);
        String gender = prefs.getString("gender", "M");
        float r = 0.7f;
        if (gender.equals("F")) r = 0.6f;
        float alcoLevel = (units * 10) / (weight * r);

        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        editor.putFloat("alcoLevel", alcoLevel);
        editor.apply();
    }

    public long calculateTimeDifference(Date date1, Date date2) {

        long second = 1000l;
        long minute = 60l * second;
        long hour = 60l * minute;

        // calculation
        long diff = date2.getTime() - date1.getTime();

        // printing output
        Log.d("timeTag", String.format("%02d", diff / hour) + " hours, ");
        Log.d("timeTag", String.format("%02d", (diff % hour) / minute) + " minutes, ");
        Log.d("timeTag", String.format("%02d", (diff % minute) / second) + " seconds");
        long hoursOut = diff / hour;
        long minOut = (diff % hour) / minute;

        return Math.abs(hoursOut * 60 + minOut);
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