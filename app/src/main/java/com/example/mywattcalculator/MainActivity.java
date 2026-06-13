package com.example.mywattcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerMonth;
    EditText editTextUnit;
    SeekBar seekBarRebate;
    TextView textRebateValue, textTotalCharges, textFinalCost;
    Button buttonCalculate, buttonSave, buttonHistory, buttonAbout;

    DatabaseHelper databaseHelper;

    double totalCharges = 0.0;
    double finalCost = 0.0;
    int rebatePercentage = 0;

    String[] months = {
            "Select Month",
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("MyWatt Calculator");

        databaseHelper = new DatabaseHelper(this);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        editTextUnit = findViewById(R.id.editTextUnit);
        seekBarRebate = findViewById(R.id.seekBarRebate);
        textRebateValue = findViewById(R.id.textRebateValue);
        textTotalCharges = findViewById(R.id.textTotalCharges);
        textFinalCost = findViewById(R.id.textFinalCost);
        buttonCalculate = findViewById(R.id.buttonCalculate);
        buttonSave = findViewById(R.id.buttonSave);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonAbout = findViewById(R.id.buttonAbout);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                months
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        seekBarRebate.setMax(5);
        seekBarRebate.setProgress(0);

        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rebatePercentage = progress;
                textRebateValue.setText("Rebate: " + rebatePercentage + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        buttonCalculate.setOnClickListener(v -> calculateBill());

        buttonSave.setOnClickListener(v -> saveRecord());

        buttonHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        buttonAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInput() {
        String selectedMonth = spinnerMonth.getSelectedItem().toString();
        String unitText = editTextUnit.getText().toString().trim();

        if (selectedMonth.equals("Select Month")) {
            Toast.makeText(this, "Please select a month.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (unitText.isEmpty()) {
            editTextUnit.setError("Please enter electricity unit used.");
            editTextUnit.requestFocus();
            return false;
        }

        int unit = Integer.parseInt(unitText);

        if (unit < 1) {
            editTextUnit.setError("Unit must be at least 1 kWh.");
            editTextUnit.requestFocus();
            return false;
        }

        if (unit > 1000) {
            editTextUnit.setError("Unit must not exceed 1000 kWh.");
            editTextUnit.requestFocus();
            return false;
        }

        return true;
    }

    private void calculateBill() {
        if (!validateInput()) {
            return;
        }

        int unit = Integer.parseInt(editTextUnit.getText().toString().trim());

        totalCharges = calculateTotalCharges(unit);
        finalCost = totalCharges - (totalCharges * rebatePercentage / 100.0);

        textTotalCharges.setText(String.format(Locale.getDefault(), "Total Charges: RM%.2f", totalCharges));
        textFinalCost.setText(String.format(Locale.getDefault(), "Final Cost: RM%.2f", finalCost));
    }

    private double calculateTotalCharges(int unit) {
        double total = 0.0;
        int remaining = unit;

        if (remaining > 0) {
            int block = Math.min(remaining, 200);
            total += block * 0.218;
            remaining -= block;
        }

        if (remaining > 0) {
            int block = Math.min(remaining, 100);
            total += block * 0.334;
            remaining -= block;
        }

        if (remaining > 0) {
            int block = Math.min(remaining, 300);
            total += block * 0.516;
            remaining -= block;
        }

        if (remaining > 0) {
            total += remaining * 0.546;
        }

        return total;
    }

    private void saveRecord() {
        if (!validateInput()) {
            return;
        }

        String month = spinnerMonth.getSelectedItem().toString();
        int unit = Integer.parseInt(editTextUnit.getText().toString().trim());

        totalCharges = calculateTotalCharges(unit);
        finalCost = totalCharges - (totalCharges * rebatePercentage / 100.0);

        textTotalCharges.setText(String.format(Locale.getDefault(), "Total Charges: RM%.2f", totalCharges));
        textFinalCost.setText(String.format(Locale.getDefault(), "Final Cost: RM%.2f", finalCost));

        boolean inserted = databaseHelper.insertBill(
                month,
                unit,
                totalCharges,
                rebatePercentage,
                finalCost
        );

        if (inserted) {
            Toast.makeText(this, "Record saved successfully.", Toast.LENGTH_SHORT).show();
            clearForm();
        } else {
            Toast.makeText(this, "Failed to save record.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        spinnerMonth.setSelection(0);
        editTextUnit.setText("");
        seekBarRebate.setProgress(0);
        textRebateValue.setText("Rebate: 0%");
        textTotalCharges.setText("Total Charges: RM0.00");
        textFinalCost.setText("Final Cost: RM0.00");
        rebatePercentage = 0;
    }
}