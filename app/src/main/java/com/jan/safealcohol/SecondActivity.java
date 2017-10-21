package com.jan.safealcohol;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_NAME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.TABLE_NAME;

public class SecondActivity extends AppCompatActivity implements Serializable {

    private ListView myList;
    private ListAdapter adapter;
    private Button searchButton;
    private EditText searchText;
    ArrayList<ListItem> items = new ArrayList<>();
    private boolean noResults;
    ArrayList<ListItem> filteredItems;
    private TextView alcoUnits;
    private TextView alcoLevel;
    private FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);

    // Data structures to store the data read from DB
    private List itemIds = new ArrayList<>();
    private List name = new ArrayList<>();
    private List amount = new ArrayList<>();
    private List units = new ArrayList<>();
    private List timestamp = new ArrayList<>();
    private Spinner spinnerTime;
    private Cursor cursor;
    private Button resetButton;

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    private Button testButton;
    public static final String MY_PREFS_FILE = "MyPrefsFile";

    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondactivitydesign);
        defineVariables();
        try {
            updateUnits((float) 0.0);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            updateListView();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        createDropdownMenu();
    }

    public void defineVariables(){

        myList = (ListView) findViewById(R.id.listView);
        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(filterListListener);
        searchText = (EditText) findViewById(R.id.searchText);
        alcoUnits = (TextView) findViewById(R.id.drinksSumText);
        alcoLevel = (TextView) findViewById(R.id.alcoLevel);
        spinnerTime = (Spinner) findViewById(R.id.spinnerTime);

        // TODO --> To delete below
        testButton = (Button) findViewById(R.id.testButton);
        testButton.setOnClickListener(testButtonListener);
        resetButton = (Button) findViewById(R.id.resetButton);
        resetButton.setOnClickListener(resetButtonListener);

        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                try {
                    updateListView();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    /**
     *  [START] Event listeners
     */

    // Calls deleteFromDb function to delete item from drinks list
    ListView.OnItemLongClickListener deleteDrinkListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {

            Log.d("debug","LongClicked: " + pos);

            try {
                if(items.size() != 1 && !items.get(0).getTitle().equals("No drinks on the list")){
                    String timestampS = timestamp.get(pos).toString();
                    deleteDrinkFromDb(timestampS);
                } else {
                    Toast.makeText(getApplicationContext(), "No drinks left - impossible to delete", Toast.LENGTH_SHORT).show();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return true;
        }
    };

    // Calls the function filterList --> Search feature
    View.OnClickListener filterListListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            filterList();
        }
    };

    // TODO To delete!!! --> Just for testing data storage!
    View.OnClickListener testButtonListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            // try-catch cause of parse exception
            try {
                handleDateDifference();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };

    View.OnClickListener resetButtonListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
            editor.putFloat("units", (float) 0.0);
            editor.apply();
            try {
                updateListView();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };



    /**
     *  [END] Event listeners
     */

    public void createDropdownMenu(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.time_array, android.R.layout.simple_spinner_item);    // Create an ArrayAdapter using the string array and a default spinner layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);             // Specify the layout to use when the list of choices appears
        spinnerTime.setAdapter(adapter);                // Apply the adapter to the spinner
    }

    /** Implementation of the search feature */
    public void filterList(){

        String text = searchText.getText().toString();              // Get query
        int textLen = text.length();
        filteredItems = new ArrayList<>();                          // Create new ArrayList to put in the results

        if(textLen != 0) {                                          // If there is search query
            for (int i = 0; i < items.size(); i++) {                // Search part

                if (textLen <= items.get(i).getTitle().length() &&
                        (text.toLowerCase()).equals(items.get(i).getTitle().substring(0, textLen).toLowerCase())) {
                    filteredItems.add(items.get(i));
                    noResults = false;

                } else if (textLen > items.size()){
                    Log.d("debug", "Here!");
                    if(!noResults) filteredItems.add(new ListItem("No results", R.drawable.ic_exit_to_app_black_48dp, ""));
                    noResults = true;
                }
            }
            showModifiedList(filteredItems);                        // Update list

        } else {
            showModifiedList(items);                                // Show original list - no query
        }
    }

    /**
     * 1.) Reads from DB --> Updates ArrayLists
     * 2.) Adds items from all ArrayLists to ArrayList<ListItem> items --> The one that is passed to the adapter
     * 3.) Updates values - Drinks sum and units sum
     * 4.) Updates ListView
     */
    public void updateListView() throws ParseException {

        int unitsSum = 0;

        getDrinksFromDb();                   // 1.) Stores data to name, amount, timestamp... ArrayLists
        copyToArrayLists();                  // 2.) Copies data to 4 ArrayLists
        items = new ArrayList<>();

        // Get current date
        Date currentTimestamp = new Date();
        int spinnerId = (int) spinnerTime.getSelectedItemId();
        Log.d("spinner", "SpinnerId: " + spinnerId);


        // 2.) Copies data from other ArrayLists to the items ArrayList --> Passed to adapter
        if(itemIds.size() > 0){
            for(int i = 0; i < itemIds.size(); i++){

                String itemTimestampString = timestamp.get(i).toString();
                Date itemTimestamp = dateFormat.parse(itemTimestampString);
                long timeDifference = calculateTimeDifference(currentTimestamp, itemTimestamp);
                Log.d("spinner", "TimeDiff: " + timeDifference + " --> Name: " + name.get(i).toString());

                // TODO Make it more readable
                if(spinnerId == 0 && timeDifference < 480){
                    addListItem(i);
                    //ListItem item = new ListItem(name.get(i).toString(), R.drawable.ic_opacity_black_48dp, "Amount: " + amount.get(i).toString() + "dl [" + timestamp.get(i).toString() +"]");
                    //items.add(item);
                    unitsSum += Integer.parseInt(units.get(i).toString());
                } else if (spinnerId == 1 && timeDifference < 1440) {
                    addListItem(i);
                    //ListItem item = new ListItem(name.get(i).toString(), R.drawable.ic_opacity_black_48dp, "Amount: " + amount.get(i).toString() + "dl [" + timestamp.get(i).toString() +"]");
                    //items.add(item);
                    unitsSum += Integer.parseInt(units.get(i).toString());
                } else if (spinnerId == 2 && timeDifference < 4320) {
                    addListItem(i);
                    //ListItem item = new ListItem(name.get(i).toString(), R.drawable.ic_opacity_black_48dp, "Amount: " + amount.get(i).toString() + "dl [" + timestamp.get(i).toString() +"]");
                    //items.add(item);
                    unitsSum += Integer.parseInt(units.get(i).toString());
                }
            }

        } else {
            items.add(new ListItem("No drinks on the list", R.drawable.ic_code_black_48dp, ""));
        }

        // TODO Finish the calculation --> // Updates values
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        float unitsFloat = prefs.getFloat("units", (float) 0.0);
        DecimalFormat numberFormat = new DecimalFormat("0.0");
        alcoUnits.setText(String.valueOf(numberFormat.format(unitsFloat)));
        alcoLevel.setText(String.valueOf(numberFormat.format(unitsFloat/3.6)));

        // Pass values to adapter
        adapter = new ListAdapter(this, items);
        myList.setAdapter(adapter);
        myList.setOnItemLongClickListener(deleteDrinkListener);
    }

    public void addListItem(int i){
        ListItem item = new ListItem(name.get(i).toString(), R.drawable.ic_opacity_black_48dp, "Amount: " + amount.get(i).toString() + "dl [" + timestamp.get(i).toString() +"]");
        items.add(item);
    }

    /**
     * 1.) Creates new instances of all ArrayLists
     * 2.) Executes SELECT statement on the DB
     * 3.) Copies values from cursor to ArrayLists
     */
    public void getDrinksFromDb(){

        itemIds = new ArrayList<>();
        name = new ArrayList<>();
        amount = new ArrayList<>();
        units = new ArrayList<>();
        timestamp = new ArrayList<>();

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    /**
     * 1.) Copies values from cursor to the ArrayLists
     *
     */

    public void copyToArrayLists(){
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID));
            itemIds.add(itemId);

            String nameField = cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_NAME));
            name.add(nameField);

            int amountField = cursor.getInt(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_AMOUNT));
            amount.add(amountField);

            int unitField = cursor.getInt(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_UNITS));
            units.add(unitField);

            String timestampField = cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_TIMESTAMP));
            timestamp.add(timestampField);
        }
        cursor.close();
    }

    /**
     * 1.) Deletes value with timestamp from the DB
     * 2.) Updates the list view
     */
    public void deleteDrinkFromDb(String timestamp) throws ParseException {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.execSQL("DELETE FROM " + FeedReaderContract.FeedEntry.TABLE_NAME + " WHERE " + FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP + " = '" + timestamp + "'");
        Log.d("debug", "SUCCESS DELETING ITEM WITH TIMESTAMP: " + timestamp);
        Toast.makeText(getApplicationContext(), "Drink successfully deleted", Toast.LENGTH_SHORT).show();

        updateListView();
        dbToLog();
    }

    public void showModifiedList(ArrayList<ListItem> items){
        adapter = new ListAdapter(this, items);
        myList.setAdapter(adapter);
    }

    public void dbToLog(){

        Log.d("debug", "Item:   | Name:  | Amount: | Units: | Timestamp: ");
        for(int i = 0; i < itemIds.size(); i++){
            Log.d("debug", "Item: " + itemIds.get(i) + " | " + name.get(i) + " | " + amount.get(i) + " | " + units.get(i) + " | " + timestamp.get(i));
        }
    }

    // TODO Testnig method, to delete later!!!
    public void handleDateDifference() throws ParseException {

        // TODO dateFormat defined in the upper part!
        // TODO Date handling
        Date myDate = new Date();
        String date = dateFormat.format(myDate);

        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        editor.putString("unitsTimestamp", date);
        editor.apply();

        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        String dateFromDb = prefs.getString("unitsTimestamp", null);
        Toast.makeText(getApplicationContext(), "DateFromDB: " + dateFromDb, Toast.LENGTH_LONG).show();

        Log.d("time123", "HERE0");
        Date d1 = dateFormat.parse(dateFromDb);
        Date d2 = dateFormat.parse("2017-10-19 06:55:00");
        Log.d("time123", "HERE0");

        long minutesDiff = calculateTimeDifference(d1, d2);
        Log.d("time123", "MinutesDiff: " + minutesDiff);

    }

    public void updateUnits(float newDrinkUnits) throws ParseException {

        // Get current date
        Date currentTimestamp = new Date();

        // Get the unitsOld and unitsTimestamp from the SharedPref
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        float unitsOld = prefs.getFloat("units", (float) 0.0);                        // Gets info from the SharedPref
        String unitsTimestampString = prefs.getString("unitsTimestamp", dateFormat.format(currentTimestamp));        // Gets the timestamp from the DB

        // Convert unistTimestamp from SharedPref to Date + Calculate timeDiff
        Date unitsTimestamp = dateFormat.parse(unitsTimestampString);
        long timeDifferenceMin = calculateTimeDifference(currentTimestamp, unitsTimestamp);


        if(timeDifferenceMin > 0) {                                         // To prevent changing timeStamp, without changing units

            float unitsMinus = (float) (timeDifferenceMin * 0.5);           // TODO Wrong factor! [unitsDrop/min]
            Toast.makeText(getApplicationContext(),
                    "UnitsMinus: " + unitsMinus + " | TimeDiff: " + timeDifferenceMin, Toast.LENGTH_LONG).show();

            float unitsNew = (newDrinkUnits + unitsOld - unitsMinus);       // units --> Current drink | unitsOld --> Prev units from SharedPref | unitsMinus --> timeDiff * decreaseOnMin
            if (unitsNew < 0) unitsNew = 0;                                 // To avoid neg. units --> You can be max sober
            editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
            editor.putFloat("units", unitsNew);                                 // Put new info to the SharedPref
            String newTimestamp = dateFormat.format(currentTimestamp);          // Update last calculation value for units in
            editor.putString("unitsTimestamp", newTimestamp);                   // shared pref file!
            editor.apply();

        } else {
            Toast.makeText(getApplicationContext(), "Calculated less than 1min ago!", Toast.LENGTH_LONG).show();
        }
        alcoUnits.setText("Current units: " + prefs.getFloat("units", (float) 0.0));
    }



    public long calculateTimeDifference(Date date1, Date date2){

        long second = 1000l;
        long minute = 60l * second;
        long hour = 60l * minute;

        // calculation
        long diff = date2.getTime() - date1.getTime();

        // printing output
        Log.d("time123", String.format("%02d", diff / hour) + " hours, ");
        Log.d("time123", String.format("%02d", (diff % hour) / minute) + " minutes, ");
        Log.d("time123", String.format("%02d", (diff % minute) / second) + " seconds");
        long hoursOut = diff/hour;
        long minOut = (diff % hour) / minute;

        return Math.abs(hoursOut*60+minOut);
    }
}

/*
Accessing SharedPreference

adb root
adb shell
chmod 777 /data/data/com.jan.safealcohol/shared_prefs/MyPrefsFile.xml
exit # return to default user

*/