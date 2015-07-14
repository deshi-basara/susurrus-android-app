package rocks.susurrus.susurrus.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import rocks.susurrus.susurrus.IntroActivity;
import rocks.susurrus.susurrus.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class IntroPageFourFragment extends Fragment {

    /**
     * Views
     */
    private EditText passwordInputOne;
    private EditText passwordInputTwo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_intro_page_four, container, false);

        setView(view);

        return view;
    }

    /**
     * Sets the view.
     * @param _view Fragment view.
     */
    private void setView(View _view) {
        this.passwordInputOne = (EditText) _view.findViewById(R.id.intro_four_passwort_one);
        this.passwordInputTwo = (EditText) _view.findViewById(R.id.intro_four_passwort_two);
    }

}
