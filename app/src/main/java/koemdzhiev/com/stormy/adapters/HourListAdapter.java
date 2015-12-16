package koemdzhiev.com.stormy.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import koemdzhiev.com.stormy.R;
import koemdzhiev.com.stormy.weather.Hour;

/**
 * Created by koemdzhiev on 21/05/2015.
 */
public class HourListAdapter extends BaseAdapter {

    private Context mContext;
    private Hour[] mHours;

    public HourListAdapter(Context context, Hour[] hours) {
        mContext = context;
        mHours = hours;
    }

    @Override
    public int getCount() {
        return mHours.length;
    }

    @Override
    public Object getItem(int position) {
        return mHours[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;//we arent doing to use this. Tag items for easy reference
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            //brand new
            convertView = LayoutInflater.from(mContext).inflate(R.layout.hourly_list_item,null);
            holder = new ViewHolder();
            holder.mTimeLabel = (TextView) convertView.findViewById(R.id.timeLabel);
            holder.mSummaryLabel = (TextView) convertView.findViewById(R.id.summaryLabel);
            holder.mTemperatureLabel= (TextView) convertView.findViewById(R.id.temperatureLabel);
            holder.mIconImageView = (ImageView) convertView.findViewById(R.id.iconImageView);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        Hour hour = mHours[position];
        holder.mTimeLabel.setText(hour.getHour());
        holder.mSummaryLabel.setText(hour.getSummary());
        holder.mTemperatureLabel.setText(hour.getTemperature() + "");
        holder.mIconImageView.setImageResource(hour.getIconId());

        return convertView;

    }

    private static class ViewHolder {
         TextView mTimeLabel;
         TextView mSummaryLabel;
         TextView mTemperatureLabel;
         ImageView mIconImageView;
    }
}
