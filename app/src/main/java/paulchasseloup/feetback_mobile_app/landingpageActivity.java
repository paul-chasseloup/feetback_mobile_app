package paulchasseloup.feetback_mobile_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.sample.LoginQuery;

import org.jetbrains.annotations.NotNull;

public class landingpageActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private TextView mRegisterLink;
    private TextView labelMessage;

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEmail = (EditText) findViewById(R.id.activity_main_email);
        mPassword = (EditText) findViewById(R.id.activity_main_password);
        labelMessage = (TextView) findViewById(R.id.labelMessage);
        mRegisterLink = (TextView) findViewById(R.id.activity_main_register_link);

        labelMessage.setText("Welcome!");

        mRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerActivity = new Intent(landingpageActivity.this, registerActivity.class);
                startActivity(registerActivity);
            }
        });
    }

    public void Login(View view) {
        ApolloConnector.setupApollo().query(
                LoginQuery
                        .builder()
                        .email(mEmail.getText().toString())
                        .password(mPassword.getText().toString())
                        .build())
                .enqueue(new ApolloCall.Callback<LoginQuery.Data>() {

                    @Override
                    public void onResponse(@NotNull Response<LoginQuery.Data> response) {

                        labelMessage.setText(response.data().login().message());
                        if (response.data().login().token() != null) {
                            Intent dataActivity = new Intent(landingpageActivity.this, dataActivity.class);
                            dataActivity.putExtra("userId", response.data().login().user().id().toString());
                            dataActivity.putExtra("token", response.data().login().token());
                            startActivity(dataActivity);
                        }
                        Log.d(TAG, "Response: " + response.data().login());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {

                        labelMessage.setText("Server Error!");
                        Log.d(TAG, "Exception " + e.getMessage(), e);
                    }
                });
    }
}
