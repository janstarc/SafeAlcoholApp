package com.jan.safealcohol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_PERCENTAGE;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry._ID;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_NAME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.TABLE_NAME;

public class DrinkingHistory extends AppCompatActivity implements Serializable {

    private Context context = this;
    private ListView myList;
    private ListAdapter adapter;
    ArrayList<ListItem> items = new ArrayList<>();
    //private TextView alcoUnits;
    //private TextView alcoLevel;
    private TextView instructions;
    private FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
    private Spinner spinnerTime;
    private Cursor cursor;
    DecimalFormat numberFormat = new DecimalFormat("0.00");

    // Data structures to store the data read from DB
    private List itemIds = new ArrayList<>();
    private List name = new ArrayList<>();
    private List amount = new ArrayList<>();
    private List<Float> units = new ArrayList<>();
    private List timestamp = new ArrayList<>();
    private List percent = new ArrayList<>();
    private HashMap picturesMap = new HashMap();
    private HashMap percentMap = new HashMap();
    private HashMap amountMap = new HashMap();
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    public static final String MY_PREFS_FILE = "MyPrefsFile";
    private boolean emptyList = false;
    private float unitsLocal;

    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.drinking_history_design);
        defineVariables();
        createDropdownMenu();
        picturesMap = HashMaps.createPicturesMap();
        percentMap = HashMaps.createPercentageMap();

        try {
            updateListView();
            optimizeDatabase();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void defineVariables(){

        myList = (ListView) findViewById(R.id.listView);
        //alcoUnits = (TextView) findViewById(R.id.drinksSumText);
        //alcoLevel = (TextView) findViewById(R.id.alcoLevel);
        instructions = (TextView) findViewById(R.id.uiText);
        String instText = "<font color=#4684C4><big>Drinking history</big></font><br>" +
                "<font color=#273F4C><i>(You can delete an item with a long click)</i></font>";
        instructions.setText(Html.fromHtml(instText));
        spinnerTime = (Spinner) findViewById(R.id.spinnerTime);
        prefs = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILE, MODE_PRIVATE).edit();
        unitsLocal = prefs.getFloat("units", 0f);

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
                if(items.size() > 0){
                    emptyList = false;
                    String timestampS = timestamp.get(pos).toString();
                    float unitsToDelete = units.get(pos);
                    Log.d("unitsTest", "DELETE - Units: " + unitsToDelete);

                    deleteDrinkFromDb(timestampS, unitsToDelete);
                }

                if(items.size() == 0){
                    Log.d("AlcoLevelCalc", "AlcoLevelCalc --> items.size == 0");
                    emptyList = true;

                    float currentUnits = prefs.getFloat("units", -1f);
                    if(currentUnits != -1f){
                        editor.putString("newDrinkTimestamp", "NewDate");
                        Log.d("unitsToDelete", "Units to delete --> " + currentUnits);
                        editor.putFloat("newDrinkUnits", (-1)*currentUnits);
                        editor.apply();
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    /**
     *  [END] Event listeners
     */

    public void createDropdownMenu(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.time_array, R.layout.spinner_item);    // Create an ArrayAdapter using the string array and a default spinner layout
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);             // Specify the layout to use when the list of choices appears
        spinnerTime.setAdapter(adapter);                // Apply the adapter to the spinner
    }

    /**
     * 1.) Reads from DB --> Updates ArrayLists
     * 2.) Adds items from all ArrayLists to ArrayList<ListItem> items --> The one that is passed to the adapter
     * 3.) Updates values - Drinks sum and units sum
     * 4.) Updates ListView
     */
    public void updateListView() throws ParseException {


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

                if(spinnerId == 0 && timeDifference < 480){
                    addListItem(i);
                } else if (spinnerId == 1 && timeDifference < 1440) {
                    addListItem(i);
                } else if (spinnerId == 2 && timeDifference < 4320) {
                    addListItem(i);
                }
            }

        } else {
            editor.putBoolean("listEmpty", true);
            editor.apply();
            items.add(new ListItem("No drinks on the list", 0, ""));
        }


        float unitsFloat = prefs.getFloat("units", (float) 0.0);
        float levelAlco = prefs.getFloat("alcoLevel", (float) 0.0);
        //alcoUnits.setText(String.valueOf(numberFormat.format(unitsFloat)));
        //alcoLevel.setText(String.valueOf(numberFormat.format(levelAlco)));

        // Pass values to adapter
        adapter = new ListAdapter(this, items);
        myList.setAdapter(adapter);
        myList.setOnItemLongClickListener(deleteDrinkListener);
    }

    public void addListItem(int i) throws ParseException {

        String itemName = name.get(i).toString();
        float itemPercent = Float.valueOf(percent.get(i).toString());
        Log.d("crash", "WTF happens here? #" + itemName + "#");
        String pictureName;
        //if (knownDrinkName(itemName)){
        if(itemName.matches("Radler|Beer|Liquor|Wine|Distilled spirit")){
            Log.d("picName", "Here? --> Item name: " + itemName);
            pictureName = picturesMap.get(itemName).toString();
        } else {
            Log.d("picName", "Here2");
            pictureName = "custom";
        }

        int resourceId = this.getResources().getIdentifier(pictureName, "drawable", this.getPackageName());
        Log.d("resources", "Resource name: " + pictureName + " | ResourceId: " + resourceId);
        String timeStamp = timestamp.get(i).toString();

        // Date handling
        Date itemDateTime = dateFormat.parse(timeStamp);
        Date currentDateTime = new Date();
        long timeDiff = calculateTimeDifference(itemDateTime, currentDateTime);
        int hours = (int) timeDiff / 60;
        int min = (int) timeDiff % 60;
        if(hours == 0){
            timeStamp = "" + min + " minutes ago";
        } else if (hours < 24) {
            timeStamp = hours + " h and " + min + " min ago";
        } else if (hours < 48){
            timeStamp = "More than 1 day ago";
        } else {
            timeStamp = "More than 2 days ago";
        }

        // New ListItem creation and putting it to items list
        ListItem item = new ListItem(itemName + " (" + itemPercent + "%)", resourceId, "    Amount: " + amount.get(i).toString() + " dl  (" + timeStamp +")");
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
        percent = new ArrayList<>();

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY _id DESC", null);
    }

    /**
     * 1.) Copies values from cursor to the ArrayLists
     */

    public void copyToArrayLists(){
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID));
            itemIds.add(itemId);

            String nameField = cursor.getString(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_NAME));
            name.add(nameField);

            float amountField = cursor.getFloat(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_AMOUNT));
            amount.add(amountField);

            float unitField = cursor.getFloat(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_UNITS));
            units.add(unitField);

            float percentField = cursor.getFloat(
                    cursor.getColumnIndexOrThrow(COLUMN_NAME_PERCENTAGE));
            percent.add(percentField);

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
    public void deleteDrinkFromDb(String timestamp, Float unitsToDelete) throws ParseException {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        boolean lastItem = false;

        db.execSQL("DELETE FROM " + FeedReaderContract.FeedEntry.TABLE_NAME + " WHERE " + FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP + " = '" + timestamp + "'");
        Log.d("debug", "SUCCESS DELETING ITEM WITH TIMESTAMP: " + timestamp);

        editor.putString("newDrinkTimestamp", "NewDate");

        //Log.d("unitsToDelete", "Units to delete --> " + unitsToDelete);
        //Log.d("unitsTest", "DELETE New Drink units: " + unitsToDelete);
        float oldUnits = prefs.getFloat("newDrinkUnits", 0);
        float newUnitsToDelete = Math.abs(unitsToDelete) + Math.abs(oldUnits);
        //Log.d("unitsTest", "New units to delete: " + newUnitsToDelete);

        editor.putFloat("newDrinkUnits", (-1)*newUnitsToDelete);
        Toast.makeText(getApplicationContext(), "Drink successfully deleted", Toast.LENGTH_SHORT).show();

        editor.apply();
        updateListView();
        unitsChangeLocal(unitsToDelete);
        dbToLog();

    }

    public void dbToLog(){

        Log.d("debug", "Item:   | Name:  | Amount: | Units: | Timestamp: ");
        for(int i = 0; i < itemIds.size(); i++){
            Log.d("debug", "Item: " + itemIds.get(i) + " | " + name.get(i) + " | " + amount.get(i) + " | " + units.get(i) + " | " + timestamp.get(i));
        }
    }

    public void unitsChangeLocal (float reduction){

        unitsLocal -= reduction;
        if(unitsLocal < 0 || emptyList) unitsLocal = 0;
        float alcoLevelLocal = calculateAlcoLevelLocal(unitsLocal);

        //alcoUnits.setText(String.valueOf(numberFormat.format(unitsLocal)));
        //alcoLevel.setText(String.valueOf(numberFormat.format(alcoLevelLocal)));
    }

    public float calculateAlcoLevelLocal (float units){


        int weight = prefs.getInt("weight", 50);
        String gender = prefs.getString("gender", "M");
        float r = 0.7f;
        if(gender.equals("F")) r = 0.6f;
        float alcoLevel = (units*10) / (weight * r);

        return alcoLevel;
    }

    public long calculateTimeDifference(Date date1, Date date2){

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

    // Delete entries older than 3 days --> Keep the DB small
    public void optimizeDatabase() throws ParseException {

        Date currentTimestamp = new Date();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int deletedItems = 0;

        for(int i = 0; i < timestamp.size(); i++){
            String itemTimestampString = timestamp.get(i).toString();
            Date itemTimestamp = dateFormat.parse(itemTimestampString);

            if(calculateTimeDifference(currentTimestamp, itemTimestamp) > 4320) {                   // If item is older than 3 days, delete!
                db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_TIMESTAMP + "= '" + itemTimestampString + "'");
                deletedItems++;
            }
        }
        Log.d("dboptimize", deletedItems + " outdated items deleted from DB");
    }
}

/*
Accessing SharedPreference

adb root
adb shell
chmod 777 /data/data/com.jan.safealcohol/shared_prefs/MyPrefsFile.xml
exit # return to default user

*/