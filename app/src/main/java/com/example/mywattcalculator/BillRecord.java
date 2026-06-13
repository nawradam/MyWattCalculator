package com.example.mywattcalculator;

public class BillRecord {
    private int id;
    private String month;
    private int unit;
    private double totalCharges;
    private double rebate;
    private double finalCost;

    public BillRecord(int id, String month, int unit, double totalCharges, double rebate, double finalCost) {
        this.id = id;
        this.month = month;
        this.unit = unit;
        this.totalCharges = totalCharges;
        this.rebate = rebate;
        this.finalCost = finalCost;
    }

    public int getId() {
        return id;
    }

    public String getMonth() {
        return month;
    }

    public int getUnit() {
        return unit;
    }

    public double getTotalCharges() {
        return totalCharges;
    }

    public double getRebate() {
        return rebate;
    }

    public double getFinalCost() {
        return finalCost;
    }
}