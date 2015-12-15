package koemdzhiev.com.stormy.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import koemdzhiev.com.stormy.ui.Current_forecast_fragment;
import koemdzhiev.com.stormy.ui.Daily_forecast_fragment;
import koemdzhiev.com.stormy.ui.Hourly_forecast_fragment;

/**
 * Created by koemdzhiev on 14/12/15.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private Current_forecast_fragment mCurrent_forecast_fragment;
    private Hourly_forecast_fragment mHourly_forecast_fragment;
    private Daily_forecast_fragment mDaily_forecast_fragment;
    CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb,Current_forecast_fragment current_fragment,
                            Hourly_forecast_fragment hourly_fragment,
                            Daily_forecast_fragment daily_fragment) {
        super(fm);
        this.mCurrent_forecast_fragment = current_fragment;
        this.mHourly_forecast_fragment = hourly_fragment;
        this.mDaily_forecast_fragment = daily_fragment;
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        if(position == 0) // if the position is 0 we are returning the First tab
        {
            return this.mCurrent_forecast_fragment;
        }
        else if (position == 1)            // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
        {
            return this.mHourly_forecast_fragment;
        }else {
            return this.mDaily_forecast_fragment;
        }

    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}