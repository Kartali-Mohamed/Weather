package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    List<Model> myList;
    Context context;

    public MyAdapter(List<Model> myList , Context context)
    {
        this.myList = myList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_item , parent , false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        Model model = myList.get(position);

        holder.maxTemp.setText(String.valueOf(model.getMaxTemp())+"°");
        holder.minTemp.setText("/"+String.valueOf(model.getMinTemp())+"°");
        Picasso.get().load("https://openweathermap.org/img/w/"+model.getIcon()+".png").into(holder.imgMinWeather);

        Date date = new Date(model.getDate()*1000);
        DateFormat dateFormat = new SimpleDateFormat("dMMM, E", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone(model.getTimezone()));
        holder.txtDate.setText(dateFormat.format(date));
    }

    @Override
    public int getItemCount() {
        return myList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView txtDate , maxTemp, minTemp;
        ImageView imgMinWeather;

        public MyViewHolder(@NonNull  View itemView) {
            super(itemView);

            txtDate = itemView.findViewById(R.id.txtDateID);
            maxTemp = itemView.findViewById(R.id.maxTempID);
            minTemp = itemView.findViewById(R.id.minTempID);
            imgMinWeather = itemView.findViewById(R.id.imgMinWeatherID);
        }
    }
}
