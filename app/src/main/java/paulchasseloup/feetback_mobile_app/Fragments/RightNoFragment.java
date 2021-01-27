package paulchasseloup.feetback_mobile_app.Fragments;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

import paulchasseloup.feetback_mobile_app.R;

public class RightNoFragment extends Fragment {


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

    private int protocole_id;
    private String time_max;

    public static RightNoFragment newInstance() {
        return (new RightNoFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_rn,
                container, false);

        this.title = rootView.findViewById(R.id.rw_title);
        this.conditions = rootView.findViewById(R.id.rw_condition);
        this.cadre = rootView.findViewById(R.id.rw_cadre);
        this.timing = rootView.findViewById(R.id.rw_time);
        this.rw_chronometer = rootView.findViewById(R.id.rw_chrono);
        this.start_btn = rootView.findViewById(R.id.rw_start);
        this.cancel_btn = rootView.findViewById(R.id.rw_stop);
        this.next_btn = rootView.findViewById(R.id.rw_next);
        this.disconnect = rootView.findViewById(R.id.disconnectLink2);

        this.time_max = "30";


/*
        // Retrieve data from landing page
        Bundle extra = getIntent().getExtras();
        if(extra !=null) {
            userId = extra.getString("userId");
            token = extra.getString("token");
        }
        */

        rw_chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override

            public void onChronometerTick(Chronometer chronometer) {

                final String finalTime_max = time_max;
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
                timing.setText("Analyse arretee");
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String finalTime_max = time_max;
                if(finalTime_max.contains("30")){
                    time_max = "10";
                    setTitle(time_max);
                    conditions.setText(getResources().getString(R.string.unipolade_conditions));
                    timing.setText(getResources().getString(R.string.unipolade_time));
                    cadre.setText(getResources().getString(R.string.unipolade_cadre));
                }else if(finalTime_max.contains("10")){
                    time_max = "90";
                    setTitle(time_max);
                    conditions.setText(getResources().getString(R.string.dynamic_contitions));
                    timing.setText(getResources().getString(R.string.dynamic_time));
                    cadre.setText(getResources().getString(R.string.dynamic_cadre));
                }else if(finalTime_max.contains("90")){
                    time_max = "30";
                    setTitle(time_max);
                    conditions.setText(getResources().getString(R.string.bipolade_conditions));
                    timing.setText(getResources().getString(R.string.bipolade_time));
                    cadre.setText(getResources().getString(R.string.bipolade_cadre));
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
                    title.setText(getResources().getString(R.string.bipolade_title_right_no));
                break;
            case "10":
                    title.setText(getResources().getString(R.string.unipolade_title_right_no));
                break;
            case "90":
                title.setText(getResources().getString(R.string.dynamic_title_right_no));
                break;
            default:
                break;
        }
    }

}