package rocks.susurrus.susurrus.fragments;


import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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


    public IntroPageThreeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_intro_page_three, container, false);

        // get input field
        inputName = (EditText) view.findViewById(R.id.intro_three_input_name);

        insertRandomName();

        return view;
    }

    /**
     * Helper method for inserting a random name into username input-field.
     */
    private void insertRandomName() {
        // get a random name & set it
        RandomName randName = new RandomName(getActivity());
        inputName.setText(randName.generate());
    }

}
