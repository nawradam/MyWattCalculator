package com.example.mywattcalculator;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    Spinner spinnerMonthDetail;
    EditText editTextUnitDetail;
    SeekBar seekBarRebateDetail;
    TextView textRebateValueDetail, textTotalChargesDetail, textFinalCostDetail;
    Button buttonUpdate, buttonDelete, buttonBackDetail;

    DatabaseHelper databaseHelper;

    int billId;
    int rebatePercentage = 0;

    String[] months = {
            "Select Month",
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle("Bill Details");

        databaseHelper = new DatabaseHelper(this);

        spinnerMonthDetail = findViewById(R.id.spinnerMonthDetail);
        editTextUnitDetail = findViewById(R.id.editTextUnitDetail);
        seekBarRebateDetail = findViewById(R.id.seekBarRebateDetail);
        textRebateValueDetail = findViewById(R.id.textRebateValueDetail);
        textTotalChargesDetail = findViewById(R.id.textTotalChargesDetail);
        textFinalCostDetail = findViewById(R.id.textFinalCostDetail);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonBackDetail = findViewById(R.id.buttonBackDetail);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                months
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerMonthDetail.setAdapter(adapter);

        billId = getIntent().getIntExtra("BILL_ID", -1);

        if (billId == -1) {
            Toast.makeText(this, "Record not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBillDetails();

        seekBarRebateDetail.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rebatePercentage = progress;
                textRebateValueDetail.setText("Rebate: " + rebatePercentage + "%");
                updatePreviewCalculation();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        editTextUnitDetail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreviewCalculation();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        buttonUpdate.setOnClickListener(v -> updateRecord());

        buttonDelete.setOnClickListener(v -> confirmDelete());

        buttonBackDetail.setOnClickListener(v -> finish());
    }

    private void loadBillDetails() {
        Cursor cursor = databaseHelper.getBillById(billId);

        if (cursor.moveToFirst()) {
            String month = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MONTH));
            int unit = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UNIT));
            double totalCharges = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TOTAL));
            double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REBATE));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FINAL));

            setSpinnerMonth(month);
            editTextUnitDetail.setText(String.valueOf(unit));

            rebatePercentage = (int) rebate;
            seekBarRebateDetail.setProgress(rebatePercentage);
            textRebateValueDetail.setText("Rebate: " + rebatePercentage + "%");

            textTotalChargesDetail.setText(String.format(Locale.getDefault(), "Total Charges: RM%.2f", totalCharges));
            textFinalCostDetail.setText(String.format(Locale.getDefault(), "Final Cost: RM%.2f", finalCost));
        } else {
            Toast.makeText(this, "Record not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        cursor.close();
    }

    private void setSpinnerMonth(String month) {
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(month)) {
                spinnerMonthDetail.setSelection(i);
                return;
            }
        }
    }

    private boolean validateInput() {
        String selectedMonth = spinnerMonthDetail.getSelectedItem().toString();
        String unitText = editTextUnitDetail.getText().toString().trim();

        if (selectedMonth.equals("Select Month")) {
            Toast.makeText(this, "Please select a month.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (unitText.isEmpty()) {
            editTextUnitDetail.setError("Please enter electricity unit used.");
            editTextUnitDetail.requestFocus();
            return false;
        }

        int unit = Integer.parseInt(unitText);

        if (unit < 1) {
            editTextUnitDetail.setError("Unit must be at least 1 kWh.");
            editTextUnitDetail.requestFocus();
            return false;
        }

        if (unit > 1000) {
            editTextUnitDetail.setError("Unit must not exceed 1000 kWh.");
            editTextUnitDetail.requestFocus();
            return false;
        }

        return true;
    }

    private void updatePreviewCalculation() {
        String unitText = editTextUnitDetail.getText().toString().trim();

        if (unitText.isEmpty()) {
            return;
        }

        int unit;

        try {
            unit = Integer.parseInt(unitText);
        } catch (NumberFormatException e) {
            return;
        }

        if (unit < 1 || unit > 1000) {
            return;
        }

        double totalCharges = calculateTotalCharges(unit);
        double finalCost = totalCharges - (totalCharges * rebatePercentage / 100.0);

        textTotalChargesDetail.setText(String.format(Locale.getDefault(), "Total Charges: RM%.2f", totalCharges));
        textFinalCostDetail.setText(String.format(Locale.getDefault(), "Final Cost: RM%.2f", finalCost));
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

    private void updateRecord() {
        if (!validateInput()) {
            return;
        }

        String month = spinnerMonthDetail.getSelectedItem().toString();
        int unit = Integer.parseInt(editTextUnitDetail.getText().toString().trim());

        double totalCharges = calculateTotalCharges(unit);
        double finalCost = totalCharges - (totalCharges * rebatePercentage / 100.0);

        boolean updated = databaseHelper.updateBill(
                billId,
                month,
                unit,
                totalCharges,
                rebatePercentage,
                finalCost
        );

        if (updated) {
            Toast.makeText(this, "Record updated successfully.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update record.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Record");
        builder.setMessage("Are you sure you want to delete this record?");
        builder.setPositiveButton("Yes", (dialog, which) -> deleteRecord());
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void deleteRecord() {
        boolean deleted = databaseHelper.deleteBill(billId);

        if (deleted) {
            Toast.makeText(this, "Record deleted successfully.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete record.", Toast.LENGTH_SHORT).show();
        }
    }
}