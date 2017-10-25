package com.jan.safealcohol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;


public class UserDataActivity extends AppCompatActivity {

    private Context context = this;
    private EditText firstname;
    private EditText lastname;
    private EditText weight;
    private EditText height;
    private Button updateDBbutton;
    private RadioButton maleRadio;
    private RadioButton femaleRadio;
    private Spinner countriesSpinner;


    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    public static final String MY_PREFS_FILE = "MyPrefsFile";


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.userdataactivity);
        defineVariables();
        createDropdownMenu();
        updateDBbutton.setOnClickListener(updateDB);
        fillUserDataForm();



    }

    public void defineVariables(){
        firstname = (EditText) findViewById(R.id.firstnameET);
        lastname = (EditText) findViewById(R.id.lastnameET);
        weight = (EditText) findViewById(R.id.weightET);
        height = (EditText) findViewById(R.id.heightET);
        updateDBbutton = (Button) findViewById(R.id.updateDbButton);
        maleRadio = (RadioButton) findViewById(R.id.maleRadio);
        femaleRadio = (RadioButton) findViewById(R.id.femaleRadio);
        countriesSpinner = (Spinner) findViewById(R.id.countriesSpinner);
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
        Log.d("spinner", "Id: " + prefs.getInt("countryId", 0));
        countriesSpinner.setSelection(prefs.getInt("countryId", 0));

        if(prefs.getString("gender", null).equals("null") || prefs.getString("gender", null).equals("M")){
            maleRadio.setChecked(true);
        } else {
            femaleRadio.setChecked(true);
        }
        height.setText(Integer.toString(prefs.getInt("height", 0)));

    }

    public void updateUserData(){

        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        editor.putString("firstname", firstname.getText().toString());
        editor.putString("lastname", lastname.getText().toString());
        if(maleRadio.isChecked()) editor.putString("gender", "M");
        else editor.putString("gender", "F");
        editor.putInt("weight", Integer.parseInt(weight.getText().toString()));
        editor.putInt("height", Integer.parseInt(height.getText().toString()));
        editor.putString("country", countriesSpinner.getSelectedItem().toString());
        editor.putInt("countryId", (int) countriesSpinner.getSelectedItemId());
        editor.apply();

        Toast.makeText(getApplicationContext(), "User data updated successfully", Toast.LENGTH_SHORT).show();
        fillUserDataForm();

    }

    public void createDropdownMenu(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.countries, android.R.layout.simple_spinner_item);    // Create an ArrayAdapter using the string array and a default spinner layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);             // Specify the layout to use when the list of choices appears
        countriesSpinner.setAdapter(adapter);                // Apply the adapter to the spinner
    }
}
