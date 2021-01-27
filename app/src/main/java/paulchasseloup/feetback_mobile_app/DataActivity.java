package paulchasseloup.feetback_mobile_app;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.FileUpload;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.sample.AddMeasureMutation;
import com.apollographql.apollo.sample.UploadCSVMutation;
import com.apollographql.apollo.sample.type.MeasureInput;
import com.apollographql.apollo.sample.type.SensorInput;
import com.opencsv.CSVWriter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import paulchasseloup.feetback_mobile_app.Fragments.LandingPageFragment;
import paulchasseloup.feetback_mobile_app.Fragments.RightNoFragment;

public class DataActivity extends AppCompatActivity {


    private TextView title;
    private TextView conditions;
    private TextView cadre;
    private TextView timing;
    private Chronometer rw_chronometer;
    private Button start_btn;
    private Button cancel_btn;
    private Button next_btn;
    private TextView disconnect;

    // Sensors data storage
    private int currentSensor;
    private ArrayList<String> listSensors1 = new ArrayList<>();
    private ArrayList<String> listSensors2 = new ArrayList<>();
    private ArrayList<String> listSensors3 = new ArrayList<>();
    private ArrayList<String> listSensors4 = new ArrayList<>();
    private ArrayList<String> listSensors5 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_rw);

        this.title = findViewById(R.id.rw_title);
        this.conditions = findViewById(R.id.rw_condition);
        this.cadre = findViewById(R.id.rw_cadre);
        this.timing = findViewById(R.id.rw_time);
        this.rw_chronometer = findViewById(R.id.rw_chrono);
        this.start_btn = findViewById(R.id.rw_start);
        this.cancel_btn = findViewById(R.id.rw_stop);
        this.next_btn = findViewById(R.id.rw_next);
        this.disconnect = findViewById(R.id.disconnectLink2);

        String title_text = this.title.getText().toString();
        int protocole_number = Integer.parseInt(String.valueOf(title_text.charAt(0)));
        String time_max = "0";
        if(protocole_number == 1){
            time_max = "30";
        }else if(protocole_number == 2){
            time_max = "10";
        }else if(protocole_number == 3){
            time_max = "90";
        }else{
            time_max = "0";
        }

/*
        // Retrieve data from landing page
        Bundle extra = getIntent().getExtras();
        if(extra !=null) {
            userId = extra.getString("userId");
            token = extra.getString("token");
        }
        */
        final String finalTime_max = time_max;
        rw_chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override

            public void onChronometerTick(Chronometer chronometer) {


                // do something when chronometer changes
                if(chronometer.getText().toString().contains(finalTime_max)){
                    chronometer.stop();
                    timing.setText("Analyse terminee ! Cliquez sur SUIVANT pour continuer");
                }

                //Récuperation données bluetooth

            }

        });

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timing.setText("Analyse en cours ...");
                // Initialize chronometer
                rw_chronometer.setBase(SystemClock.elapsedRealtime());
                rw_chronometer.stop();
                rw_chronometer.start();

                    /*
                    // Start BT connection
                    try {
                        if (findBT()) {
                            openBT();
                            sendData("1");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/


                // End BT connection
//                    try {
                //                      sendData("0");
                //                    closeBT();
                //                  writeCsv();
                //            } catch (IOException e) {
                //              e.printStackTrace();
                //        }
                //  }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rw_chronometer.stop();
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(finalTime_max=="30"){
                    title.setText("1. Pied droit avec semelle en bipolade");
                    conditions.setText(getResources().getString(R.string.bipolade_conditions));
                    timing.setText(getResources().getString(R.string.bipolade_cadre));
                    cadre.setText(getResources().getString(R.string.bipolade_time));
                }else if(finalTime_max=="10"){
                    title.setText("2. Pied droit avec semelle en unipolade");
                    conditions.setText(getResources().getString(R.string.unipolade_conditions));
                    timing.setText(getResources().getString(R.string.unipolade_time));
                    cadre.setText(getResources().getString(R.string.unipolade_cadre));
                }else if(finalTime_max=="90"){
                    title.setText("3. Pied droit avec semelle en dynamique");
                    conditions.setText(getResources().getString(R.string.dynamic_contitions));
                    timing.setText(getResources().getString(R.string.dynamic_time));
                    cadre.setText(getResources().getString(R.string.dynamic_cadre));
                }else{
                    Fragment fragmentRightNo = RightNoFragment.newInstance();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.activity_landing_drawer_layout, fragmentRightNo);
                    fragmentTransaction.commit();
                }
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Return to landing page
                Fragment fragmentLandingActivity = LandingPageFragment.newInstance();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activity_landing_drawer_layout, fragmentLandingActivity);
                fragmentTransaction.commit();
            }
        });

    }

}
