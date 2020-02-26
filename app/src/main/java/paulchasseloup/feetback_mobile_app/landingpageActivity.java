package paulchasseloup.feetback_mobile_app;

        import androidx.appcompat.app.AppCompatActivity;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;

public class landingpageActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private Button mLoginBtn;
    private TextView mRegisterLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEmail = (EditText) findViewById(R.id.activity_main_email);
        mPassword = (EditText) findViewById(R.id.activity_main_password);
        mLoginBtn = (Button) findViewById(R.id.activity_main_login_btn);
        mRegisterLink = (TextView) findViewById(R.id.activity_main_register_link);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /// put here a method to check if the Email/password is set in the DB

                Intent dataActivity = new Intent(landingpageActivity.this, dataActivity.class);
                startActivity(dataActivity);
            }
        });

        mRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerActivity = new Intent(landingpageActivity.this, registerActivity.class);
                startActivity(registerActivity);
            }
        });
    }
}
