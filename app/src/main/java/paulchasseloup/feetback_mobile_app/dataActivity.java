package paulchasseloup.feetback_mobile_app;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class dataActivity extends AppCompatActivity {

    private ToggleButton mStartStopBtn;
    private Button mCancelBtn;
    private TextView mDisconnect;

    private int currentSensor;
    private ArrayList<String> listSensors1 = new ArrayList<>();
    private ArrayList<String> listSensors2 = new ArrayList<>();
    private ArrayList<String> listSensors3 = new ArrayList<>();
    private ArrayList<String> listSensors4 = new ArrayList<>();
    private ArrayList<String> listSensors5 = new ArrayList<>();

    private final String TAG = "DataActivity";
    private String userId;
    private Chronometer mChronometer;

    private String token;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private int counter;
    volatile boolean stopWorker;
    private TextView bluetoothMsg;
    private String btDeviceName = "ESP32_Feetback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        mStartStopBtn = (ToggleButton) findViewById(R.id.toggleButton);
        mDisconnect = (TextView) findViewById(R.id.disconnectLink);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mCancelBtn = (Button) findViewById(R.id.cancelButton);
        bluetoothMsg = (TextView) findViewById(R.id.bluetoothMsg);

//        listSensors1.addAll(Arrays.asList("1","2"));
//        listSensors2.addAll(Arrays.asList("2","3"));
//        listSensors3.addAll(Arrays.asList("3","4"));
//        listSensors4.addAll(Arrays.asList("4","5"));
//        listSensors5.addAll(Arrays.asList("5","6"));

        Bundle extra = getIntent().getExtras();
        if(extra !=null) {
            userId = extra.getString("userId");
            token = extra.getString("token");
        }

        mStartStopBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.stop();
                    mChronometer.start();
                    /// here the method to collect the data from the device
                    try {
                        if (findBT()) {
                            openBT();
                            sendData("1");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.stop();
                    /// here the method to to stop the sampling and to send the DB
                    try {
                        sendData("0");
                        closeBT();
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
                try {
                    sendData("1");
                    closeBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mStartStopBtn.setChecked(false);
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

    private boolean findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            bluetoothMsg.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        boolean deviceFound = false;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                // Bluetooth device name
                if(device.getName().equals(btDeviceName)) {
                    bluetoothMsg.setText("Bluetooth Device Found");
                    deviceFound = true;
                    mmDevice = device;
                    break;
                }
            }
        }
        if (!deviceFound) {
            bluetoothMsg.setText("Bluetooth NOT Device Found");
        }
        return deviceFound;
    }

    private void openBT() throws IOException {
        bluetoothMsg.setText("");
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        currentSensor = 1;
        beginListenForData();
    }

    private void updateSensorList(String data) {
        switch (currentSensor) {
            case 2:
                listSensors2.add(data);
                break;
            case 3:
                listSensors3.add(data);
                break;
            case 4:
                listSensors4.add(data);
                break;
            case 5:
                listSensors5.add(data);
                break;
            default:
                listSensors1.add(data);
                currentSensor = 1;
        }
        currentSensor++;
    }

    private void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            updateSensorList(data);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }

    private void sendData(String msg) throws IOException {
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
    }

    private SensorInput findValues(ArrayList<String> sensors, int num) {
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

    private void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        Activity activity = (Activity) this;
        int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission1 != PackageManager.PERMISSION_GRANTED && permission2 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void writeCsv() throws IOException {
        verifyStoragePermissions();
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

        String fileName = "SensorsDataFeetback.csv";
        String filePath = baseDir + File.separator + fileName;
        final File f = new File(filePath);
        CSVWriter writer;
        FileWriter mFileWriter;

        FileWriter fw = new FileWriter(filePath);
        writer = new CSVWriter(fw);

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
                        .token(token)
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

    private void addMeasure(String fileCompleteName) throws IOException {
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
