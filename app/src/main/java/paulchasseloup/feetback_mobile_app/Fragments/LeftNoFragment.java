package paulchasseloup.feetback_mobile_app.Fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

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

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import paulchasseloup.feetback_mobile_app.ApolloConnector;
import paulchasseloup.feetback_mobile_app.R;

public class LeftNoFragment extends Fragment {


    private TextView title;
    private TextView conditions;
    private TextView cadre;
    private TextView timing;
    private Chronometer ln_chronometer;
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

    private int protocole_id;
    private String time_max;

    private final String TAG = "DataActivity";
    private String userId;
    private String token;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //        Manifest.permission.REQUEST_ENABLE,
    };

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    volatile boolean stopWorker;
    private TextView bluetoothMsg;
    private String btDeviceName = "ESP32_Feetback";


    public static LeftNoFragment newInstance() {
        return (new LeftNoFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_ln,
                container, false);

        this.title = rootView.findViewById(R.id.ln_title);
        this.conditions = rootView.findViewById(R.id.ln_condition);
        this.cadre = rootView.findViewById(R.id.ln_cadre);
        this.timing = rootView.findViewById(R.id.ln_time);
        this.ln_chronometer = rootView.findViewById(R.id.ln_chrono);
        this.start_btn = rootView.findViewById(R.id.ln_start);
        this.cancel_btn = rootView.findViewById(R.id.ln_stop);
        this.next_btn = rootView.findViewById(R.id.ln_next);
        this.disconnect = rootView.findViewById(R.id.disconnectLinkLN);

        this.time_max = "30";

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.userId = bundle.getString("userId");
            this.token = bundle.getString("token");
        }else{
            this.token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjp7Il9pZCI6IjYwMGVjYzVmMGExZDBkMDAxNzdlZjI0OSIsIm5hbWUiOiJMYXVyZW50IiwibGFzdG5hbWUiOiJEZWxpc2xlIiwiZW1haWwiOiJsZGVsaXNsZUBpbnNlZWMuY29tIiwicG9kaWF0cmlzdCI6ZmFsc2UsImlkIjoxMywicmVnaXN0ZXJEYXRlIjoiMjAyMS0wMS0yNSAxMzo0OToxOSIsImFub21hbHkiOmZhbHNlLCJhbm9tYWx5X3RocmVzaG9sZCI6MjAsInNlbnNvcl8xX3RvcF9wb3NpdGlvbiI6NDU0LCJzZW5zb3JfMl90b3BfcG9zaXRpb24iOjQ1NCwic2Vuc29yXzNfdG9wX3Bvc2l0aW9uIjo0NTQsInNlbnNvcl80X3RvcF9wb3NpdGlvbiI6NDU0LCJzZW5zb3JfNV90b3BfcG9zaXRpb24iOjQ1NCwic2Vuc29yXzFfbGVmdF9wb3NpdGlvbiI6NDEyLCJzZW5zb3JfMl9sZWZ0X3Bvc2l0aW9uIjo0NTIsInNlbnNvcl8zX2xlZnRfcG9zaXRpb24iOjQ5Miwic2Vuc29yXzRfbGVmdF9wb3NpdGlvbiI6NTMyLCJzZW5zb3JfNV9sZWZ0X3Bvc2l0aW9uIjo1NzIsImN1cnJlbnRQb2RpYXRyaXN0IjoiIn0sImlhdCI6MTYxMTc4NzA4NCwiZXhwIjoxNjExODczNDg0fQ.8u9yRKHlPmssn2X7OLMTpceuKi7jDQeAMOulSDJ30EQ";
            this.userId = "13";
        }

        Log.d(TAG, "USER ID : "+this.userId);

        ln_chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override

            public void onChronometerTick(Chronometer chronometer) {

                final String finalTime_max = time_max;
                // do something when chronometer changes
                    if(chronometer.getText().toString().contains(finalTime_max)){
                        chronometer.stop();
                        timing.setText("Analyse terminee ! Cliquez sur SUIVANT pour continuer");
                    }

                // End BT connection
                try {
                    sendData("0");
                    closeBT();
                    writeLocalFile();
                    //writeCsv();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

        });

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timing.setText("Analyse en cours ...");
                // Initialize chronometer
                ln_chronometer.setBase(SystemClock.elapsedRealtime());
                ln_chronometer.stop();
                ln_chronometer.start();

                //Récuperation données bluetooth
                // Start BT connection
                try {
                    if (findBT()) {
                        openBT();
                        sendData("1");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ln_chronometer.setBase(SystemClock.elapsedRealtime());
                ln_chronometer.stop();
                timing.setText("Analyse arretee");
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ln_chronometer.setBase(SystemClock.elapsedRealtime());
                ln_chronometer.stop();
                String finalTime_max = time_max;
                if (time_max.contains("01:30")) {
                    time_max = "30";
                    setTitle(time_max);
                    conditions.setText(getResources().getString(R.string.bipodale_conditions));
                    timing.setText(getResources().getString(R.string.bipodale_time));
                    cadre.setText(getResources().getString(R.string.bipodale_conditions));
                }else if (finalTime_max.contains("30")) {
                    time_max = "10";
                    setTitle(time_max);
                    conditions.setText(getResources().getString(R.string.unipodale_conditions));
                    timing.setText(getResources().getString(R.string.unipodale_time));
                    cadre.setText(getResources().getString(R.string.unipodale_cadre));
                }else if (finalTime_max.contains("10")) {
                    time_max = "01:30";
                    setTitle(time_max);
                    conditions.setText(getResources().getString(R.string.dynamic_contitions));
                    timing.setText(getResources().getString(R.string.dynamic_time));
                    cadre.setText(getResources().getString(R.string.dynamic_cadre));
                    next_btn.setClickable(true);
                }
                next_btn.setClickable(true);
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Return to landing page
                Fragment fragmentLandingActivity = LandingPageFragment.newInstance();
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activity_landing_drawer_layout, fragmentLandingActivity);
                fragmentTransaction.commit();
            }
        });

        return rootView;
    }

    public void setTitle(String time){

        switch (time){
            case "30" :
                title.setText(getResources().getString(R.string.bipodale_title_left_no));
                break;
            case "10":
                title.setText(getResources().getString(R.string.unipodale_title_left_no));
                break;
            case "01:30":
                title.setText(getResources().getString(R.string.dynamic_title_left_no));
                break;
            default:
                break;
        }
    }
    private void writeLocalFile() throws JSONException {
        verifyStoragePermissions();
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        // Initialize file
        String date = DateFormat.getDateTimeInstance().format(new Date());
        String fileName = "SensorsLocalDataFeetback"+date+".txt";
        ////AJOUTER DATE ET HEURE AU NOM FICHIER
        String filePath = "Documents" + File.separator + fileName;
        Log.d("WRITE LOCAL", "filename : "+filePath);

        final File f = new File(filePath);
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        findValues(listSensors1, 1);
        findValues(listSensors2, 2);
        findValues(listSensors3, 3);
        findValues(listSensors4, 4);
        findValues(listSensors5, 5);

        Realm.init(getContext());
        String appID = "ppe-salix";
        App app = new App(new AppConfiguration.Builder(appID)
                .build());

        Credentials credentials = Credentials.anonymous();

        app.loginAsync(credentials, result -> {
            if(result.isSuccess()){
                Log.v("QUICKSTART", "Successfully authenticated");
                User user = app.currentUser();
                MongoClient mongoClient = user.getMongoClient("mongodb-atlas");
                MongoDatabase mongoDatabase = mongoClient.getDatabase("PPE");
                MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("measures");

                Document doc = new Document();
                doc.put("_id", new ObjectId());
                doc.append("patientId", this.userId);
                doc.put("date", date);
                doc.put("time", time_max);
                doc.put("protocole", "leftNo");
                doc.put("sensor1", this.listSensors1);
                doc.put("sensor2", this.listSensors2);
                doc.put("sensor3", this.listSensors3);
                doc.put("sensor4", this.listSensors4);
                doc.put("sensor5", this.listSensors5);

                Log.d("fgchvjk",": "+this.listSensors1);
                Log.d("OULALA", " : "+ doc.toJson().toString());
                mongoCollection.insertOne(doc).getAsync(task -> {
                    if(task.isSuccess()){
                        Log.v("QUICKSTART", "Success"+task.get().getInsertedId());
                    }else
                    {
                        Log.e("QUICKSTART", "Failed to log in. Error "+ task.getError().getErrorMessage());
                    }
                });
            }else{
                Log.e("QUICKSTART2", "Failed to log in. Error "+ result.getError());
            }
        });
        Log.d("App ", " : "+ app);
        // User user = app.currentUser();
        //Log.d("USer ", " : "+user);


        //String response = jsonObject.toString();


    }

    /**
     * Find Bluetooth connection
     *
     * @return boolean
     */
    private boolean findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            bluetoothMsg.setText("No bluetooth adapter available");
        }

        // BT permissions enable
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        boolean deviceFound = false;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
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

    /**
     * Open / Establishes BT connection
     *
     * @throws IOException
     */
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

    /**
     * Add data to corresponding sensor list (data captured in order 1 - 5)
     *
     * @param data sensor's reading
     */
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

    /**
     * Capture sensors' data from device
     */
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
                                            // save data in corresponding data list
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

    /**
     * Send data to device.  Start -> msg = 1, End -> msg = 0
     *
     * @param msg data to be sent
     * @throws IOException
     */
    private void sendData(String msg) throws IOException {
        //
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
    }

    /**
     * Analyze data: get min, max and average
     *
     * @param sensors list
     * @param num sensor's number
     * @return SensorInput
     */
    private SensorInput findValues(ArrayList<String> sensors, int num) {
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
        Double sum = 0.0;
        Input<List<Double>> sensorValues = null;
        //List<Float> sensorValues = null;
        //List<Double> sensorValues = null;
        //ArrayList<Double> sensorValues = null;
        //ArrayList<Float> sensorValues = null;
        for (String valString : sensors) {
            double val = Double.parseDouble(valString);
            // Minimum
            if (val < min) {
                min = val;
            }
            // Maximum
            if (val > max) {
                max = val;
            }
            //sensorValues.add(val);
            //sensorValues.add(val);
            // Add values to get average
            sum += val;
        }


        final SensorInput sensorInput = SensorInput
                .builder()
                .numberInput(Input.optional(num))
                .posXInput(Input.optional(0.0))
                .posYInput(Input.optional(0.0))
                .listInput(sensorValues)
                .minPressureS(min)
                .maxPressureS(max)
                .averagePressureS(sum / sensors.size())
                .build();
        return sensorInput;
    }

    /**
     * Close BT connection
     *
     * @throws IOException
     */
    private void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

    /**
     * Get permissions if required
     */
    public void verifyStoragePermissions() {
        // Check if we have write permission
        Activity activity = (Activity) this.getActivity();
        int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        //    int permission3 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.REQUEST_ENABLE);


        if (permission1 != PackageManager.PERMISSION_GRANTED && permission2 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * Create csv file with sensors' data
     *
     * @throws IOException
     */
    private void writeCsv() throws IOException {
        verifyStoragePermissions();
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

        // Initialize file
        String fileName = "SensorsDataFeetback.csv";
        String filePath = baseDir + File.separator + fileName;
        final File f = new File(filePath);
        CSVWriter writer;
        FileWriter mFileWriter;

        FileWriter fw = new FileWriter(filePath);
        writer = new CSVWriter(fw);

        // Write data
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

    /**
     * Send measure to server
     *
     * @param fileCompleteName
     * @throws IOException
     */
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
