package rocks.susurrus.susurrus.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import rocks.susurrus.susurrus.R;

public class IntroPageOneFragment extends Fragment {

    private final String logIndictaor = "IntroPageOneFragment";

    private View rootView;
    private ViewGroup upperContainer;
    private ImageView logo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_intro_page_one, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setLayout(view);
        //startAnimation();
    }


    private void setLayout(View view) {
        upperContainer = (ViewGroup) view.findViewById(R.id.upper_container);
        logo = (ImageView) view.findViewById(R.id.logo);
    }

    private void startAnimation() {
        logo.setVisibility(View.INVISIBLE);
    }

}
