package koemdzhiev.com.stormy.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by koemdzhiev on 17/05/2015.
 */
public class Day implements Parcelable {
    public static final Creator<Day> CREATOR = new Creator<Day>() {
        @Override
        public Day createFromParcel(Parcel source) {
            return new Day(source);
        }

        @Override
        public Day[] newArray(int size) {
            return new Day[size];
        }
    };
    ;
    private long mTime;
    private String mSummary;
    private double mTemperatureMax;
    private String mIcon;
    private String mTimezone;
    private long mSunRiseTime;
    private long mSunsetTime;
    private double mWindSpeed;
    private double mPressure;


    //default constructor
    public Day() {

    }

    private Day(Parcel in) {
        mTime = in.readLong();
        mSummary = in.readString();
        mTemperatureMax = in.readDouble();
        mIcon = in.readString();
        mTimezone = in.readString();
        mSunRiseTime = in.readLong();
        mSunsetTime = in.readLong();
        mWindSpeed = in.readDouble();
        mPressure = in.readDouble();
    }

    public double getPressure() {
        return mPressure;
    }

    public void setPressure(double mPressure) {
        this.mPressure = mPressure;
    }

    public double getWindSpeed() {
        return mWindSpeed;
    }

    public void setWindSpeed(double mWindSpeed) {
        this.mWindSpeed = mWindSpeed;
    }

    public long getSunsetTime() {
        return mSunsetTime;
    }

    public void setSunsetTime(long mSunsetTime) {
        this.mSunsetTime = mSunsetTime;
    }

    public long getSunRiseTime() {
        return mSunRiseTime;
    }

    public void setSunriseTime(long mSunRiseTime) {
        this.mSunRiseTime = mSunRiseTime;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public int getTemperatureMax() {

        return Math.round(((int) mTemperatureMax));
    }

    public void setTemperatureMax(double temperatureMax) {
        mTemperatureMax = temperatureMax;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public void setTimezone(String timezone) {
        mTimezone = timezone;
    }

    public int getIconId(){
        return Forecast.getIconId(mIcon);
    }

    public String getDayOfTheWeek() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE", Locale.UK);
        formatter.setTimeZone(TimeZone.getTimeZone(mTimezone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    public String getFormattedSunRiseTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.UK);
        formatter.setTimeZone(TimeZone.getTimeZone(getTimezone()));
        Date dateTime = new Date(getSunRiseTime() * 1000);
        String timeString = formatter.format(dateTime);

        return timeString;
    }
// TODO Try to make formatter local variable

    public String getFormattedSunSetTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.UK);
        formatter.setTimeZone(TimeZone.getTimeZone(getTimezone()));
        Date dateTime = new Date(getSunsetTime() * 1000);
        String timeString = formatter.format(dateTime);

        return timeString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //wraps the data to be parsed
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTime);
        dest.writeString(mSummary);
        dest.writeDouble(mTemperatureMax);
        dest.writeString(mIcon);
        dest.writeString(mTimezone);
        dest.writeLong(mSunRiseTime);
        dest.writeLong(mSunsetTime);
        dest.writeDouble(mWindSpeed);
        dest.writeDouble(mPressure);
    }
}
