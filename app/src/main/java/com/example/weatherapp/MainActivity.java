package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private EditText edCityName;
    private ImageView imgWeather;
    private TextView txtTemp, txtCity, txtDate, txtDesc ;
    private ImageView imgSearch, maps;
    LinearLayout linear;
    private String API_KEY = "9579de45a47701abfb63dfb0f7d6bb9c";
    private RecyclerView recyclerView ;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        edCityName = findViewById(R.id.edCityNameID);
        imgWeather = findViewById(R.id.imgWeatherID);
        txtTemp = findViewById(R.id.txtTempID);
        txtCity = findViewById(R.id.txtCityID);
        txtDate = findViewById(R.id.txtDateID);
        txtDesc = findViewById(R.id.txtDescID);
        imgSearch = findViewById(R.id.imgSearchID);
        maps = findViewById(R.id.mapsID);
        recyclerView = findViewById(R.id.rcDailyWeatherID);
        linear = findViewById(R.id.linearID);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));


        // TODO: When User Click On Button for Search Weather Of Her City
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String city = edCityName.getText().toString();

                if (city.isEmpty())
                    Toast.makeText(MainActivity.this, "Please enter your city name", Toast.LENGTH_SHORT).show();
                else
                {
                    loadWeatherByCityName(city);

                    // TODO: Show the ad
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(MainActivity.this);
                    } else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    }
                }
            }
        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        // TODO: Banner Ads
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // TODO: Interstitial Ads
        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                    }
                });


    }



     private void loadWeatherByCityName(String city)
    {
        Ion.with(this)
                .load("http://api.openweathermap.org/data/2.5/weather?q="+city+"&&units=metric&appid="+API_KEY)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null)
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        else
                        {
                            // TODO: Convert Json to Java
                            linear.setBackground(getDrawable(R.drawable.back_header));
                            maps.setBackgroundResource(R.drawable.maps);

                            JsonObject main = result.get("main").getAsJsonObject();
                            int temp = main.get("temp").getAsInt();
                            txtTemp.setText(temp + "°");

                            JsonObject sys = result.get("sys").getAsJsonObject();
                            String country = sys.get("country").getAsString();
                            txtCity.setText(city +", "+ country);

                            Long dt = result.get("dt").getAsLong();
                            Date date = new Date(dt*1000);
                            DateFormat dateFormat = new SimpleDateFormat("E, dMMM", Locale.ENGLISH);
                            txtDate.setText(dateFormat.format(date));

                            JsonArray weather = result.get("weather").getAsJsonArray();
                            String icon = weather.get(0).getAsJsonObject().get("icon").getAsString();
                            Picasso.get().load("https://openweathermap.org/img/w/"+icon+".png").into(imgWeather);

                            String desc = weather.get(0).getAsJsonObject().get("description").getAsString();
                            txtDesc.setText(desc);

                            JsonObject coord = result.get("coord").getAsJsonObject();
                            double lon = coord.get("lon").getAsDouble();
                            double lat = coord.get("lat").getAsDouble();
                            loadDailyForecast(lon , lat);
                        }
                    }
                });
    }

    private void loadDailyForecast(double lon, double lat)
    {

        Ion.with(this)
                .load("https://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lon+"&exclude=current,minutely,hourly&&units=metric&appid="+API_KEY)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null)
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        else
                        {
                            List<Model> modelList = new ArrayList<>();

                            String timeZone = result.get("timezone").getAsString();

                            JsonArray daily = result.get("daily").getAsJsonArray();

                            for (int i = 1 ; i< daily.size(); i++){
                                Long date = daily.get(i).getAsJsonObject().get("dt").getAsLong();

                                int maxTemp = daily.get(i).getAsJsonObject().get("temp").getAsJsonObject().get("max").getAsInt();
                                int minTemp = daily.get(i).getAsJsonObject().get("temp").getAsJsonObject().get("min").getAsInt();

                                String icon = daily.get(i).getAsJsonObject().get("weather").getAsJsonArray().get(0).getAsJsonObject().get("icon").getAsString();

                                modelList.add(new Model(date , timeZone , maxTemp , minTemp , icon ));
                            }

                            MyAdapter adapter = new MyAdapter(modelList , MainActivity.this);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                });
    }

}