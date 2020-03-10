package paulchasseloup.feetback_mobile_app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.FileUpload;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.sample.AddMeasureMutation;
import com.apollographql.apollo.sample.UploadCSVMutation;
import com.apollographql.apollo.sample.type.MeasureInput;
import com.apollographql.apollo.sample.type.SensorInput;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVWriter;

public class dataActivity extends AppCompatActivity {

    private ToggleButton mStartStopBtn;
    private Button mCancelBtn;
    private TextView mDisconnect;

    ArrayList<String> listSensors1 = new ArrayList<>();
    ArrayList<String> listSensors2 = new ArrayList<>();
    ArrayList<String> listSensors3 = new ArrayList<>();
    ArrayList<String> listSensors4 = new ArrayList<>();
    ArrayList<String> listSensors5 = new ArrayList<>();

    private final String TAG = "DataActivity";
    private String userId;
    private Chronometer mChronometer;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        mStartStopBtn = (ToggleButton) findViewById(R.id.toggleButton);
        mDisconnect = (TextView) findViewById(R.id.disconnectLink);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mCancelBtn = (Button) findViewById(R.id.cancelButton);

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
                    /// here the method to collect the data from the device
                } else {
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    /// here the method to to stop the sampling and to send the DB
                    try {
                        writeCsv();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChronometer.setBase(SystemClock.elapsedRealtime());
            }
        });

        mDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /// put here a method to disconect from DB

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

    public void verifyStoragePermissions() {
        // Check if we have write permission
        Activity activity = (Activity) this;
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    void writeCsv() throws IOException {
        verifyStoragePermissions();
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "SensorsData.csv";
        String filePath = baseDir + File.separator + fileName;
        final File f = new File(filePath);
        CSVWriter writer;
        FileWriter mFileWriter;

        // File exist
        if(f.exists()&&!f.isDirectory())
        {
                mFileWriter = new FileWriter(filePath, true);
                writer = new CSVWriter(mFileWriter);
        }
        else
        {
                writer = new CSVWriter(new FileWriter(filePath));
        }

        writer.writeNext(listSensors1.toArray(new String[listSensors1.size()]));
        writer.writeNext(listSensors2.toArray(new String[listSensors2.size()]));
        writer.writeNext(listSensors3.toArray(new String[listSensors3.size()]));
        writer.writeNext(listSensors4.toArray(new String[listSensors4.size()]));
        writer.writeNext(listSensors5.toArray(new String[listSensors5.size()]));
        writer.close();

        final  Input<FileUpload> fileInputType = Input.optional(new FileUpload("text/csv", f));

        ApolloConnector.setupApollo().mutate(
                UploadCSVMutation
                        .builder()
                        .fileInput(fileInputType)
                        .build()
        )
                .enqueue(new ApolloCall.Callback<UploadCSVMutation.Data>() {

                    @Override
                    public void onResponse(@NotNull Response<UploadCSVMutation.Data> response) {
                        Log.d(TAG, "Response: " + response.data().uploadCSV());
                        try {
                            f.delete();
                            addMeasure(response.data().uploadCSV().fileCompleteName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        Log.d(TAG, "Server Exception " + e.getMessage(), e);
                    }
                });


    }

    void addMeasure(String fileCompleteName) throws IOException {

        List<SensorInput> sensorList = new ArrayList<>();

        sensorList.add(findValues(listSensors1, 1));
        sensorList.add(findValues(listSensors2, 2));
        sensorList.add(findValues(listSensors3, 3));
        sensorList.add(findValues(listSensors4, 4));
        sensorList.add(findValues(listSensors5, 5));

        final MeasureInput measureInput = MeasureInput
                .builder()
                .patientId(userId)
                .csv(fileCompleteName)
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
