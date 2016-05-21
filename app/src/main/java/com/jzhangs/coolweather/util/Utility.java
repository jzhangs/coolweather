package com.jzhangs.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.jzhangs.coolweather.db.CoolWeatherDB;
import com.jzhangs.coolweather.model.City;
import com.jzhangs.coolweather.model.County;
import com.jzhangs.coolweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {

    // https://api.heweather.com/x3/weather?cityid=countyCode&key=XXXXXXXXX
    public static final String CITYID_URL = "https://api.heweather.com/x3/weather?cityid=";
    public static final String API_KEY = "&key=4e8f0b3c96454a44a8bff0a21b3d4a73";

    /**
     * province info, formatted as "1|北京|10101"
     */
    public synchronized static boolean handleProvinces(CoolWeatherDB coolWeatherDB,
                                                       String content) {
        if (!TextUtils.isEmpty(content)) {
            String[] allProvinces = content.split("\n");
            if (allProvinces.length > 0) {
                for (String p : allProvinces) {
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceName(array[1]);
                    province.setProvinceCode(array[2]);
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * City info, formatted as "1|北京|1010101|10101"
     */
    public synchronized static boolean handleCities(CoolWeatherDB coolWeatherDB,
                                                    String content, String provinceCode) {
        if (!TextUtils.isEmpty(content)) {
            String[] allCities = content.split("\n");
            if (allCities.length > 0) {
                for (String c : allCities) {
                    String[] array = c.split("\\|");
                    if (provinceCode.equals(array[3])) {
                        City city = new City();
                        city.setCityName(array[1]);
                        city.setCityCode(array[2]);
                        city.setProvinceCode(array[3]);

                        coolWeatherDB.saveCity(city);
                    }

                }
                return true;
            }
        }
        return false;
    }

    /**
     * County info, formatted as "1|北京|CN101010100|1010101"
     */
    public synchronized static boolean handleCounties(CoolWeatherDB coolWeatherDB,
                                                      String content, String cityCode) {
        if (!TextUtils.isEmpty(content)) {
            String[] allCounties = content.split("\n");
            if (allCounties.length > 0) {
                for (String c : allCounties) {
                    String[] array = c.split("\\|");
                    if (cityCode.equals(array[3])) {
                        County county = new County();
                        county.setCountyName(array[1]);
                        county.setCountyCode(array[2]);
                        county.setCityCode(array[3]);

                        coolWeatherDB.saveCounty(county);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static String loadRawFile(Context context, int id) {
        InputStream in = context.getResources().openRawResource(id);
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    /**
     * Parse json data from server, and save to local storage. sample json data in
     * res/raw/county_info.json or http://www.heweather.com/documents/api-sample,
     * formatted as below:
     *   {
     *     "HeWeather data service 3.0": [
     *       {
     *         "api": {...},
     *         "basic": {...},
     *         "daily_forecast": [...],
     *         "hourly_forecast": [...],
     *         "now": {"fl": "20"...},
     *         "status": "ok",
     *         "suggestion": {...}
     *       }
     *     ]
     *   }
     */
    public static void handleWeatherResponse(Context context, String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONArray("HeWeather data service 3.0")
                    .getJSONObject(0);
            String cityName = weatherInfo.getJSONObject("basic").getString("city");
            String cityId = weatherInfo.getJSONObject("basic").getString("id");

            JSONObject tempToday = weatherInfo.getJSONArray("daily_forecast")
                    .getJSONObject(0).getJSONObject("tmp");
            String temp1 = tempToday.getString("min");
            String temp2 = tempToday.getString("max");

            JSONObject currentCond = weatherInfo.getJSONObject("now").getJSONObject("cond");
            String weatherDesp = currentCond.getString("txt");
            String publishTime = weatherInfo.getJSONObject("basic").getJSONObject("update")
                    .getString("loc");

            saveWeatherInfo(context, cityName, cityId, temp1, temp2,
                    weatherDesp, publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save weather info to shared preferences
     */
    public static void saveWeatherInfo(Context context, String cityName, String cityId,
                                       String temp1, String temp2, String weatherDesp,
                                       String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                .edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("city_id", cityId);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time", publishTime);
        editor.putString("current_date", sdf.format(new Date()));
        editor.apply();
    }
}
