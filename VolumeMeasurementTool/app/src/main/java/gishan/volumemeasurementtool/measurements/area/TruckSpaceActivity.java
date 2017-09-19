package gishan.volumemeasurementtool.measurements.area;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.atap.tango.reconstruction.TangoPolygon;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.tangosupport.TangoSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.measurements.area.renderers.TruckSpaceView;

/**
 * Created by Gishan Don Ranasinghe
 */

public class TruckSpaceActivity extends Activity implements TruckSpaceView
        .DrawingCallback {
    private static final String TAG = TruckSpaceActivity.class.getSimpleName();

    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int CAMERA_PERMISSION_CODE = 0;

    private TruckSpacePlanner mTangoFloorplanner;
    private Tango mTango;
    private TangoConfig mConfig;
    private boolean mIsConnected = false;
    private boolean mIsPaused;
    private FloatingActionButton mPauseButton;
    private TruckSpaceView mFloorplanView;
    private TextView mAreaText;

    private int mDisplayRotation = 0;

    private static float yRotationFromQuaternion(float x, float y, float z, float w) {
        return (float) Math.atan2(2 * (w * y - x * z), w * (w + x) - y * (z + y));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_truck_space);

        mPauseButton = (FloatingActionButton) findViewById(R.id.btnPause);
        mFloorplanView = (TruckSpaceView) findViewById(R.id.floorplan);
        mFloorplanView.registerCallback(this);
        mAreaText = (TextView) findViewById(R.id.tvArea);

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                @Override
                public void onDisplayAdded(int displayId) {
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized (this) {
                        setDisplayRotation();
                    }
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                }
            }, null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkAndRequestPermissions()) {
            bindTangoService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        synchronized (this) {
            try {
                if (mIsConnected) {
                    mTangoFloorplanner.stopFloorplanning();
                    mTango.disconnect();
                    mTangoFloorplanner.resetFloorplan();
                    mTangoFloorplanner.release();
                    mIsConnected = false;
                    mIsPaused = true;
                }
            } catch (TangoErrorException e) {
                Log.e(TAG, getString(R.string.exception_tango_error), e);
            }
        }
    }

    private void bindTangoService() {
        mTango = new Tango(TruckSpaceActivity.this, new Runnable() {
            // Pass in a Runnable to be called from UI thread when Tango is ready,
            // this Runnable will be running on a new thread.
            // When Tango is ready, we can call Tango functions safely here only
            // when there is no UI thread changes involved.
            @Override
            public void run() {
                synchronized (TruckSpaceActivity.this) {
                    try {
                        TangoSupport.initialize();
                        mConfig = setupTangoConfig(mTango);
                        mTango.connect(mConfig);
                        startupTango();
                        mIsConnected = true;
                        mIsPaused = false;
                        setDisplayRotation();
                    } catch (TangoOutOfDateException e) {
                        Log.e(TAG, getString(R.string.exception_out_of_date), e);
                        showsToastAndFinishOnUiThread(R.string.exception_out_of_date);
                    } catch (TangoErrorException e) {
                        Log.e(TAG, getString(R.string.exception_tango_error), e);
                        showsToastAndFinishOnUiThread(R.string.exception_tango_error);
                    } catch (TangoInvalidException e) {
                        Log.e(TAG, getString(R.string.exception_tango_invalid), e);
                        showsToastAndFinishOnUiThread(R.string.exception_tango_invalid);
                    }
                }
            }
        });
    }

    private TangoConfig setupTangoConfig(Tango tango) {
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);
        return config;
    }

    private void startupTango() {
        mTangoFloorplanner = new TruckSpacePlanner(new TruckSpacePlanner
                .OnFloorplanAvailableListener() {
            @Override
            public void onFloorplanAvailable(List<TangoPolygon> polygons) {
                mFloorplanView.setFloorplan(polygons);
                calculateAndUpdateArea(polygons);
            }
        });
        mTangoFloorplanner.setDepthCameraCalibration(mTango.getCameraIntrinsics
                (TangoCameraIntrinsics.TANGO_CAMERA_DEPTH));

        mTangoFloorplanner.startFloorplanning();

        List<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        mTango.connectListener(framePairs, new Tango.OnTangoUpdateListener() {
            @Override
            public void onPoseAvailable(TangoPoseData tangoPoseData) {
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData tangoXyzIjData) {
            }

            @Override
            public void onFrameAvailable(int i) {
            }

            @Override
            public void onTangoEvent(TangoEvent tangoEvent) {
            }

            @Override
            public void onPointCloudAvailable(TangoPointCloudData tangoPointCloudData) {
                mTangoFloorplanner.onPointCloudAvailable(tangoPointCloudData);
            }
        });
    }

    @Override
    public void onPreDrawing() {
        try {
            synchronized (TruckSpaceActivity.this) {
                if (!mIsConnected) {
                    return;
                }

                TangoPoseData devicePose = TangoSupport.getPoseAtTime(0.0,
                        TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                        TangoPoseData.COORDINATE_FRAME_DEVICE,
                        TangoSupport.TANGO_SUPPORT_ENGINE_OPENGL,
                        mDisplayRotation);

                if (devicePose.statusCode == TangoPoseData.POSE_VALID) {
                    float[] devicePosition = devicePose.getTranslationAsFloats();
                    float[] deviceOrientation = devicePose.getRotationAsFloats();
                    float yawRadians = yRotationFromQuaternion(deviceOrientation[0],
                            deviceOrientation[1], deviceOrientation[2],
                            deviceOrientation[3]);

                    mFloorplanView.updateCameraMatrix(devicePosition[0], -devicePosition[2],
                            yawRadians);
                } else {
                    Log.w(TAG, "Can't get last device pose");
                }
            }
        } catch (TangoErrorException e) {
            Log.e(TAG, "Tango error while querying device pose.", e);
        } catch (TangoInvalidException e) {
            Log.e(TAG, "Tango exception while querying device pose.", e);
        }
    }

    private void calculateAndUpdateArea(List<TangoPolygon> polygons) {
        double area = 0;
        for (TangoPolygon polygon : polygons) {
            if (polygon.layer == TangoPolygon.TANGO_3DR_LAYER_SPACE) {
                area += polygonArea(polygon);
            }
        }
        final String areaText = String.format(Locale.ENGLISH, "%s %.2fm²", getResources().getString(R.string.truck_space), area);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAreaText.setText(areaText);
            }
        });
    }

    private double polygonArea(TangoPolygon polygon) {
        double area = 0;
        int size = polygon.vertices2d.size();
        for (int i = 0; i < size; i++) {
            float[] v0 = polygon.vertices2d.get(i);
            float[] v1 = polygon.vertices2d.get((i + 1) % size);
            area += (v1[0] - v0[0]) * (v0[1] + v1[1]) / 2.0;
        }
        return area;
    }

    public void onPauseButtonClick(View v) {
        if (mIsPaused) {
            mTangoFloorplanner.startFloorplanning();
            mPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        } else {
            mTangoFloorplanner.stopFloorplanning();
            mPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        }
        mIsPaused = !mIsPaused;
    }

    public void onClearButtonClicked(View v) {
        mTangoFloorplanner.resetFloorplan();
    }

    private void setDisplayRotation() {
        Display display = getWindowManager().getDefaultDisplay();
        mDisplayRotation = display.getRotation();
    }

    private boolean checkAndRequestPermissions() {
        if (!hasCameraPermission()) {
            requestCameraPermission();
            return false;
        }
        return true;
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSION)) {
            showRequestPermissionRationale();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA_PERMISSION},
                    CAMERA_PERMISSION_CODE);
        }
    }

    private void showRequestPermissionRationale() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Java Floorplan Reconstruction Example requires camera permission")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(TruckSpaceActivity.this,
                                new String[]{CAMERA_PERMISSION}, CAMERA_PERMISSION_CODE);
                    }
                })
                .create();
        dialog.show();
    }

    private void showsToastAndFinishOnUiThread(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TruckSpaceActivity.this,
                        getString(resId), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (hasCameraPermission()) {
            bindTangoService();
        } else {
        }
    }
}