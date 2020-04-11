package paulchasseloup.feetback_mobile_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.sample.LoginQuery;
import com.apollographql.apollo.sample.RegisterMutation;
import com.apollographql.apollo.sample.type.UserInput;

import org.jetbrains.annotations.NotNull;

public class registerActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private EditText mName;
    private EditText mLastName;
    private Button mRegisterBtn;
    private TextView mLandingPageLink;
    private TextView mMessage;

    private String message;

    private final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mEmail = (EditText) findViewById(R.id.register_activity_email);
        mPassword = (EditText) findViewById(R.id.register_activity_password);
        mName = (EditText) findViewById(R.id.register_activity_name);
        mLastName = (EditText) findViewById(R.id.register_activity_lastName);
        mRegisterBtn = (Button) findViewById(R.id.register_activity_register_btn);
        mLandingPageLink = (TextView) findViewById(R.id.register_activity_landingpage_link);
        mMessage = (TextView) findViewById(R.id.register_activity_message);

        mLandingPageLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent landingpageActivity = new Intent(registerActivity.this, landingpageActivity.class);
                startActivity(landingpageActivity);
            }
        });

    }

    /**
     * Register new user
     *
     * @param view
     */
    public void RegisterUser(View view) {

        final UserInput userInput = UserInput
                .builder()
                .email(mEmail.getText().toString())
                .password(mPassword.getText().toString())
                .name(mName.getText().toString())
                .lastname(mLastName.getText().toString())
                .podiatrist(false)
                .build();

        final Input<UserInput> userInputType = Input.optional(userInput);

        ApolloConnector.setupApollo().mutate(
                RegisterMutation
                .builder()
                        .userInput(userInputType)
                .build()
        )
                .enqueue(new ApolloCall.Callback<RegisterMutation.Data>() {

                    @Override
                    public void onResponse(@NotNull Response<RegisterMutation.Data> response) {
                        message = response.data().register().message();
                        Log.d(TAG, "Response: " + response.data().register());
                        // Correct information
                        if (response.data().register().status()) {
                            Intent landingpageActivity = new Intent(registerActivity.this, landingpageActivity.class);
                            startActivity(landingpageActivity);
                        } else {
                            mMessage.setText(message);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        mMessage.setText("Server Error!");
                        Log.d(TAG, "Exception " + e.getMessage(), e);
                    }
                });
    }

}
