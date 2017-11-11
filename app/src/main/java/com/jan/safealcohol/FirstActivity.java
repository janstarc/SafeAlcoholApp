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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
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
//import static com.jan.safealcohol.R.id.spinnerDesign;

public class FirstActivity extends AppCompatActivity implements Serializable {

    private Button secondActivityButton;
    private Context context = this;

    // TO DELETE
    private EditText amount;
    private EditText percent;
    private Button addButton;
    private Spinner spinner;
    FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(context);
    // TO DELETE
    private EditText mealtime;
    private Button saveButton;
    private Button userDataButton;
    private TextView welcomeMessage;
    private RadioButton mealSize1Radio;
    private RadioButton mealSize2Radio;
    private TextView lastMeal;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    public static final String MY_PREFS_FILE = "MyPrefsFile";
    private TextView unitsTextView;
    private Button updateUnitsButton;
    private HashMap<String, Float> percentMap;
    private HashMap<String, Float> amountMap;
    private HashMap<String, Float> levelMap;
    private DecimalFormat myFormat = new DecimalFormat("0.0");
    private Button addDrinkActivity;

    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstactivitydesign);

        // Create DB if not created
        new FeedReaderDbHelper(context);
        defineVariables();
        percentMap = HashMaps.createPercentageMap();
        amountMap = HashMaps.createAmountMap();
        levelMap = HashMaps.createLevelMap();
        callEventListeners();
        createDropdownMenu();
        checkIfUserIsRegistered();
        updateUserMessages();

        try {
            updateUnits((float) 0.0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        Intent intent = getIntent();
        String toastMessage = intent.getStringExtra("toast");
        if(toastMessage != null && !toastMessage.equals("")) {
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
        }

        updateUserMessages();
        try {
            updateUnits((float) 0.0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void defineVariables(){

        secondActivityButton = (Button) findViewById(R.id.secondActivity);
        //amount = (EditText) findViewById(R.id.amountET);
        //percent = (EditText) findViewById(R.id.percentET);
        //addButton = (Button) findViewById(R.id.addButton);
        //spinner = (Spinner) findViewById(spinnerDesign);
        mealtime = (EditText) findViewById(R.id.mealTimeET);
        saveButton = (Button) findViewById(R.id.updateDbButton);
        userDataButton = (Button) findViewById(R.id.userDataButton);
        welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        mealSize1Radio = (RadioButton) findViewById(R.id.mealSize1);
        mealSize1Radio.setChecked(true);
        mealSize2Radio = (RadioButton) findViewById(R.id.mealSize2);
        lastMeal = (TextView) findViewById(R.id.lastMeal);
        unitsTextView = (TextView) findViewById(R.id.unitsDisplay);
        updateUnitsButton = (Button) findViewById(R.id.updateUnitsButton);
        addDrinkActivity = (Button) findViewById(R.id.addDrinkActivity);
    }

    /**
     *  [START] Event listeners
     */

    public void callEventListeners(){
        secondActivityButton.setOnClickListener(startSecondActivityListener);
        addButton.setOnClickListener(addDrinkListener);
        saveButton.setOnClickListener(addMealToDBListener);
        userDataButton.setOnClickListener(startUserDataListener);
        updateUnitsButton.setOnClickListener(updateUnitsListener);
        addDrinkActivity.setOnClickListener(addDrinkActivityListener);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                fillAmountPercent();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
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
            try {
                addDrinkToDB();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };

    // Starts Drink History activity
    View.OnClickListener startSecondActivityListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v){
            runSecondActivity();
        }
    };

    View.OnClickListener updateUnitsListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            try {
                updateUnits((float) 0.0);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };

    View.OnClickListener addDrinkActivityListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            runAddDrinkActivity();
        }
    };
    /**
     *  [END] Event listeners
     */

    /**
     * [START] Functions
     */

    public void checkIfUserIsRegistered(){
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);


        String fn = prefs.getString("firstname", null);
        String ln = prefs.getString("lastname", null);
        int hg = prefs.getInt("height", 0);
        int wg = prefs.getInt("weight", 0);
        int cs = prefs.getInt("countryId", 0);
        String gd = prefs.getString("gender", null);

        if(fn == null || ln == null || hg == 0 || wg == 0 || cs == 0 || gd == null){
            Intent intent = new Intent(context, UserDataActivity.class);
            intent.putExtra("userMsg", "Please, enter your personal data");
            context.startActivity(intent);

        }
    }

    public void fillAmountPercent(){
        /*
        String item = spinner.getSelectedItem().toString();
        percent.setText(percentMap.get(item).toString());
        amount.setText(amountMap.get(item).toString());
        */
    }

    public void createDropdownMenu(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.drinks_array, android.R.layout.simple_spinner_item);    // Create an ArrayAdapter using the string array and a default spinner layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);             // Specify the layout to use when the list of choices appears
        spinner.setAdapter(adapter);                // Apply the adapter to the spinner
    }

    public void addDrinkToDB() throws ParseException {

        String amountText = amount.getText().toString();
        String percentText = percent.getText().toString();
        if(amountText.equals("") || percentText.equals("")){
            Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_LONG).show();
        } else if (Float.valueOf(amountText) > 10.1f || Float.valueOf(amountText) < 0.1f) {
            Toast.makeText(getApplicationContext(), "Please check the amount again", Toast.LENGTH_LONG).show();
        } else if (Float.valueOf(percentText) > 100 || Float.valueOf(percentText) < 0){
            Toast.makeText(getApplicationContext(), "Please check the percentage again", Toast.LENGTH_LONG).show();
        } else {

            float amount = Float.valueOf(amountText);

            // Gets the data repository in write mode
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            String selectedDrink = spinner.getSelectedItem().toString();
            float percent = percentMap.get(selectedDrink);
            if(Float.valueOf(percentText) != percent) percent = Float.valueOf(percentText);
            Float newDrinkUnits = (amount*(percent)/100) / 0.125f;
            Log.d("drinkCalc", "newDrinkUnits: " + newDrinkUnits);

            // Create a new map of values, where column names are the keys
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
        }
    }

    /**
     * 1.) Ko se drink doda, se level alkohola itak dvigne za vrednost dodatka
     * 2.) Vpise se datum zadnjega izracuna --> IZRACUN: Potreben datum zadnjega izracuna + time difference do trenutnega casa
     *      --> Faktor upadanja levela na minuto
     *  // http://www.izberisam.org/alkopedija/alko-osnove/izracun-alkohola-v-krvi/
     *
     *  Alcohol level reduces for 1 unit/hour (male) and 0.5 unit/hour (female)
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
        if(timeDifferenceMin > 0 || newDrinkUnits != 0.0) {                 

            float unitsMinus;
            if(prefs.getString("gender", null).equals("M")) unitsMinus = (float) (timeDifferenceMin * 0.0167);
            else unitsMinus = (float) (timeDifferenceMin * (0.0167/2));

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
        unitsTextView.setText("Current units: " + myFormat.format(currentUnits) + "  |  AlcoLevel: " + myFormat.format(alcoLevel) + "\n");
        String limitInCountry = prefs.getString("country", null);

        if(limitInCountry != null){
            Log.d("limitString", "Here1");
            Log.d("limitString", "Level: " + myFormat.format(levelMap.get(prefs.getString("country", null)) * 10));
            unitsTextView.append("Limit in your country: " + myFormat.format(levelMap.get(prefs.getString("country", null)) * 10));
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

    public void calculateAlcoLevel (float units){

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        int weight = prefs.getInt("weight", 50);
        String gender = prefs.getString("gender", "M");
        float r = 0.7f;
        if(gender.equals("F")) r = 0.6f;
        float alcoLevel = (units*10) / (weight * r);

        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        editor.putFloat("alcoLevel", alcoLevel);
        editor.apply();
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

    public long calculateTimeDifference(Date date1, Date date2){

        Log.d("timeTag", "HERE!!!!!");
        long second = 1000l;
        long minute = 60l * second;
        long hour = 60l * minute;

        // calculation
        long diff = date2.getTime() - date1.getTime();

        // printing output
        Log.d("timeTag", String.format("%02d", diff / hour) + " hours, ");
        Log.d("timeTag", String.format("%02d", (diff % hour) / minute) + " minutes, ");
        Log.d("timeTag", String.format("%02d", (diff % minute) / second) + " seconds");
        long hoursOut = diff/hour;
        long minOut = (diff % hour) / minute;

        return Math.abs(hoursOut*60+minOut);
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
        intent.putExtra("userMsg", "Edit user data");
        context.startActivity(intent);
    }

    public void runAddDrinkActivity(){
        Intent intent = new Intent(context, AddDrink.class);
        context.startActivity(intent);
    }

    /**
     *  [END] Other activities
     */
}