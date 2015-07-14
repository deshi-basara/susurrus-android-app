package rocks.susurrus.susurrus.fragments;


import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import rocks.susurrus.susurrus.R;
import rocks.susurrus.susurrus.utils.RandomName;

/**
 * A simple {@link Fragment} subclass.
 */
public class IntroPageThreeFragment extends Fragment {

    /**
     * Views
     */
    EditText inputName;
    ImageButton buttonRandomName;


    public IntroPageThreeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_intro_page_three, container, false);

        setView(view);
        insertRandomName();

        return view;
    }

    /**
     * Set all view elements.
     * @param _view Fragment view.
     */
    private void setView(View _view) {
        // get views
        this.inputName = (EditText) _view.findViewById(R.id.username_input_name);
        this.buttonRandomName = (ImageButton) _view.findViewById(R.id.username_refresh_button);

        // set listeners
        this.buttonRandomName.setOnClickListener(this.randomListener);
    }

    /**
     * Helper method for inserting a random name into username input-field.
     */
    private void insertRandomName() {
        // get a random name & set it
        RandomName randName = new RandomName(getActivity());
        inputName.setText(randName.generate());
    }

    /**
     * OnClickListener: buttonRandomName.
     * Refreshes the random name.
     */
    private View.OnClickListener randomListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            insertRandomName();
        }
    };

}
