package paulchasseloup.feetback_mobile_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.sample.LoginQuery;

import org.jetbrains.annotations.NotNull;

import paulchasseloup.feetback_mobile_app.Fragments.LandingPageFragment;
import paulchasseloup.feetback_mobile_app.Fragments.LeftNoFragment;
import paulchasseloup.feetback_mobile_app.Fragments.LeftWithFragment;
import paulchasseloup.feetback_mobile_app.Fragments.RightNoFragment;
import paulchasseloup.feetback_mobile_app.Fragments.RightWithFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{



    //FOR DESIGN
    //private Handler handler;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    //FOR FRAGMENTS
    //Declare fragment handled by Navigation Drawer
    private Fragment fragmentRightNo;
    private Fragment fragmentRightWith;
    private Fragment fragmentLeftNo;
    private Fragment fragmentLeftWith;
    //Declare first fragment
    private Fragment fragmentLandingPage;

    //FOR DATAS
    //Identify each fragment with a number
    private static final int FRAGMENT_RIGHT_NO = 0;
    private static final int FRAGMENT_RIGHT_WHITH = 1;
    private static final int FRAGMENT_LEFT_NO = 2;
    private static final int FRAGMENT_LEFT_WITH = 3;
    private static final int FRAGMENT_LANDING_PAGE = 4;

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Configure all views
        customizeLayout();

        //Show first fragment
        this.showFirstFragment();

    }


    private void customizeLayout() {
        // hide notification bar (battery status, wifi, ...)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.configureToolBar();
        this.configureDrawerLayout();

        this.configureNavigationView();
    }


    @Override
    public void onBackPressed() {
        //Handle back click to close menu
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            this.drawerLayout.openDrawer(GravityCompat.START);
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //Handle Navigation Item Click
        int id = item.getItemId();

        switch (id){
            case R.id.activity_main_menu_right_no :
                this.showFragment(FRAGMENT_RIGHT_NO);
                break;
            case R.id.activity_main_menu_right_with:
                this.showFragment(FRAGMENT_RIGHT_WHITH);
                break;
            case R.id.activity_main_menu_left_no:
                this.showFragment(FRAGMENT_LEFT_NO);
                break;
            case R.id.activity_main_menu_left_with:
                this.showFragment(FRAGMENT_LEFT_WITH);
                break;
            default:
                break;
        }

        this.drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    // ---------------------
    // CONFIGURATION
    // ---------------------

    //Configure Toolbar
    private void configureToolBar(){
        this.toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
    }

    //Configure Drawer Layout
    private void configureDrawerLayout(){
        this.drawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    //Configure NavigationView
    private void configureNavigationView(){
        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    // ---------------------
    // FRAGMENTS
    // ---------------------

    //Show first fragment when activity is created
    private void showFirstFragment(){
        Fragment visibleFragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_frame_layout);
        if (visibleFragment == null){
            //Show Map Fragment
            this.showFragment(FRAGMENT_LANDING_PAGE);
            //Mark as selected the menu item corresponding to MapFragment

            this.navigationView.getMenu().getItem(0).setChecked(true);
        }
    }


    // Show fragment according an Identifier
    private void showFragment(int fragmentIdentifier){

        Log.d("Main" , "in showFragment "+fragmentIdentifier);
        switch (fragmentIdentifier){
            case FRAGMENT_RIGHT_NO:
                Log.d("Main" , "in rn ");
                this.showRightNoFragment();
                break;
            case FRAGMENT_RIGHT_WHITH :
                Log.d("Main" , "in rw ");
                this.showRightWithFragment();
                break;
            case FRAGMENT_LEFT_NO:
                Log.d("Main" , "in ln ");
                this.showLeftNoFragment();
                break;
            case FRAGMENT_LEFT_WITH:
                Log.d("Main" , "in lw ");
                this.showLeftWithFragment();
                break;
            case FRAGMENT_LANDING_PAGE:
                this.showLandingPageFragment();
            default:
                break;
        }
    }
    // ---

    //Create each fragment page and show it
    private void showRightNoFragment(){
        if (this.fragmentRightNo == null)
            this.fragmentRightNo = RightNoFragment.newInstance();
        this.startTransactionFragment(this.fragmentRightNo);

        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void showRightWithFragment(){
        if (this.fragmentRightWith == null)
            this.fragmentRightWith = RightWithFragment.newInstance();
        this.startTransactionFragment(this.fragmentRightWith);

        navigationView.getMenu().getItem(1).setChecked(true);
    }

    private void showLeftNoFragment(){
        if (this.fragmentLeftNo == null)
            Log.d(TAG, "in new insta,ce left no");
            this.fragmentLeftNo = LeftNoFragment.newInstance();
        this.startTransactionFragment(this.fragmentLeftNo);

        navigationView.getMenu().getItem(2).setChecked(true);

    }

    private void showLeftWithFragment(){
        if (this.fragmentLeftWith == null)
            this.fragmentLeftWith = LeftWithFragment.newInstance();
        this.startTransactionFragment(this.fragmentLeftWith);

        navigationView.getMenu().getItem(3).setChecked(true);

    }

    private void showLandingPageFragment(){
        if (this.fragmentLandingPage == null)
            this.fragmentLandingPage = LandingPageFragment.newInstance();
        this.startTransactionFragment(this.fragmentLandingPage);

    }

    //Generic method that will replace and show a fragment inside the MainActivity Frame Layout
    private void startTransactionFragment(Fragment fragment){
        if (!fragment.isVisible()){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_frame_layout, fragment).commit();
        }
    }

    // ---


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }



}
