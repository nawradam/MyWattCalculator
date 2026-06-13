package com.example.mywattcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "mywatt.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "bill_records";

    public static final String COL_ID = "id";
    public static final String COL_MONTH = "month";
    public static final String COL_UNIT = "unit";
    public static final String COL_TOTAL = "total_charges";
    public static final String COL_REBATE = "rebate";
    public static final String COL_FINAL = "final_cost";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MONTH + " TEXT, " +
                COL_UNIT + " INTEGER, " +
                COL_TOTAL + " REAL, " +
                COL_REBATE + " REAL, " +
                COL_FINAL + " REAL)";
        Log.d("DATABASE", "onCreate: " + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertBill(String month, int unit, double totalCharges, double rebate, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_MONTH, month);
        values.put(COL_UNIT, unit);
        values.put(COL_TOTAL, totalCharges);
        values.put(COL_REBATE, rebate);
        values.put(COL_FINAL, finalCost);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    public Cursor getAllBills() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC", null);
    }

    public Cursor getBillById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public boolean updateBill(int id, String month, int unit, double totalCharges, double rebate, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_MONTH, month);
        values.put(COL_UNIT, unit);
        values.put(COL_TOTAL, totalCharges);
        values.put(COL_REBATE, rebate);
        values.put(COL_FINAL, finalCost);

        int result = db.update(
                TABLE_NAME,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }

    public boolean deleteBill(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_NAME,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }
}