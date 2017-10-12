package com.jan.safealcohol;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;



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
    private int alcoSum;
    private TextView drinksSumText;

    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondactivitydesign);
        myList = (ListView) findViewById(R.id.listView);

        Intent intent = getIntent();


        if(intent.getBooleanExtra("itemsAdded", true)){      // Combine items list with the items, that already exist
            Log.d("debug", "Here");

            ArrayList<String> thingsToAdd = intent.getStringArrayListExtra("newItems");
            Log.d("debug", "Size of list: " + thingsToAdd.size());
            for(int i = 0; i < thingsToAdd.size(); i += 2){
                Log.i("debug", "Value: " + i + " = " + thingsToAdd.get(i));
                items.add(new ListItem(thingsToAdd.get(i).toString(), R.drawable.ic_code_black_48dp, "Amount: " + thingsToAdd.get(i+1).toString() + "dl"));
                alcoSum += Integer.parseInt(thingsToAdd.get(i+1));
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
        
        drinksSumText = (TextView) findViewById(R.id.drinksSumText);



        //Log.d("debug", "AlcoSum=" + alcoSum);

        //delButton = (Button) findViewById(R.id.delButton);
        //delButton.setOnClickListener(deleteFilteredItems);

    }

    /*
    View.OnClickListener deleteFilteredItems = new Button.OnClickListener(){

        @Override
        public void onClick(View v){
            for(int i = 0; i < filteredItems.size(); i++){
                for(int j = 0; j < items.size(); j++){

                    Log.d("debug", "FilItems: " + filteredItems.get(i).getTitle());
                    Log.d("debug", "Items: " + items.get(j).getTitle());


                    if(filteredItems.get(i).getTitle().equals(items.get(j).getTitle())){
                        Log.d("debug", "HERE!");
                        filteredItems.remove(i);
                        items.remove(j);
                        break;
                    }
                }
            }
            showModifiedList(items);
        }
    };
    */

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
