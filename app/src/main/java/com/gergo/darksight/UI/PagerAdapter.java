package com.gergo.darksight.UI;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
    private int numberOfTabs;
    private Activity act;

    public PagerAdapter(FragmentManager fm, int numberOfTabs, Activity act) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
        this.act = act;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                LeftTab leftTab = new LeftTab();
                return leftTab;
            case 1:
                CenterTab centerTab = new CenterTab();
                return centerTab;
            case 2:
                RightTab rightTab = new RightTab();
                return rightTab;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
