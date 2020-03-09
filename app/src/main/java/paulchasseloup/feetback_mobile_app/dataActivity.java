package paulchasseloup.feetback_mobile_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.sample.AddMeasureMutation;
import com.apollographql.apollo.sample.RegisterMutation;
import com.apollographql.apollo.sample.type.MeasureInput;
import com.apollographql.apollo.sample.type.SensorInput;
import com.apollographql.apollo.sample.type.UserInput;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import com.opencsv.CSVWriter;

public class dataActivity extends AppCompatActivity {

    private ToggleButton mStartStopBtn;
    private TextView mDisconnect;

    ArrayList<String> listSensors1 = new ArrayList<>();
    ArrayList<String> listSensors2 = new ArrayList<>();
    ArrayList<String> listSensors3 = new ArrayList<>();
    ArrayList<String> listSensors4 = new ArrayList<>();
    ArrayList<String> listSensors5 = new ArrayList<>();

    private final String TAG = "DataActivity";
    private String userId;
    private Chronometer mChronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        mStartStopBtn = (ToggleButton) findViewById(R.id.toggleButton);
        mDisconnect = (TextView) findViewById(R.id.disconnectLink);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        listSensors1.addAll(Arrays.asList("1","2"));
        listSensors2.addAll(Arrays.asList("2","3"));
        listSensors3.addAll(Arrays.asList("3","4"));
        listSensors4.addAll(Arrays.asList("4","5"));
        listSensors5.addAll(Arrays.asList("5","6"));

        Bundle extra = getIntent().getExtras();
        if(extra !=null){
            userId = extra.getString("userId");
        }

        mStartStopBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mChronometer.start();
                    // here the method to collect the data from the device
                } else {
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    // here the method to to stop the sampling and to send the DB
                    try {
                        processData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /// put here a method to check if the Email/password is set in the DB

                Intent landingpageActivity = new Intent(dataActivity.this, landingpageActivity.class);
                startActivity(landingpageActivity);
            }
        });
    }

    SensorInput findValues(ArrayList<String> sensors, int num) {
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
        Double sum = 0.0;
        for (String valString : sensors) {
            double val = Double.parseDouble(valString);
            if (val < min) {
                min = val;
            }
            if (val > max) {
                max = val;
            }
            sum += val;
        }

        final SensorInput sensorInput = SensorInput
                .builder()
                .numberInput(Input.optional(num))
                .posXInput(Input.optional(0.0))
                .posYInput(Input.optional(0.0))
                .minPressureS(min)
                .maxPressureS(max)
                .averagePressureS(sum / sensors.size())
                .build();
        return sensorInput;
    }

//    void writeCsv() {
//        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
//        String fileName = "SensorsData.csv";
//        String filePath = baseDir + File.separator + fileName;
//        File f = new File(filePath);
//        CSVWriter writer;
//
//        // File exist
//        if(f.exists()&&!f.isDirectory())
//        {
//            mFileWriter = new FileWriter(filePath, true);
//            writer = new CSVWriter(mFileWriter);
//        }
//        else
//        {
//            writer = new CSVWriter(new FileWriter(filePath));
//        }
//    }

    void processData() throws IOException {

        List<SensorInput> sensorList = new ArrayList<>();

        sensorList.add(findValues(listSensors1, 1));
        sensorList.add(findValues(listSensors2, 2));
        sensorList.add(findValues(listSensors3, 3));
        sensorList.add(findValues(listSensors4, 4));
        sensorList.add(findValues(listSensors5, 5));

//        // Write CSV file
//        PrintWriter writer = new PrintWriter("sensorsData.csv");
//        writer.println(Arrays.asList("pacientId:", userId));
//        writer.println(Arrays.asList("duration:" ));
//        writer.println(listSensors1);
//        writer.println(listSensors2);
//        writer.println(listSensors3);
//        writer.println(listSensors4);
//        writer.println(listSensors5);
//        writer.close();

        final MeasureInput measureInput = MeasureInput
                .builder()
                .patientId(userId)
                .sensors(sensorList)
                .build();

        final  Input<MeasureInput> measureInputType = Input.optional(measureInput);

        ApolloConnector.setupApollo().mutate(
                AddMeasureMutation
                        .builder()
                        .measureInput(measureInputType)
                        .build()
        )
                .enqueue(new ApolloCall.Callback<AddMeasureMutation.Data>() {

                    @Override
                    public void onResponse(@NotNull Response<AddMeasureMutation.Data> response) {
                        Log.d(TAG, "Response: " + response.data().addMeasure());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.d(TAG, "Server Exception " + e.getMessage(), e);
                    }
                });
    }


}
