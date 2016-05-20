package com.jzhangs.coolweather.util;

import android.content.Context;
import android.text.TextUtils;

import com.jzhangs.coolweather.db.CoolWeatherDB;
import com.jzhangs.coolweather.model.City;
import com.jzhangs.coolweather.model.County;
import com.jzhangs.coolweather.model.Province;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utility {
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
}
