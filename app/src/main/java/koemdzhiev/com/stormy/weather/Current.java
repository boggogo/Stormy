package koemdzhiev.com.stormy.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by koemdzhiev on 15/05/2015.
 */
public class Current {
    private String mIcon;
    private long mTime;
    private double mTemperature;
    private double mHumidity;
    private double mPrecipChange;
    private String mSummery;
    private String mTimeZone;
    private double mWindSpeed;
    private String locationName;
    private double mFeelsLike;

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getWindSpeed() {
        return mWindSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        mWindSpeed = windSpeed;
    }

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public int getIconId(){
        return Forecast.getIconId(mIcon);
    }

    public long getTime() {
        return mTime;
    }

    public String getFormattedTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.UK);
        formatter.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        Date dateTime = new Date(getTime() * 1000);
        String timeString = formatter.format(dateTime);

        return timeString;
    }


    public void setTime(long time) {
        mTime = time;
    }

    public int getTemperature() {
        return (int)Math.round(mTemperature);
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public int getHumidity() {
        return (int) (mHumidity * 100);
    }

    public void setHumidity(double humidity) {
        mHumidity = humidity;
    }

    public int getPrecipChange() {
        double precPercentage = mPrecipChange * 100;

        return (int)Math.round(precPercentage);
    }

    public void setPrecipChange(double precipChange) {
        mPrecipChange = precipChange;
    }

    public String getSummery() {
        return mSummery;
    }

    public void setSummery(String summery) {
        mSummery = summery;
    }

    public double getFeelsLike() {
        return mFeelsLike;
    }

    public void setFeelsLike(double feelsLike) {
        mFeelsLike = feelsLike;
    }
}
