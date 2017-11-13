package com.jan.safealcohol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class FirstActivity extends AppCompatActivity  {

    private Button secondActivityButton;
    private Context context = this;
    private Button userDataButton;
    private TextView welcomeMessage;
    private Button addMealActivity;


    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    public static final String MY_PREFS_FILE = "MyPrefsFile";
    private TextView textLevel;
    private TextView textDrive;
    private Button updateUnitsButton;
    private HashMap<String, Float> levelMap;
    private DecimalFormat myFormat = new DecimalFormat("0.0");
    private Button addDrinkActivity;
    private TextView lastMeal;

    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstactivitydesign);


        // Create DB if not created
        new FeedReaderDbHelper(context);
        defineVariables();
        levelMap = HashMaps.createLevelMap();
        callEventListeners();
        drawButtons();
        checkIfUserIsRegistered();
        updateUserMessages();
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


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
        drawButtons();
        String toastMessage = intent.getStringExtra("toast");
        if(toastMessage != null && !toastMessage.equals("")) {
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
            intent.putExtra("toast", "");
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
        //userDataButton = (Button) findViewById(R.id.userDataButton);
        welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        textLevel = (TextView) findViewById(R.id.textLevel);
        textDrive = (TextView) findViewById(R.id.textDrive);
        //updateUnitsButton = (Button) findViewById(R.id.updateUnitsButton);
        addDrinkActivity = (Button) findViewById(R.id.addDrinkActivity);
        addMealActivity = (Button) findViewById(R.id.addMealActivity);
        //lastMeal = (TextView) findViewById(R.id.lastMeal);

    }

    // Initialization of GUI at when the activity is started
    public void drawButtons(){
        secondActivityButton.setBackgroundResource(R.drawable.drinking_history_default);
        addDrinkActivity.setBackgroundResource(R.drawable.add_drink_home_default);
        addMealActivity.setBackgroundResource(R.drawable.add_meal_home_default);
    }

    /**
     *  [START] Event listeners
     */

    public void callEventListeners(){
        secondActivityButton.setOnClickListener(startSecondActivityListener);
        //userDataButton.setOnClickListener(startUserDataListener);
        //updateUnitsButton.setOnClickListener(updateUnitsListener);
        addDrinkActivity.setOnClickListener(addDrinkActivityListener);
        addMealActivity.setOnClickListener(addMealActivityListener);
    }

    // Starts UserDataActivity
    View.OnClickListener startUserDataListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            runUserDataActivity();
        }
    };

    // Starts Drink History activity
    View.OnClickListener startSecondActivityListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v){

            secondActivityButton.setBackgroundResource(R.drawable.drinking_history_pressed);
            runSecondActivity();
        }
    };

    View.OnClickListener addDrinkActivityListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){

            addDrinkActivity.setBackgroundResource(R.drawable.add_drink_home_pressed);
            runAddDrinkActivity();
        }
    };

    View.OnClickListener addMealActivityListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){

            addMealActivity.setBackgroundResource(R.drawable.add_meal_home_pressed);
            runAddMealActivity();
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

        Float aboveLimit = 0f;
        textLevel.setText("Current units: " + myFormat.format(currentUnits) + "  |  AlcoLevel: " + myFormat.format(alcoLevel) + "\n");
        String country = prefs.getString("country", null);
        Float limitInCountry = levelMap.get(country)*10;
        Log.d("calc", "Limit in country: " + limitInCountry + "AlcoLevel: " + alcoLevel);
        Log.d("calc", "Calc: " + (alcoLevel-limitInCountry));

        if(limitInCountry != -1){
            //Log.d("limitString", "Alco level: " + alcoLevel);
            //Log.d("limitString", "Level: " + myFormat.format(levelMap.get(prefs.getFloat("country", -1)) * 10));


            aboveLimit = alcoLevel - limitInCountry;
            if(aboveLimit < 0){
                float out = Math.abs(aboveLimit);
                textDrive.setText("You are " + myFormat.format(out) + " below the limit. Be careful!\n");
            } else if (aboveLimit == 0){
                textDrive.setText("You are on limit. Remember, app just calculates an assumption. Wait before you drive");
            } else {
                textDrive.setText("You are " + myFormat.format(aboveLimit) + " above the limit. Don't even think about driving!");
            }

            textDrive.append("Limit in your country: " + myFormat.format(levelMap.get(prefs.getString("country", null)) * 10));
        } else {
            textDrive.setText("Limit for your country is unknown!");
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
        welcomeMessage.setText(fn + " " + ln);
        
        
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

    public void runAddMealActivity(){
        Intent intent = new Intent(context, AddMeal.class);
        context.startActivity(intent);
    }

    /**
     *  [END] Other activities
     */
}