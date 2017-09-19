package gishan.volumemeasurementtool.home;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.home.adapters.HomeListAdapter;
import gishan.volumemeasurementtool.login.LoginActivity;
import gishan.volumemeasurementtool.measurements.area.TruckSpaceActivity;
import gishan.volumemeasurementtool.measurements.volumetric.MeasurementActivity;
import gishan.volumemeasurementtool.models.ModelMenuItem;
import gishan.volumemeasurementtool.notifications.NotificationsActivity;
import gishan.volumemeasurementtool.utils.AlertUtils;
import gishan.volumemeasurementtool.utils.ProgressUtils;

/**
 * Created by Gishan Don Ranasinghe
 */

public class HomeActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private DrawerLayout mDrawerLayout;
    private TextView tvDriverId;
    private TextView tvName;
    private ImageButton mImgBtnSettings;
    private ListView mLvMenuItems;
    private HomeListAdapter mListAdapter;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        locateViews();
        initViews();
    }

    private void locateViews() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.dlMain);
        tvName = (TextView) findViewById(R.id.tvName);
        tvDriverId = (TextView) findViewById(R.id.tvDriverId);
        mImgBtnSettings = (ImageButton) findViewById(R.id.imgbtnSettings);
        mLvMenuItems = (ListView) findViewById(R.id.lvMenuItems);
    }

    private void initViews() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.title_home, R.string.app_name);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        tvName.setText(ParseUser.getCurrentUser().getString("name"));
        tvDriverId.setText(getString(R.string.carrier_id, ParseUser.getCurrentUser().getUsername()));
        mImgBtnSettings.setOnClickListener(this);

        List<ModelMenuItem> menuItems = new ArrayList<ModelMenuItem>();
        menuItems.add(new ModelMenuItem(getString(R.string.home_title_notifications)));
        menuItems.add(new ModelMenuItem(getString(R.string.about)));
        menuItems.add(new ModelMenuItem(getString(R.string.home_title_logout)));
        mListAdapter = new HomeListAdapter(getApplicationContext(), R.layout.listelement_home_drawer, menuItems);
        mLvMenuItems.setAdapter(mListAdapter);
        mLvMenuItems.setOnItemClickListener(this);

        loadFrag(new HomePercentageFragment(), R.id.flContainerProgress);
        loadFrag(new HomeItemsAddedTodayFragment(), R.id.flContainerItemsAdded);
    }

    private void loadFrag(Fragment fragment, int layoutDest) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(layoutDest, fragment);
        transaction.commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            } else {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        } else if (item.getItemId() == R.id.action_refresh) {
            HomePercentageFragment homePercentageFragment = (HomePercentageFragment) getFragmentManager().findFragmentById(R.id.flContainerProgress);
            homePercentageFragment.updateProgress();

            HomeItemsAddedTodayFragment homeItemsAddedTodayFragment = (HomeItemsAddedTodayFragment) getFragmentManager().findFragmentById(R.id.flContainerItemsAdded);
            homeItemsAddedTodayFragment.reloadItemsAddedToday();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View view) {
        if (view == mImgBtnSettings) {

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                startActivity(new Intent(this, NotificationsActivity.class));
                break;
            case 1:
                AlertUtils.showInfoAlert(HomeActivity.this, getResources().getString(R.string.version_number));
                break;
            case 2:
                ProgressUtils.showDialog(HomeActivity.this);

                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        ProgressUtils.dismissDialog();
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                break;
        }
    }

    public void measureItem(View view) {
        Intent intent = new Intent(HomeActivity.this, MeasurementActivity.class);
        startActivity(intent);
    }

    public void calculateTruckSpace(View view) {
        Intent intent = new Intent(HomeActivity.this, TruckSpaceActivity.class);
        startActivity(intent);
    }
}