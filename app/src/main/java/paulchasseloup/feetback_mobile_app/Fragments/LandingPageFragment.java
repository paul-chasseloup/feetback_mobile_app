package paulchasseloup.feetback_mobile_app.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import paulchasseloup.feetback_mobile_app.LandingPageActivity;
import paulchasseloup.feetback_mobile_app.R;

public class LandingPageFragment extends Fragment {

    public static LandingPageFragment newInstance() {
        return (new LandingPageFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_landing,
                container, false);

        Intent view = new Intent(getActivity(), LandingPageActivity.class);
        startActivity(view);

        return rootView;



    }
}
