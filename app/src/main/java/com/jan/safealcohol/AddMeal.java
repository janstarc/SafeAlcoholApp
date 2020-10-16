package com.jan.safealcohol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class AddMeal extends AppCompatActivity implements View.OnClickListener {

    private Context context = this;
    private Button addMealButton;
    private Button snackButton;
    private Button midSizeButton;
    private Button fullMealButton;
    private TextView lastMeal;
    public static final String MY_PREFS_FILE = "MyPrefsFile";
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private int mealId = 1;
    private HashMap<String, String> buttonPressed;

    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_meal_design);
        defineVariables();
        updateUserMessage();
        buttonPressed = HashMaps.createButtonPressedMap();
        callEventListeners();
        drawButtons();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.snackButton:
                setFocus(snackButton);
                mealId = 1;
                break;

            case R.id.midSizeButton :
                setFocus(midSizeButton);
                mealId = 2;
                break;

            case R.id.fullMealButton :
                setFocus(fullMealButton);
                mealId = 3;
                break;

            case R.id.addMealButton :
                setFocus(addMealButton);
                try {
                    addMealToDB();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void defineVariables(){
        snackButton = (Button) findViewById(R.id.snackButton);
        midSizeButton = (Button) findViewById(R.id.midSizeButton);
        fullMealButton = (Button) findViewById(R.id.fullMealButton);
        lastMeal = (TextView) findViewById(R.id.lastMealText);
        addMealButton = (Button) findViewById(R.id.addMealButton);
    }

    // This --> Overriding onClick method from View.OnClickListener()
    public void callEventListeners(){
        snackButton.setOnClickListener(this);
        midSizeButton.setOnClickListener(this);
        fullMealButton.setOnClickListener(this);
        addMealButton.setOnClickListener(this);
    }

    // Initialization of GUI at when the activity is started
    public void drawButtons(){
        snackButton.setBackgroundResource(R.drawable.snack_default);
        midSizeButton.setBackgroundResource(R.drawable.mid_default);
        fullMealButton.setBackgroundResource(R.drawable.full_default);
        addMealButton.setBackgroundResource(R.drawable.addmeal_default);
    }

    // Radio button logic on the entire AddDrink screen
    private void setFocus(Button focus){

        drawButtons();
        String pressedKey = getResources().getResourceEntryName(focus.getId());
        String pressedFile = buttonPressed.get(pressedKey);
        Log.d("focusButtons", "Pressed file name: " + pressedFile);
        int idFocus = context.getResources().getIdentifier(pressedFile, "drawable", context.getPackageName());
        focus.setBackgroundResource(idFocus);
    }

    // User message - info about last meal added
    public void updateUserMessage(){

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        int sizeOfMeal = 0;
        String mt = null;

        try{
            sizeOfMeal = prefs.getInt("sizeofmeal", 1);
            mt = prefs.getString("timeofmeal", null);
        } catch (NullPointerException e){
            Log.d("Error", "NullPointerException");
        }

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
        if(mt != null){
            lastMeal.setText("Your last meal was a " + mealType + " at " + mt);
        } else {
            lastMeal.setText("You didn't add any meals yet");
        }

    }

    // Adds last meal to the db
    public void addMealToDB() throws ParseException {

        Log.d("foodTest", "Entered addMeal() with foodId" + mealId);

        // Check, if meal can be added --> Check time limit
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        Date currentDate = new Date();
        Date lastMealDate;
        String currentDateString = dateFormat.format(currentDate);
        long timeDifference = -1;

        String snackTime = prefs.getString("snackTimestamp", null);
        String msTime = prefs.getString("midsizeMealTimestamp", null);
        String fullTime = prefs.getString("fullMealTimestamp", null);

        // Check, if meal can be added --> If not, display Toast and return from addToDb() function
        switch (mealId){

            case 1:

                // If snackTime exists - some meal was already added in the past. Then, calculate time difference between meals
                if(snackTime != null){
                    lastMealDate = dateFormat.parse(snackTime);
                    timeDifference = calculateTimeDifference(currentDate, lastMealDate);
                }

                // If there is LESS than 15 min between snacks AND this is not the first meal added
                if(timeDifference < 15 && timeDifference != -1){
                    runFirstActivity("ERROR: You can add a new snack every 15 minutes!");
                    return;
                }

                  break;

            case 2:

                if(msTime != null){
                    lastMealDate = dateFormat.parse(msTime);
                    timeDifference = calculateTimeDifference(currentDate, lastMealDate);
                }

                if(timeDifference < 60 && timeDifference != -1){
                    Log.d("foodTest", "If evaluated to TRUE  --> RETURN");
                    runFirstActivity("ERROR: You can add mid-size meal every hour!");
                    return;
                }

                /*
                if(snackTime != null && (calculateTimeDifference(dateFormat.parse(snackTime), currentDate) < 15 || calculateTimeDifference(dateFormat.parse()))){
                    runFirstActivity();
                }
                */

                break;

            case 3:

                if(fullTime != null){
                    lastMealDate = dateFormat.parse(fullTime);
                    timeDifference = calculateTimeDifference(currentDate, lastMealDate);
                }

                if(timeDifference < 180 && timeDifference != -1){
                    runFirstActivity("ERROR: You can add full meal every 3 hours!");
                    return;
                }

                break;
        }

        // Looks like meal can be added --> Time difference seems ok
            // Otherwise, there was a return from function --> We're no longer here

        switch (mealId){
            case 1:
                editor.putString("snackTimestamp", currentDateString);
                editor.putBoolean("snackCalculated", false);

                break;

            case 2:

                editor.putString("midsizeMealTimestamp", currentDateString);
                editor.putBoolean("midsizeMealCalculated", false);

                break;

            case 3:
                editor.putString("fullMealTimestamp", currentDateString);
                editor.putBoolean("fullMealCalculated", false);

                break;
        }

        editor.apply();
        runFirstActivity("Your meal was added successfully!");
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

    public void runFirstActivity (String message) {
        Intent intent = new Intent(context, FirstActivity.class);
        intent.putExtra("toast", message);
        context.startActivity(intent);
    }
}
