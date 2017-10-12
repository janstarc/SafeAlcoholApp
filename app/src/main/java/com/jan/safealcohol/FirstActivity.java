package com.jan.safealcohol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


import java.io.Serializable;
import java.util.ArrayList;

import static com.jan.safealcohol.R.id.spinnerDesign;


public class FirstActivity extends AppCompatActivity implements Serializable {


    private Button secondActivityButton;
    //private Button thirdActivityButton;
    private Context context = this;
    private EditText description;
    private Button addButton;
    private ArrayList<String> itemsList = new ArrayList<>();                // Used to transfer data between activities
    private boolean newItemsAdded = false;
    private Spinner spinner;


    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstactivitydesign);


        // Defining the variables
        secondActivityButton = (Button) findViewById(R.id.secondActivity);
        description = (EditText) findViewById(R.id.description);
        addButton = (Button) findViewById(R.id.addButton);

        // Calling event listeners
        secondActivityButton.setOnClickListener(startSecondActivity);
        secondActivityButton.setOnLongClickListener(startSecondActivityLong);
        addButton.setOnClickListener(addNewElement);


        spinner = (Spinner) findViewById(spinnerDesign);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.drinks_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    // Adds new entry to the itemsList
    View.OnClickListener addNewElement = new Button.OnClickListener() {

        @Override
        public void onClick(View v){

            itemsList.add(spinner.getSelectedItem().toString());
            String amount = description.getText().toString();
            if(amount.equals("")) amount = "1";
            itemsList.add(amount);
            newItemsAdded = true;
            //Log.d("debug", "Title:")
        }
    };

    View.OnClickListener startSecondActivity = new Button.OnClickListener() {

        @Override
        public void onClick(View v){
            if(newItemsAdded){                  // Send the array list with added items to the second activity
                runSecondActivity(true, newItemsAdded, itemsList);

            } else {                            // Start normally
                runSecondActivity(true, newItemsAdded, null);
            }

        }
    };

    View.OnLongClickListener startSecondActivityLong = new Button.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v){
            runSecondActivity(false, false, null);
            return true;
        }
    };

    public void runSecondActivity (boolean b, boolean itemsAdded, ArrayList<String> itemsList) {

        Intent intent = new Intent(context, SecondActivity.class);
        intent.putExtra("flag", b);
        intent.putExtra("itemsAdded", itemsAdded);
        intent.putExtra("newItems", itemsList);

        context.startActivity(intent);
    }


}
