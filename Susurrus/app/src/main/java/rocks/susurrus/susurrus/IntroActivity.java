package rocks.susurrus.susurrus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;

import me.relex.circleindicator.CircleIndicator;
import rocks.susurrus.susurrus.fragments.IntroPageFourFragment;
import rocks.susurrus.susurrus.utils.Crypto;
import rocks.susurrus.susurrus.utils.RandomName;
import rocks.susurrus.susurrus.utils.Settings;
import rocks.susurrus.susurrus.views.adapters.IntroPageAdapter;

public class IntroActivity extends FragmentActivity {

    final private String logIndicator = "IntroActivity";

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
    private CircleIndicator introIndicator;
    private RadioGroup radioIndicators;

    /**
     * Data
     */
    private String userPassword;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // set content
        setContentView(R.layout.activity_intro);

        // Instantiate ViewPager and PagerAdapter.
        introPager = (ViewPager) findViewById(R.id.viewpager_unselected_background);
        introIndicator = (CircleIndicator) findViewById(R.id.indicator_unselected_background);
        introPagerAdapter = new IntroPageAdapter(getSupportFragmentManager());
        //introPager.setOffscreenPageLimit(introPagerAdapter.getCount());
        introPager.setAdapter(introPagerAdapter);
        introIndicator.setViewPager(introPager);

        // start listening for page changes
        introIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                // call our handler for position changes.
                onPositionChanged(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // get all elements and add click events if necessary
        buttonNext = (Button) findViewById(R.id.intro_button_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // user clicked next button, increment position
                slidePos = introPager.getCurrentItem() + 1;
                onControlButton();
            }
        });
        buttonPrev = (Button) findViewById(R.id.intro_button_prev);
        buttonPrev.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // the user clicked the prev button, decrement position
                slidePos = introPager.getCurrentItem() - 1;
                onControlButton();
            }
        });
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
        Log.d(logIndicator, "onControlButton(): " + slidePos);

        if(slidePos != 4) {
            introPager.setCurrentItem(slidePos, true);
        }
        else {
            // last step, check passwords and finish intro
            Log.d(logIndicator, "case 4");

            String username = checkUsername();
            String password = checkPassword();

            if(password == null) {
                Log.e(logIndicator, "return");
                return;
            }

            finishIntro(password, username);
        }
    }

    /**
     * Is called whenever the viewPager's page changes.
     * Is used for changing the position indicatators.
     * @param currentPos
     */
    private void onPositionChanged(int currentPos) {
        //Log.d(logIndicator, "onPositionChanged() " + currentPos);

        switch(currentPos) {
            case 0:
                // default start position, hide prev-button
                buttonPrev.animate()
                        .alpha(0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // after fadeOut, make button invisible
                                buttonPrev.setVisibility(View.INVISIBLE);
                            }
                        });
                break;
            case 1:
                // make the prev-button visible & animate it in
                buttonPrev.setVisibility(View.VISIBLE);
                buttonPrev.animate()
                        .alpha(1f)
                        .setListener(null); // clears previously set listeners
                break;
            case 2:
                // insert button next text, if the user clicked the buttonPrev
                buttonNext.setText(getString(R.string.intro_button_next));
                break;
            case 3:
                // insert button ready text, we are on the last step
                buttonNext.setText(getString(R.string.intro_button_ready));
                break;
        }
    }

    /**
     * Fetches and validates the entered passwords inside fragment four.
     * @return Entered password-string or "null" if both entered passwords do not match.
     */
    private String checkPassword() {
        // get the entered passwords
        EditText passwordInputOne = (EditText) introPager
                .findViewById(R.id.intro_four_passwort_one);
        EditText passwordInputTwo = (EditText) introPager
                .findViewById(R.id.intro_four_passwort_two);

        // do the passwords match?
        String passwordValueOne = passwordInputOne.getText().toString().trim();
        String passwordValueTwo = passwordInputTwo.getText().toString().trim();
        if(passwordValueOne.equals(passwordValueTwo) && !passwordValueOne.isEmpty()) {
            return passwordValueOne;
        }
        else {
            return null;
        }
    }

    /**
     * Fetches and validates the entered username inside fragment three.
     * @return Entered username-string.
     */
    private String checkUsername() {
        // get the entered username
        EditText usernameInput = (EditText) introPager
                .findViewById(R.id.username_input_name);

        // do the passwords match?
        String usernameInputValue = usernameInput.getText().toString().trim();
        if(!usernameInputValue.isEmpty()) {
            return usernameInputValue;
        }
        else {
            // no valid username entered, generate a new random name
            return new RandomName(this).generate();
        }
    }

    /**
     * Generates all needed setting-values, inserts them inside the user's encrypted settings
     * and redirects the user to the MainActivity.
     * @param _password Application password string.
     * @param _username Username string.
     */
    private void finishIntro(String _password, String _username) {

        // give feedback
        MaterialDialog finishDialog = new MaterialDialog.Builder(this)
            .title(R.string.intro_finish_dialog_title)
            .content(R.string.intro_finish_dialog_content)
            .progress(true, 0)
            .show();

        // create public/private-Key for the user
        ArrayList keys = Crypto.generateKeys();
        String privateKeyString = Crypto.keyToString((PrivateKey) keys.get(0));
        String publicKeyString = Crypto.keyToString((PublicKey) keys.get(1));

        // save all needed data in a new settings files
        Settings.setupSettings(this, _password, _username, publicKeyString, privateKeyString);
        Settings.disableFirstRun(this);

        // redirect to the MainActivity
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        finishDialog.dismiss();
        startActivity(mainIntent);

    }
}
