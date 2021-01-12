package paulchasseloup.feetback_mobile_app;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class menuActivity extends AppCompatActivity {

    private ImageView profileButton = findViewById(R.id.profileButton);
    private ImageView activityButton = findViewById(R.id.activityButton);
    private ImageView resultsButton = findViewById(R.id.resultsButton);

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Bundle extra = getIntent().getExtras();
        if(extra !=null){
            userId = extra.getString("userId");
        }
    }
}
