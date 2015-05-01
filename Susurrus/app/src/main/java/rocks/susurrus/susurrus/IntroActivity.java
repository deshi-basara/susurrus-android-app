package rocks.susurrus.susurrus;



import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioGroup;

import me.relex.circleindicator.CircleIndicator;
import rocks.susurrus.susurrus.views.adapters.IntroPageAdapter;


public class IntroActivity extends FragmentActivity {

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager introPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter introPagerAdapter;

    /**
     * Other global attributes
     */
    private int slidePos = 0;
    private Button buttonPrev;
    private Button buttonNext;
    private RadioGroup radioIndicators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // set content
        setContentView(R.layout.activity_intro);

        // Instantiate ViewPager and PagerAdapter.
        introPager =(ViewPager) findViewById(R.id.viewpager_unselected_background);
        CircleIndicator introIndicator = (CircleIndicator) findViewById(R.id.indicator_unselected_background);
        introPagerAdapter = new IntroPageAdapter(getSupportFragmentManager());
        introPager.setAdapter(introPagerAdapter);
        introIndicator.setViewPager(introPager);

        // start listening for page changes
        /*introPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                // call our handler for position changes.
                onPositionChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });*/

        // get all elements and add click events if necessary
        buttonNext = (Button) findViewById(R.id.intro_button_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // the user clicked the next button, increment position
                slidePos++;
                onControlButton();
            }
        });
        /*buttonPrev = (Button) findViewById(R.id.intro_button_prev);
        buttonPrev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // the user clicked the prev button, decrement position
                slidePos--;
                onControlButton();
            }
        });*/
    }

    @Override
    public void onBackPressed() {
        if (introPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            introPager.setCurrentItem(introPager.getCurrentItem() - 1);
        }
    }

    /**
     * Is called whenever the prev-/next-button is clicked.
     * Sets the viewPager page to the current slidePos.
     */
    private void onControlButton() {
        Log.d("IntroAcitvity", "Control button clicked");
        //introPager.setCurrentItem(slidePos, true);
    }

    /**
     * Is called whenever the viewPager's page changes.
     * Is used for changing the position indicatators.
     * @param currentPos
     */
    private void onPositionChanged(int currentPos) {
        Log.w("IntroActivity", String.valueOf(currentPos));

        /*switch(currentPos) {
            case 0:
                // default start position, hide prev-button
                buttonPrev.setVisibility(View.INVISIBLE);
                // change the indicator dot
                radioIndicators.check(R.id.intro_radio_0);
                break;
            case 1:
                // make the prev-button visible
                buttonPrev.setVisibility(View.VISIBLE);
                // change the indicator dot
                radioIndicators.check(R.id.intro_radio_1);
                break;
            case 2:
                radioIndicators.check(R.id.intro_radio_2);
                // insert button next text, if the user clicked the buttonPrev
                buttonNext.setText(getString(R.string.intro_button_next));
                break;
            case 3:
                radioIndicators.check(R.id.intro_radio_3);
                // insert button ready text, we are on the last step
                buttonNext.setText(getString(R.string.intro_button_ready));
                break;
        }*/
    }
}
