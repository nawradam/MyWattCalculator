package com.example.mywattcalculator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    ListView listViewBills;
    Button buttonBackHistory;

    DatabaseHelper databaseHelper;

    ArrayList<String> billList;
    ArrayList<Integer> billIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        setTitle("Saved Bills");

        listViewBills = findViewById(R.id.listViewBills);
        buttonBackHistory = findViewById(R.id.buttonBackHistory);

        databaseHelper = new DatabaseHelper(this);

        buttonBackHistory.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBills();
    }

    private void loadBills() {
        billList = new ArrayList<>();
        billIds = new ArrayList<>();

        Cursor cursor = databaseHelper.getAllBills();

        if (cursor.getCount() == 0) {
            billList.add("No saved records yet.");
        } else {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String month = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MONTH));
                double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FINAL));

                billIds.add(id);
                billList.add(String.format(Locale.getDefault(), "%s - RM%.2f", month, finalCost));
            }
        }

        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.list_item_bill,
                billList
        );

        listViewBills.setAdapter(adapter);

        listViewBills.setOnItemClickListener((parent, view, position, id) -> {
            if (billIds.isEmpty()) {
                Toast.makeText(this, "No record selected.", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedId = billIds.get(position);

            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("BILL_ID", selectedId);
            startActivity(intent);
        });
    }
}