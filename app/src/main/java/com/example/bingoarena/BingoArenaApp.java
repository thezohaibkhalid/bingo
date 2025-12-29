package com.example.bingoarena;

import android.app.Application;
import com.example.bingoarena.network.RetrofitClient;

public class BingoArenaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitClient.init(this);
    }
}
