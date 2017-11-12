package com.jan.safealcohol;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by janst on 11/12/2017.
 */

public class AddMeal extends AppCompatActivity {

    //private EditText mealtime;
    private Button saveButton;
    private RadioButton mealSize1Radio;
    private RadioButton mealSize2Radio;
    private TextView lastMeal;
    public static final String MY_PREFS_FILE = "MyPrefsFile";
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SharedPreferences.Editor editor;
    SharedPreferences prefs;

    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_meal_design);

        defineVariables();
        callEventListeners();
    }

    public void defineVariables(){
        //mealtime = (EditText) findViewById(R.id.mealTimeET);
        saveButton = (Button) findViewById(R.id.addMealButton);
        mealSize1Radio = (RadioButton) findViewById(R.id.mealSize1);
        mealSize1Radio.setChecked(true);
        mealSize2Radio = (RadioButton) findViewById(R.id.mealSize2);
        lastMeal = (TextView) findViewById(R.id.lastMeal);

    }

    public void callEventListeners(){
        saveButton.setOnClickListener(addMealToDBListener);
    }

    // Adds new meal to DB
    View.OnClickListener addMealToDBListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            addMealToDB();
        }
    };

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
        //updateUserMessages();
    }

    public void updateUserMessage(){
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
        lastMeal.setText("Your last meal was a " + mealType + " on " + mt);
    }




}
