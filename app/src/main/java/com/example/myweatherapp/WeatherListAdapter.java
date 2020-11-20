package com.example.myweatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.WeatherViewHolder> {

    private final LayoutInflater myInflater;
    private List<Weather> myWeather;

    WeatherListAdapter(Context context) {
        myInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public WeatherListAdapter.WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = myInflater.inflate(R.layout.list_item, parent, false);
        return new WeatherViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherListAdapter.WeatherViewHolder holder, int position) {
        if(myWeather != null) {
            Weather current = myWeather.get(position);
            holder.myDayText.setText(current.getDate());
            holder.myConditionText.setText(current.getCondition());
            holder.myHighLowText.setText(current.getTemp_high_low());
        }
    }

    @Override
    public int getItemCount() {
        if(myWeather != null) {
            return myWeather.size();
        }
        else {
            return 0;
        }
    }

    class WeatherViewHolder extends RecyclerView.ViewHolder {

        private TextView myDayText;
        private TextView myConditionText;
        private TextView myHighLowText;

        private WeatherViewHolder(View itemView) {
            super(itemView);

            myDayText = itemView.findViewById(R.id.day_field);
            myConditionText = itemView.findViewById(R.id.condition_field);
            myHighLowText = itemView.findViewById(R.id.temp_high_low);
        }
    }
}
