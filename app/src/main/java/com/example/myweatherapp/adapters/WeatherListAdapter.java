package com.example.myweatherapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myweatherapp.R;
import com.example.myweatherapp.objects.Weather;

import java.util.ArrayList;

public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.WeatherViewHolder> {

    private final LayoutInflater myInflater;
    private ArrayList<Weather> myWeather;
    private Context myContext;

    public WeatherListAdapter(Context context, ArrayList<Weather> weatherData) {
        myInflater = LayoutInflater.from(context);
        this.myWeather = weatherData;
        this.myContext = context;
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
            String iconURL = "https://openweathermap.org/img/wn/" + current.getIcon() + ".png";
            Glide.with(myContext).load(iconURL).override(110, 110).into(holder.myConditionView);
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
        private ImageView myConditionView;
        private TextView myConditionText;
        private TextView myHighLowText;

        private WeatherViewHolder(View itemView) {
            super(itemView);

            myDayText = itemView.findViewById(R.id.day_field);
            myConditionView = itemView.findViewById(R.id.condition_image);
            myConditionText = itemView.findViewById(R.id.condition_field);
            myHighLowText = itemView.findViewById(R.id.temp_high_low);
        }
    }
}
