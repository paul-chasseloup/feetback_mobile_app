package paulchasseloup.feetback_mobile_app.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.sample.LoginQuery;

import org.jetbrains.annotations.NotNull;

import paulchasseloup.feetback_mobile_app.ApolloConnector;
import paulchasseloup.feetback_mobile_app.LandingPageActivity;
import paulchasseloup.feetback_mobile_app.R;
import paulchasseloup.feetback_mobile_app.registerActivity;

public class LandingPageFragment extends Fragment {

    private EditText mEmail;
    private EditText mPassword;
    private TextView mRegisterLink;
    private TextView labelMessage;
    private Button loginButton;

    public static LandingPageFragment newInstance() {
        return (new LandingPageFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_landing,
                container, false);

       // Intent view = new Intent(getActivity(), LandingPageActivity.class);
        //startActivity(view);

        mEmail = (EditText) rootView.findViewById(R.id.activity_main_email);
        mPassword = (EditText) rootView.findViewById(R.id.activity_main_password);
        labelMessage = (TextView) rootView.findViewById(R.id.labelMessage);
        mRegisterLink = (TextView) rootView.findViewById(R.id.activity_main_register_link);
        loginButton = (Button) rootView.findViewById(R.id.activity_main_login_btn);

        labelMessage.setText("Bienvenue!");

        mRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Intent registerActivity = new Intent(LandingPageActivity.this, paulchasseloup.feetback_mobile_app.registerActivity.class);
                //startActivity(registerActivity);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               //Login();
                                               labelMessage.setText("Correct Token");
                                               Fragment fragmentDataActivity = DataFragment.newInstance();
                                               FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                               fragmentTransaction.replace(R.id.activity_landing_drawer_layout, fragmentDataActivity);
                                               fragmentTransaction.commit();
                                           }
                                       }
        );

        return rootView;

    }

    /**
     * Login
     *
     */
    public void Login() {

        ApolloConnector.setupApollo().query(
                LoginQuery
                        .builder()
                        .email(mEmail.getText().toString())
                        .password(mPassword.getText().toString())
                        .build())
                .enqueue(new ApolloCall.Callback<LoginQuery.Data>() {


                    @Override
                    public void onResponse(@NotNull Response<LoginQuery.Data> response) {
                        Log.d("TAG", " IN RESPONSE");
                        labelMessage.setText(response.data().login().message());
                        // Correct credentials
                        if (response.data().login().token() != null) {
/*
                            Intent dataActivity = new Intent(LandingPageActivity.this, DataActivity.class);
                            dataActivity.putExtra("userId", response.data().login().user().id().toString());
                            dataActivity.putExtra("token", response.data().login().token());
                            startActivity(dataActivity);
*/
                            Log.d("TAG", "I'm IN");
                            Fragment fragmentDataActivity = DataFragment.newInstance();
                            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.activity_landing_drawer_layout, fragmentDataActivity);
                            fragmentTransaction.commit();
                            Log.d("TAG", "I'm OUT");

                        }
                        //Log.d(TAG, "Response: " + response.data().login());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {

                        Log.d("TAG", " IN Failure");
                        labelMessage.setText("Server Error!");
                        //Log.d(TAG, "Exception " + e.getMessage(), e);
                    }
                });
    }

}
