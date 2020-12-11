package com.example.myweatherapp.objects;

public class Weather {

    private String date;
    private String icon;
    private String condition;
    private String temp_high_low;

    public Weather(String date, String icon, String cond, String high_low) {
        this.date = date;
        this.icon = icon;
        this.condition = cond;
        this.temp_high_low = high_low;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTemp_high_low() {
        return temp_high_low;
    }

    public void setTemp_high_low(String temp_high_low) {
        this.temp_high_low = temp_high_low;
    }
}
