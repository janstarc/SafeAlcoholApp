package com.jan.safealcohol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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
    private TextView textJoke;
    private HashMap<String, Float> levelMap;
    private DecimalFormat myFormat = new DecimalFormat("0.00");
    private Button addDrinkActivity;
    private Timer timer;
    private TimerTask timerTask;
    private ImageView drunkImage;

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
        startTimer();

        try {
            updateUnits((float) 0.0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        Log.d("unitsToDelete", "On resume called!");
        Intent intent = getIntent();
        drawButtons();
        String toastMessage = intent.getStringExtra("toast");

        if(toastMessage != null && !toastMessage.equals("")) {
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
            intent.putExtra("toast", "");
        }

        updateUserMessages();
        try {

            Log.d("unitsToDelete", "NDTimestamp: " + prefs.getString("newDrinkTimestamp", null));

            if(prefs.getString("newDrinkTimestamp", null) != null){     // If new drink was added
                float newUnits = prefs.getFloat("newDrinkUnits", 0f);
                Log.d("unitsToDelete", "Units from FirstActivity --> " + newUnits);
                if(newUnits > 0){
                    Log.d("unitsToDelete", "Adding new units");
                    updateUnits(newUnits);
                    editor.putString("newDrinkTimestamp", null);
                    editor.putFloat("newDrinkUnits", 0f);
                    editor.apply();
                } else {            // Obviously, drink was deleted
                    Log.d("unitsToDelete", "Deleting units --> Units to delete: " + newUnits);
                    reduceUnits(newUnits);
                    editor.putString("newDrinkTimestamp", null);
                    editor.putFloat("newDrinkUnits", 0f);
                    editor.apply();
                }

            } else {
                updateUnits(0f);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void defineVariables(){

        secondActivityButton = (Button) findViewById(R.id.secondActivity);
        userDataButton = (Button) findViewById(R.id.userDataButton);
        welcomeMessage = (TextView) findViewById(R.id.welcomeMessage);
        textLevel = (TextView) findViewById(R.id.textLevel);
        textDrive = (TextView) findViewById(R.id.textDrive);
        textJoke = (TextView) findViewById(R.id.textJoke);
        addDrinkActivity = (Button) findViewById(R.id.addDrinkActivity);
        addMealActivity = (Button) findViewById(R.id.addMealActivity);
        drunkImage = (ImageView) findViewById(R.id.drunkImage);
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
    }

    // Initialization of GUI at when the activity is started
    public void drawButtons(){

        secondActivityButton.setBackgroundResource(R.drawable.drinking_history_default);
        addDrinkActivity.setBackgroundResource(R.drawable.add_drink_home_default);
        addMealActivity.setBackgroundResource(R.drawable.add_meal_home_default);
        userDataButton.setBackgroundResource(R.drawable.profile_edit);
    }

    // Function which updates units every minute
    public void startTimer(){

            timer = new Timer();

            // Timer task to be executed
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {                  // To avoid changing view by other thread --> Crash!
                        @Override
                        public void run() {
                            Log.d("timer", "TimerTask executed");
                            try {                                   // Recalculate & display things
                                updateUnits(0.0f);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };

            // Schedule timer to execute TimerTask every minute
            timer.schedule(timerTask, 60000, 60000);
    }

    /**
     *  [START] Event listeners
     */

    public void callEventListeners(){
        secondActivityButton.setOnClickListener(startSecondActivityListener);
        userDataButton.setOnClickListener(startUserDataListener);
        addDrinkActivity.setOnClickListener(addDrinkActivityListener);
        addMealActivity.setOnClickListener(addMealActivityListener);
    }

    // Starts UserDataActivity
    View.OnClickListener startUserDataListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){

            userDataButton.setBackgroundResource(R.drawable.profile_edit_pressed);
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

        // Get current date and data needed from SharedPref
        Date currentTimestamp = new Date();
        String gender = prefs.getString("gender", null);
        float unitsOld = prefs.getFloat("units", 0f);
        String unitsTimestampString = prefs.getString("unitsTimestamp", dateFormat.format(currentTimestamp));

        // Get TimeDiff
        Date unitsTimestamp = dateFormat.parse(unitsTimestampString);                           // Timestamp of the last units calculation
        long timeDifferenceMin = calculateTimeDifference(currentTimestamp, unitsTimestamp);

        // If there are was meal added and alco level needs to be reduced
        float mealReductionUnits = calcMealReductionUnits();
        if(mealReductionUnits != 0){

            float unitsNew = unitsOld - mealReductionUnits;       // Avoid units < 0
            if(unitsNew > 0){
                editor.putFloat("units", unitsNew);
            } else {
                editor.putFloat("units", 0f);
            }
            editor.apply();
        }

        if(prefs.getBoolean("listEmpty", false)){
            Log.d("listEmpty", "List empty?");
            editor.putFloat("units", 0);
            String newTimestamp = dateFormat.format(currentTimestamp);              // Update last calculation value for units in
            editor.putString("unitsTimestamp", newTimestamp);                       // shared pref file!
            calculateAlcoLevel(0);
            editor.putBoolean("listEmpty", false);

            editor.apply();
        }

        // To prevent changing timeStamp, without changing units. | newDrinkUnits == 0.0 --> Only update for onCreate and onUpdate, nothing new added! 
        if(timeDifferenceMin > 0 || newDrinkUnits != 0.0) {                 

            float unitsMinus;
            if(gender.equals("M")){                                                 // Alcohol level reduces for 1 unit/hour (male) and 0.5 unit/hour (female)
                unitsMinus = (float) (timeDifferenceMin * 0.0167);
            } else {
                unitsMinus = (float) (timeDifferenceMin * (0.0167/2));
            }

            unitsOld = prefs.getFloat("units", 0f);
            float unitsNew = (newDrinkUnits + unitsOld - unitsMinus - mealReductionUnits);               // units --> Current drink | unitsOld --> Prev units from SharedPref | unitsMinus --> timeDiff * decreaseOnMin
            if (unitsNew < 0) unitsNew = 0;                                         // To avoid neg. units --> You can be max sober
            calculateAlcoLevel(unitsNew);                                           // AlcoLevel depends on MORE FACTORS! It is not directly dividable from units!
            editor.putFloat("units", unitsNew);                                     // Put new info to the SharedPref
            String newTimestamp = dateFormat.format(currentTimestamp);              // Update last calculation value for units in
            editor.putString("unitsTimestamp", newTimestamp);                       // shared pref file!
            editor.apply();
        }

        // Fill all three textboxes
        fillTextLevel();
        fillTextDrive();
        fillTextJokeSetPicture();
    }

    public float calcMealReductionUnits(){

        // Get relevant info from the SharedPref
        boolean snackCalculated = prefs.getBoolean("snackCalculated", false);
        boolean midsizeCalculated = prefs.getBoolean("midsizeMealCalculated", false);
        boolean fullmealCalculated = prefs.getBoolean("fullMealCalculated", false);
        String snackTime = prefs.getString("snackTimestamp", null);
        String midsizeTime = prefs.getString("midsizeMealTimestamp", null);
        String fullTime = prefs.getString("fullMealTimestamp", null);

        // If it wasn't yet calculated AND it was added to the SharedPref
        if(!snackCalculated && snackTime != null){

            editor.putBoolean("snackCalculated", true);
            editor.apply();
            return 0.1f;

        } else if (!midsizeCalculated && midsizeTime != null){

            editor.putBoolean("midsizeMealCalculated", true);
            editor.apply();
            return 0.3f;

        } else if (!fullmealCalculated && fullTime != null){

            editor.putBoolean("fullMealCalculated", true);
            editor.apply();
            return 0.7f;
        }

        return 0f;
    }

    public void reduceUnits(float unitsReduction){

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        float currentUnits = prefs.getFloat("units", 0f);
        float newUnits = currentUnits + unitsReduction;                 // MUST be +, cause argument is always negative!
        if(newUnits < 0) newUnits = 0f;
        editor.putFloat("units", newUnits);
        editor.apply();
        calculateAlcoLevel(newUnits);

        // Fill all three textboxes
        fillTextLevel();
        fillTextDrive();
        fillTextJokeSetPicture();
    }


    public void fillTextJokeSetPicture(){

        Float alcoLevel = prefs.getFloat("alcoLevel", 0f);
        String textOut;
        Log.d("Level: ", "Level: " + alcoLevel);

        if(alcoLevel == 0){
            textOut = "<font color=#273F4C><big>Phase: </font>" +
                    "<font color=#4684C4><b>Sober </b></big></font><br>" +
                    "<font color=#273F4C>You are expected to do (mostly) rational things,<br>" +
                    " wake up without hangover and save a lot of money." +
                    " Keep on the good work!</font>";
            drunkImage.setImageResource(R.drawable.face1);

        } else if (alcoLevel < 0.35) {          // 0.35
            textOut = "<font color=#273F4C><big>Phase: </font>" +
                    "<font color=#4684C4><b>The pre-tipsy phase </b></big></font><br>" +
                    "<font color=#273F4C>Depends on how much you ate and how you feel, you " +
                    " might notice a very mild effect of alcohol. " +
                    " Your reaction time isn't noticeably affected.</font>";
            drunkImage.setImageResource(R.drawable.face2);

        } else if (alcoLevel < 0.55) {       // 0.55
            textOut = "<font color=#273F4C><big>Phase: </font>" +
                    "<font color=#4684C4><b>The tipsy phase </b></big></font><br>" +
                    "<font color=#273F4C>Your self-confidence is boosted, you are more talkative " +
                    "and daring. A little bit more, and <br>stupid decisions, here we come!</font>";
            drunkImage.setImageResource(R.drawable.face3);

        } else if (alcoLevel < 1) {         // 1
            textOut = "<font color=#273F4C><big>Phase: </font>" +
                    "<font color=#C49549><b>The slurring phase </b></big></font><br>" +
                    "<font color=#273F4C>Your are experiencing a wave of alcohol. " +
                    "Everything sounds and feels a little bit funny, because your speech is slurry and your motor skills are impaired.</font>";
            drunkImage.setImageResource(R.drawable.face4);

        } else if (alcoLevel < 1.5) {       // 1.5
            textOut = "<font color=#273F4C><big>Phase: </font>" +
                    "<font color=#C47549><b>The blurring phase </b></big></font><br>" +
                    "<font color=#273F4C>Everything is suspiciously blurry. " +
                    "Everyone in the entire freaking bar is suddenly beautiful. Good " +
                    "luck with your wonderful drunken pick up lines!</font>";
            drunkImage.setImageResource(R.drawable.face5);

        } else if (alcoLevel < 2){          // 2
            textOut = "<font color=#273F4C><big>Phase: </font>" +
                    "<font color=#C46949><b>The toppling over phase </b></big></font><br>" +
                    "<font color=#273F4C>Now you’re starting to lean on tables, walls, chairs, even unsuspecting people. " +
                    "Just. Stop. Drinking. Nothing good is going to happen from this point on.</font>";
            drunkImage.setImageResource(R.drawable.face6);

        } else {
            textOut = "<font color=#273F4C><big>Phase: </font>" +
                    "<font color=#C64747><b>The dead phase </b></big></font><br>" +
                    "<font color=#273F4C>Things just got real. By now you’re probably lying down somewhere in a drunken stupor (hopefully your bed).  " +
                    "Sorry, party is over for tonight.</font>";
            drunkImage.setImageResource(R.drawable.face7);
        }
        textJoke.setText(Html.fromHtml(textOut));
    }

    public void fillTextDrive(){

        Float alcoLevel = prefs.getFloat("alcoLevel", 0f);
        String country = prefs.getString("country", null);
        Float aboveLimit;
        String textOut = "";

        if(country != null) {
            Float limitInCountry = levelMap.get(country) * 10;

            if (limitInCountry != -1) {
                aboveLimit = alcoLevel - limitInCountry;

                if(alcoLevel == 0){
                    textOut = "<font color=#273F4C><big>You are </font>" +
                                "<font color=#4684C4>sober</font>" +
                                "<font color=#273F4C>. Enjoy your ride! </big>" +
                                "<br><font color=#273F4C> Limit in your country: </font><font color=#4684C4>" + myFormat.format(limitInCountry) + "</font></font> ";
                } else if (aboveLimit < 0){
                    float out = Math.abs(aboveLimit);
                    textOut = "<font color=#273F4C><big>You are </font>" +
                            "<font color=#4684C4><b> " + myFormat.format(out) + "</b></font>" +
                            "<font color=#273F4C> below the legal limit.<br> Be careful and enjoy your ride</big></font> ";
                } else if (aboveLimit == 0){
                    textOut = "<font color=#273F4C><big>You are </font>" +
                            "<font color=#C54747><b> on the legal limit</b></font>" +
                            "<font color=#273F4C><br> Wait before you drive! </big> </font> ";
                }else if (aboveLimit < 0.2){
                    textOut = "<font color=#273F4C><big>You are </font>" +
                            "<font color=#C54747><b> " + myFormat.format(aboveLimit) + "</b></font>" +
                            "<font color=#273F4C> above limit.<br></font> " +
                            "<font color=#C54747><b>Wait before you drive!</b></big></font> ";
                } else {
                    textOut = "<font color=#273F4C><big>You are </font>" +
                            "<font color=#C54747><b> " + myFormat.format(aboveLimit) + "</b></font>" +
                            "<font color=#273F4C> above limit.<br></font> " +
                            "<font color=#C54747><b>Don't even think about driving!</b></big></font> ";
                }
            } else {
                textDrive.setText("Limit for your country is unknown!");
            }
        }
        textDrive.setText(Html.fromHtml(textOut));
    }

    public void fillTextLevel(){


        Float currentUnits = prefs.getFloat("units", 0f);
        Float alcoLevel = prefs.getFloat("alcoLevel", 0f);
        String gender = prefs.getString("gender", null);

        String textOut = "<font color=#273F4C>Current units: </font> " +
                         "<font color=#4684C4><big>" + myFormat.format(currentUnits) + "</big></font>" +
                         "<font color=#273F4C> &ensp;&ensp;&ensp;Alcohol level: </font> " +
                         "<font color=#4684C4><big>" + myFormat.format(alcoLevel) + "</big></font><br>";

        int hours;
        int minutes;

        if(alcoLevel == 0){
            // textOut = "You are sober";
        } else if(gender.equals("M")){

            minutes = Math.round(currentUnits / 0.0167f);
            hours = minutes / 60;
            minutes = minutes - (hours*60);

            if(hours == 0){
                textOut += "<font color=#273F4C>You are expected to be sober in </font>" +
                            "<font color=#4684C4><big>" + minutes + "</big></font>" +
                            "<font color=#273F4C> minutes </font>";
            } else {
                textOut += "<font color=#273F4C>You are expected to be sober in </font>" +
                            "<font color=#4684C4><big>" + hours + "</big></font>" +
                            "<font color=#273F4C> h and </font>" +
                            "<font color=#4684C4><big>" + minutes + "</big></font>" +
                            "<font color=#273F4C> min </font>";
            }

        } else {
            minutes = Math.round(alcoLevel / (0.0167f/2));
            Log.d("calcualtion", "AlcoLevel: " + alcoLevel + " | Minutes: " + minutes);
            hours = minutes / 60;
            minutes = minutes - (hours*60);
            if(hours == 0){
                textOut += "<font color=#273F4C>You are expected to be sober in </font>" +
                        "<font color=#4684C4><big>" + minutes + "</big></font>" +
                        "<font color=#273F4C> minutes </font>";
            } else {
                textOut += "<font color=#273F4C>You are expected to be sober in </font>" +
                        "<font color=#4684C4><big>" + hours + "</big></font>" +
                        "<font color=#273F4C> hours and </font>" +
                        "<font color=#4684C4><big>" + minutes + "</big></font>" +
                        "<font color=#273F4C> minutes </font>";
            }
        }
        textLevel.setText(Html.fromHtml(textOut));
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


        int weight = prefs.getInt("weight", 50);
        String gender = prefs.getString("gender", "M");
        float r = 0.7f;
        if(gender.equals("F")) r = 0.6f;
        float alcoLevel = (units*10) / (weight * r);


        editor.putFloat("alcoLevel", alcoLevel);
        editor.apply();
    }

    // Welcome message and last meal message
    public void updateUserMessages(){

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
        Intent intent = new Intent(context, DrinkingHistory.class);
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