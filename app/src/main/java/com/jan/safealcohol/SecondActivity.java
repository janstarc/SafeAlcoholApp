package com.jan.safealcohol;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.database.sqlite.*;

public class SecondActivity extends AppCompatActivity implements Serializable {


    private ListView myList;
    private ListAdapter adapter;
    private Button sortButton;
    private Button filterButton;
    private EditText filterText;
    ArrayList<ListItem> items = new ArrayList<>();
    private boolean noResults;
    private boolean itemsFiltered = false;
    ArrayList<ListItem> filteredItems;
    private int unitsSum;

    private TextView alcoUnits;
    private TextView alcoLevel;
    //Context context = this  ;

    private FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
    private Cursor cursor;
    private List itemIds = new ArrayList<>();
    private List name = new ArrayList<>();
    private List amount = new ArrayList<>();
    private List units = new ArrayList<>();
    private List timestamp = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondactivitydesign);
        myList = (ListView) findViewById(R.id.listView);

        readFromDb();

        if(itemIds.size() > 0){
            for(int i = 0; i < itemIds.size(); i++){
                ListItem item = new ListItem(name.get(i).toString(), R.drawable.ic_opacity_black_48dp, "Amount: " + amount.get(i).toString() + "dl [" + timestamp.get(i).toString() +"]");
                items.add(item);
                unitsSum += Integer.parseInt(units.get(i).toString());
            }

        } else {
            items.add(new ListItem("No drinks added", R.drawable.ic_code_black_48dp, ""));
        }

        adapter = new ListAdapter(this, items);
        myList.setAdapter(adapter);

        sortButton = (Button) findViewById(R.id.sortButton);
        sortButton.setOnClickListener(sortList);

        filterButton = (Button) findViewById(R.id.filterButton);
        filterButton.setOnClickListener(filterList);
        filterText = (EditText) findViewById(R.id.filterText);
        
        alcoUnits = (TextView) findViewById(R.id.drinksSumText);
        alcoLevel = (TextView) findViewById(R.id.alcoLevel);

        alcoUnits.setText(Integer.toString(unitsSum));
        alcoLevel.setText(Integer.toString(unitsSum*2));

        // TODO - Delete option on long click
        //myList.setLongClickable(true);
        //myList.setOnItemLongClickListener(longClickListener);

    }

    /*
    myList.setOnItemLongClickListener(new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
        int pos, long id) {
            // TODO Auto-generated method stub

            Log.v("long clicked","pos: " + pos);

            return true;
        }
    });*/

    public void readFromDb(){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Log.d("debug", "DB:" + db);

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_NAME,
                FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT,
                FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP
        };

        // Filter results WHERE "title" = 'My Title'
        //String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " = ";
        //String[] selectionArgs = { "My Title" };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedReaderContract.FeedEntry._ID + " ASC";

        cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,        // The table to query
                projection,                                     // The columns to return
                null,                                           // The columns for the WHERE clause
                null,                                           // The values for the WHERE clause
                null,                                           // don't group the rows
                null,                                           // don't filter by row groups
                sortOrder                                       // The sort order
        );


        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID));
            itemIds.add(itemId);

            String nameField = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_NAME));
            name.add(nameField);

            int amountField = cursor.getInt(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_AMOUNT));
            amount.add(amountField);

            int unitField = cursor.getInt(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_UNITS));
            units.add(unitField);

            String timestampField = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_TIMESTAMP));
            timestamp.add(timestampField);

        }
        cursor.close();

        Log.d("debug", "Item:   | Name:  | Amount: | Units: | Timestamp: ");
        for(int i = 0; i < itemIds.size(); i++){
            Log.d("debug", "Item: " + itemIds.get(i) + " | " + name.get(i) + " | " + amount.get(i) + " | " + units.get(i) + " | " + timestamp.get(i));
        }
    }




    View.OnClickListener filterList = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            String text = filterText.getText().toString();
            int textLen = text.length();
            filteredItems = new ArrayList<>();
            Log.d("debug", "TextLen: " + textLen);

            if(textLen != 0) {
                for (int i = 0; i < items.size(); i++) {

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
                itemsFiltered = true;
                showModifiedList(filteredItems);

            } else {
                itemsFiltered = false;
                showModifiedList(items);
            }
        }
    };

    View.OnClickListener sortList = new Button.OnClickListener() {

        @Override
        public void onClick(View v){

            //Log.d("debug", "Sort button pressed. Num of items: " + items.size());
            if(itemsFiltered){
                Log.d("debug", "Sorting filtered items");
                Collections.sort(filteredItems, new CustomComparator());
                showModifiedList(filteredItems);

            } else {
                Log.d("debug", "Sorting non-filtered items");
                Collections.sort(items, new CustomComparator());
                showModifiedList(items);
            }
        }
    };

    public void showModifiedList(ArrayList<ListItem> items){
        adapter = new ListAdapter(this, items);
        myList.setAdapter(adapter);
    }
}

class CustomComparator implements Comparator<ListItem> {
    @Override
    public int compare(ListItem o1, ListItem o2) {
        return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
    }
}