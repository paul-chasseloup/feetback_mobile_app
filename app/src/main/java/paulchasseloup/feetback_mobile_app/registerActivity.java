package paulchasseloup.feetback_mobile_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class registerActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private Button mRegisterBtn;
    private TextView mLandingPageLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mEmail = (EditText) findViewById(R.id.register_activity_email);
        mPassword = (EditText) findViewById(R.id.register_activity_password);
        mRegisterBtn = (Button) findViewById(R.id.register_activity_register_btn);
        mLandingPageLink = (TextView) findViewById(R.id.register_activity_landingpage_link);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /// put here a method to check if the Email/password is valid and to add it to the DB

                Intent landingpageActivity = new Intent(registerActivity.this, landingpageActivity.class);
                startActivity(landingpageActivity);
            }
        });

        mLandingPageLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent landingpageActivity = new Intent(registerActivity.this, landingpageActivity.class);
                startActivity(landingpageActivity);
            }
        });
    }
}
