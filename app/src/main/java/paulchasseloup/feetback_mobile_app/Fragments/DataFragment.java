package paulchasseloup.feetback_mobile_app.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import paulchasseloup.feetback_mobile_app.R;

public class DataFragment extends Fragment {

    public static DataFragment newInstance() {
        return (new DataFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_data,
                container, false);

      //  Intent view = new Intent(getActivity(), DataActivity.class);
       // startActivity(view);

        return rootView;



    }
}
