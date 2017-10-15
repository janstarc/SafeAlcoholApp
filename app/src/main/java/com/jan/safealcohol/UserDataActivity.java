package com.jan.safealcohol;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_FIRSTNAME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_GENDER;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_HEIGHT;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_LASTNAME;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.COLUMN_NAME_WEIGHT;
import static com.jan.safealcohol.FeedReaderContract.FeedEntry.TABLE2_NAME;


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

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + TABLE2_NAME, null);
        cursor.moveToNext();

        firstname.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_FIRSTNAME)));
        lastname.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LASTNAME)));
        weight.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_WEIGHT)));
        gender.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_GENDER)));
        height.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_HEIGHT)));
    }

    public void updateUserData(){

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Log.d("debug", "DB: " + db);

        String query = "UPDATE userDataNew SET " +
                "firstname = '" + firstname.getText().toString() + "', " +
                "lastname = '" + lastname.getText().toString() + "', " +
                "weight = '" + weight.getText().toString() + "', " +
                "gender = '" + gender.getText().toString() + "', " +
                "height = '" + height.getText().toString() + "' " +
                "WHERE _id = '1'";

        Log.d("db", "QUERY: " + query);
        db.execSQL(query);
        Toast.makeText(getApplicationContext(), "User data updated successfully", Toast.LENGTH_SHORT).show();
        fillUserDataForm();
    }
}
