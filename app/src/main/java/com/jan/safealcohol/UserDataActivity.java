package com.jan.safealcohol;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class UserDataActivity extends AppCompatActivity {

    private Context context = this;
    FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(context);
    Cursor cursor;
    private EditText firstname;
    private EditText lastname;
    private EditText weight;
    private EditText gender;
    private EditText height;
    private Button updateDBbutton;

    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    public static final String MY_PREFS_FILE = "MyPrefsFile";


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.userdataactivity);
        defineVariables();
        updateDBbutton.setOnClickListener(updateDB);
        fillUserDataForm();

    }

    public void defineVariables(){
        firstname = (EditText) findViewById(R.id.firstnameET);
        lastname = (EditText) findViewById(R.id.lastnameET);
        weight = (EditText) findViewById(R.id.weightET);
        gender = (EditText) findViewById(R.id.genderET);
        height = (EditText) findViewById(R.id.heightET);
        updateDBbutton = (Button) findViewById(R.id.updateDbButton);
    }

    View.OnClickListener updateDB = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            updateUserData();
        }
    };

    public void fillUserDataForm(){

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        firstname.setText(prefs.getString("firstname", null));
        lastname.setText(prefs.getString("lastname", null));
        weight.setText(Integer.toString(prefs.getInt("weight", 0)));
        gender.setText(prefs.getString("gender", null));
        height.setText(Integer.toString(prefs.getInt("height", 0)));

    }

    public void updateUserData(){

        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        editor.putString("firstname", firstname.getText().toString());
        editor.putString("lastname", lastname.getText().toString());
        editor.putString("gender", gender.getText().toString());
        editor.putInt("weight", Integer.parseInt(weight.getText().toString()));
        editor.putInt("height", Integer.parseInt(height.getText().toString()));
        editor.apply();

        Toast.makeText(getApplicationContext(), "User data updated successfully", Toast.LENGTH_SHORT).show();
        fillUserDataForm();

    }
}
