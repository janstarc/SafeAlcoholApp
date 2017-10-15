package com.jan.safealcohol;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.Serializable;
import java.util.ArrayList;
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


    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondactivitydesign);
        defineVariables();
        updateListView();
    }

    public void defineVariables(){

        myList = (ListView) findViewById(R.id.listView);
        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(filterListListener);
        searchText = (EditText) findViewById(R.id.searchText);
        alcoUnits = (TextView) findViewById(R.id.drinksSumText);
        alcoLevel = (TextView) findViewById(R.id.alcoLevel);
    }

    /**
     *  [START] Event listeners
     */

    // Calls deleteFromDb function to delete item from drinks list
    ListView.OnItemLongClickListener deleteDrinkListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {

            Log.d("debug","LongClicked: " + pos);
            String timestampS = timestamp.get(pos).toString();
            deleteDrinkFromDb(timestampS);

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

    /**
     *  [END] Event listeners
     */

    // Implementation of the search feature
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
    public void updateListView(){

        int unitsSum = 0;

        getDrinksFromDb();                   // 1.) Stores data to name, amount, timestamp... ArrayLists
        items = new ArrayList<>();

        // 2.) Copies data from other ArrayLists to the items ArrayList --> Passed to adapter
        if(itemIds.size() > 0){
            for(int i = 0; i < itemIds.size(); i++){
                ListItem item = new ListItem(name.get(i).toString(), R.drawable.ic_opacity_black_48dp, "Amount: " + amount.get(i).toString() + "dl [" + timestamp.get(i).toString() +"]");
                items.add(item);
                unitsSum += Integer.parseInt(units.get(i).toString());
            }

        } else {
            items.add(new ListItem("No drinks on the list", R.drawable.ic_code_black_48dp, ""));
        }

        // Updates values
        alcoUnits.setText(Integer.toString(unitsSum));
        alcoLevel.setText(Integer.toString(unitsSum *2));

        // Pass values to adapter
        adapter = new ListAdapter(this, items);
        myList.setAdapter(adapter);
        myList.setOnItemLongClickListener(deleteDrinkListener);
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
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        copyToArrayLists(cursor);
    }

    /**
     * 1.) Copies values from cursor to the ArrayLists
     *
     */

    public void copyToArrayLists(Cursor cursor){
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
    public void deleteDrinkFromDb(String timestamp){

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
}