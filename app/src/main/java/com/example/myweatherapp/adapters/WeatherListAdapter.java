package com.example.myweatherapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myweatherapp.R;
import com.example.myweatherapp.objects.Weather;

import java.util.ArrayList;

/**
 * The adapter class for the RecyclerView, contains the daily Weather data.
 */
public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.WeatherViewHolder> {

    //Member variables
    private final LayoutInflater myInflater;
    private ArrayList<Weather> myWeather;
    private Context myContext;
    private int mExpandedPosition = -1;
    private int prevExpandedPosition = -1;

    /**
     * Constructor that passes in the daily Weather data and the context.
     * @param context Context of the application.
     * @param weatherData ArrayList containing the daily Weather data.
     */
    public WeatherListAdapter(Context context, ArrayList<Weather> weatherData) {
        myInflater = LayoutInflater.from(context);
        this.myWeather = weatherData;
        this.myContext = context;
    }

    /**
     * Required method for creating the viewholder objects.
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The newly created Viewholder.
     */
    @NonNull
    @Override
    public WeatherListAdapter.WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = myInflater.inflate(R.layout.list_item, parent, false);
        return new WeatherViewHolder(itemView);
    }

    /**
     * Required method that binds the data to the viewholder.
     * @param holder The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    @Override
    public void onBindViewHolder(@NonNull WeatherListAdapter.WeatherViewHolder holder, int position) {
        if(myWeather != null) {
            Weather current = myWeather.get(position);
            holder.myDayText.setText(current.getDate());
            String iconURL = "https://openweathermap.org/img/wn/" + current.getIcon() + ".png";
            Glide.with(myContext).load(iconURL).override(110, 110).into(holder.myConditionView);
            holder.myConditionText.setText(current.getCondition());
            holder.myHighLowText.setText(current.getTemp_high_low());
            holder.myCardPrecipitation.setText(current.getPrecipitation());
            holder.myCardHumidity.setText(current.getHumidity());
            holder.myCardCloudiness.setText(current.getCloudiness());
            holder.myCardWindSpeed.setText(current.getWind_speed());
        }

        boolean isExpanded = position==mExpandedPosition;
        holder.myCardDetails.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.itemView.setActivated(isExpanded);

        if(isExpanded) {
            prevExpandedPosition = position;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpandedPosition = isExpanded ? -1 : position;
                notifyItemChanged(prevExpandedPosition);
                notifyItemChanged(position);
            }
        });
    }

    /**
     * Required method for determining the size of the ArrayList.
     * @return Size of the Arraylist.
     */
    @Override
    public int getItemCount() {
        if(myWeather != null) {
            return myWeather.size();
        }
        else {
            return 0;
        }
    }

    /**
     * ViewHolder class that represents each row of data in the RecyclerView.
     */
    class WeatherViewHolder extends RecyclerView.ViewHolder {

        //Member variables for the Views.
        private TextView myDayText;
        private ImageView myConditionView;
        private TextView myConditionText;
        private TextView myHighLowText;
        private RelativeLayout myCardDetails;
        private TextView myCardPrecipitation;
        private TextView myCardHumidity;
        private TextView myCardCloudiness;
        private TextView myCardWindSpeed;

        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         * @param itemView the rootview of the list_item.xml layout file.
         */
        WeatherViewHolder(View itemView) {
            super(itemView);

            myDayText = itemView.findViewById(R.id.day_field);
            myConditionView = itemView.findViewById(R.id.condition_image);
            myConditionText = itemView.findViewById(R.id.condition_field);
            myHighLowText = itemView.findViewById(R.id.temp_high_low);
            myCardDetails = itemView.findViewById(R.id.card_detail);
            myCardPrecipitation = itemView.findViewById(R.id.card_precipitation);
            myCardHumidity = itemView.findViewById(R.id.card_humidity);
            myCardCloudiness = itemView.findViewById(R.id.card_cloudiness);
            myCardWindSpeed = itemView.findViewById(R.id.card_wind_speed);
        }
    }
}
