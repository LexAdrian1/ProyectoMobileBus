package com.example.toshibask.myapplicationbus;

import com.example.toshibask.myapplicationbus.Remote.IGoogleApi;
import com.example.toshibask.myapplicationbus.Remote.RetrofitClient;

public class Common {
    public static final String baseURL="https://www.googleapis.com/";
    public static IGoogleApi getGoogleApi()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleApi.class);
    }
}
