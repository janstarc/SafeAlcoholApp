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
                addMealToDB();
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
        lastMeal.setText("Your last meal was a " + mealType + " at " + mt);
    }

    // Adds last meal to the db
    public void addMealToDB(){

        // Gets the data repository in write mode
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        editor.putInt("sizeofmeal", mealId);
        Date myDate = new Date();
        String date = dateFormat.format(myDate);
        editor.putString("timeofmeal", date);
        editor.apply();

        runFirstActivity();
    }




    public void runFirstActivity () {
        Intent intent = new Intent(context, FirstActivity.class);
        intent.putExtra("toast", "Meal was added successfully!");
        context.startActivity(intent);
    }
}
