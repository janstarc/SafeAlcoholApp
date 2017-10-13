package com.jan.safealcohol;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.jan.safealcohol.R.id.spinnerDesign;

public class FirstActivity extends AppCompatActivity implements Serializable {


    private Button secondActivityButton;
    private Context context = this;
    private EditText description;
    private Button addButton;
    private ArrayList<String> itemsList = new ArrayList<>();                // Used to transfer data between activities
    private boolean newItemsAdded = false;
    private Spinner spinner;
    FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(context);
    private EditText customDateTime;
    private CheckBox customDateTimeCheckBox;
    


    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstactivitydesign);

        // Defining the variables
        secondActivityButton = (Button) findViewById(R.id.secondActivity);
        description = (EditText) findViewById(R.id.description);
        addButton = (Button) findViewById(R.id.addButton);
        spinner = (Spinner) findViewById(spinnerDesign);
        customDateTime = (EditText) findViewById(R.id.customDateTime);
        customDateTime.setText(DateFormat.getDateTimeInstance().format(new Date()));
        customDateTimeCheckBox = (CheckBox) findViewById(R.id.checkBox);

        // Calling event listeners
        secondActivityButton.setOnClickListener(startSecondActivity);
        secondActivityButton.setOnLongClickListener(startSecondActivityLong);
        addButton.setOnClickListener(addNewElement);

        // Dropdown menu
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.drinks_array, android.R.layout.simple_spinner_item);    // Create an ArrayAdapter using the string array and a default spinner layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);             // Specify the layout to use when the list of choices appears
        spinner.setAdapter(adapter);                // Apply the adapter to the spinner

        // Create DB if not created
        new FeedReaderDbHelper(context);
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
            insertIntoDB();
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

    public void insertIntoDB(){

        String descriptionText = description.getText().toString();
        if(descriptionText.equals("")) descriptionText = "1.0";
        float amount = Float.parseFloat(descriptionText);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Log.d("debug", "DB: " + db);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_NAME, spinner.getSelectedItem().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT, amount);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS, amount * 1.4);

        if(!customDateTimeCheckBox.isChecked()){
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP, DateFormat.getDateTimeInstance().format(new Date()));
        } else {
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP, customDateTime.getText().toString());
        }


        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);

    }

    public void runSecondActivity (boolean b, boolean itemsAdded, ArrayList<String> itemsList) {

        Intent intent = new Intent(context, SecondActivity.class);
        intent.putExtra("flag", b);
        intent.putExtra("itemsAdded", itemsAdded);
        intent.putExtra("newItems", itemsList);

        context.startActivity(intent);
    }
}
