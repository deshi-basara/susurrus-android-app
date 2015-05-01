package rocks.susurrus.susurrus.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import rocks.susurrus.susurrus.fragments.IntroPageFragment;

/**
 * Created by simon on 01.05.15.
 */
public class IntroPageAdapter extends FragmentStatePagerAdapter {

    /**
     * Intro page count
     */
    final static int NUM_PAGES = 4;

    /**
     * Constructor
     * @param fm
     */
    public IntroPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // for each different position, return another fragment with different layout
        switch(position) {
            case 0:
                return new IntroPageFragment();
            case 1:
                return new IntroPageFragment();
            default:
                return new IntroPageFragment();
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
