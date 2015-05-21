package koemdzhiev.com.stormy.weather;

import koemdzhiev.com.stormy.R;

/**
 * Created by koemdzhiev on 17/05/2015.
 */
public class Forecast {
    private Current mCurrent;
    private Hour[] mHourlyForecast;
    private Day[] mDailyForecast;

    public Current getCurrent() {
        return mCurrent;
    }

    public void setCurrent(Current current) {
        mCurrent = current;
    }

    public Hour[] getHourlyForecast() {
        return mHourlyForecast;
    }

    public void setHourlyForecast(Hour[] hourlyForecast) {
        mHourlyForecast = hourlyForecast;
    }

    public Day[] getDailyForecast() {
        return mDailyForecast;
    }

    public void setDailyForecast(Day[] dailyForecast) {
        mDailyForecast = dailyForecast;
    }

    public static int getIconId(String mString){
        int iconId = R.mipmap.clear_day;
        if(mString.equals("clear-day")){
            iconId = R.mipmap.clear_day;
        }else if(mString.equals("clear-night")){
            iconId = R.mipmap.clear_night;
        }else if (mString.equals("rain")) {
            iconId = R.mipmap.rain;
        }
        else if (mString.equals("snow")) {
            iconId = R.mipmap.snow;
        }
        else if (mString.equals("sleet")) {
            iconId = R.mipmap.sleet;
        }
        else if (mString.equals("wind")) {
            iconId = R.mipmap.wind;
        }
        else if (mString.equals("fog")) {
            iconId = R.mipmap.fog;
        }
        else if (mString.equals("cloudy")) {
            iconId = R.mipmap.cloudy;
        }
        else if (mString.equals("partly-cloudy-day")) {
            iconId = R.mipmap.partly_cloudy;
        }
        else if (mString.equals("partly-cloudy-night")) {
            iconId = R.mipmap.cloudy_night;
        }

        return iconId;
    }
}
