package gishan.volumemeasurementtool.measurements.volumetric;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoException;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.google.atap.tangoservice.experimental.TangoImageBuffer;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.projecttango.tangosupport.TangoSupport;

import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.ASceneFrameCallback;
import org.rajawali3d.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.measurements.volumetric.renderers.MeasurementRenderer;
import gishan.volumemeasurementtool.utils.Utils;

/**
 * Created by Gishan Don Ranasinghe
 */

public class MeasurementActivity extends Activity {
    private static final String TAG = MeasurementActivity.class.getSimpleName();

    public static final String CAPTURED_BITMAP = "captured_bitmap";
    public static final String MEASURED_WIDTH = "measured_width";
    public static final String MEASURED_HEIGHT = "measured_height";
    public static final String MEASURED_LENGTH = "measured_length";
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static final int CAMERA_PERMISSION_CODE = 0;
    private static final int INVALID_TEXTURE_ID = 0;
    private static final int UPDATE_UI_INTERVAL_MS = 100;

    private ImageView mCrosshair;
    private SurfaceView mSurfaceView;
    private MeasurementRenderer mRenderer;
    private TangoPointCloudManager mPointCloudManager;
    private Tango mTango;
    private TangoConfig mConfig;
    private boolean mIsConnected = false;
    private double mCameraPoseTimestamp = 0;
    private int mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
    private AtomicBoolean mIsFrameAvailableTangoThread = new AtomicBoolean(false);
    private double mRgbTimestampGlThread;
    private Handler mHandler = new Handler();
    private float[][] mLinePoints = new float[2][3];
    private ArrayList<float[]> rgbPoints;
    private boolean mPointSwitch = true;
    private boolean mPointAdded = false;
    private int mColorCameraToDisplayAndroidRotation = 0;
    private boolean isPoseValid;
    private int mDisplayRotation = 0;
    private volatile TangoImageBuffer mCurrentImageBuffer;
    private float mWidth, mLength, mHeight = 0;

    private Runnable mUpdateUiLoopRunnable = new Runnable() {
        public void run() {
            updateUi();
            mHandler.postDelayed(this, UPDATE_UI_INTERVAL_MS);
        }
    };
    private Runnable mUpdateCrossHair = new Runnable() {
        public void run() {
            updateCrossHair();
        }
    };

    private static float[] projectionMatrixFromCameraIntrinsics(TangoCameraIntrinsics intrinsics) {
        float near = 0.1f;
        float far = 100;

        double cx = intrinsics.cx;
        double cy = intrinsics.cy;
        double width = intrinsics.width;
        double height = intrinsics.height;
        double fx = intrinsics.fx;
        double fy = intrinsics.fy;

        double xscale = near / fx;
        double yscale = near / fy;

        double xoffset = (cx - (width / 2.0)) * xscale;
        double yoffset = -(cy - (height / 2.0)) * yscale;

        float m[] = new float[16];
        Matrix.frustumM(m, 0,
                (float) (xscale * -width / 2.0 - xoffset),
                (float) (xscale * width / 2.0 - xoffset),
                (float) (yscale * -height / 2.0 - yoffset),
                (float) (yscale * height / 2.0 - yoffset), near, far);
        return m;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volume_measure);

        locateViews();
        initViews();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mSurfaceView.onResume();

        if (checkAndRequestPermissions()) {
            bindTangoService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        synchronized (this) {
            if (mIsConnected) {
                try {
                    mRenderer.getCurrentScene().clearFrameCallbacks();
                    mTango.disconnectCamera(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                    mConnectedTextureIdGlThread = INVALID_TEXTURE_ID;
                    mTango.disconnect();
                    mIsConnected = false;
                } catch (TangoErrorException e) {
                    Log.e(TAG, getString(R.string.exception_tango_error), e);
                }
            }
        }
    }

    private void locateViews() {
        mSurfaceView = (SurfaceView) findViewById(R.id.ar_view);
        mCrosshair = (ImageView) findViewById(R.id.crosshair);
    }

    private void initViews() {
        mCrosshair.setColorFilter(getResources().getColor(R.color.brightGreen));

        mRenderer = new MeasurementRenderer(this);
        mSurfaceView.setSurfaceRenderer(mRenderer);
        mSurfaceView.setZOrderOnTop(false);

        mPointCloudManager = new TangoPointCloudManager();

        rgbPoints = new ArrayList<>();

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

    private void bindTangoService() {
        mTango = new Tango(MeasurementActivity.this, new Runnable() {
            // Pass in a Runnable to be called from UI thread when Tango is ready, this Runnable
            // will be running on a new thread.
            // When Tango is ready, we can call Tango functions safely here only when there is no UI
            // thread changes involved.
            @Override
            public void run() {
                synchronized (MeasurementActivity.this) {
                    try {
                        TangoSupport.initialize();
                        mConfig = setupTangoConfig(mTango);
                        mTango.connect(mConfig);
                        startupTango();
                        connectRenderer();
                        mIsConnected = true;
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
        mHandler.post(mUpdateUiLoopRunnable);
    }

    private TangoConfig setupTangoConfig(Tango tango) {
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_LOWLATENCYIMUINTEGRATION, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_COLORCAMERA, true);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DRIFT_CORRECTION, true);
        return config;
    }

    private void startupTango() {
        ArrayList<TangoCoordinateFramePair> framePairs =
                new ArrayList<TangoCoordinateFramePair>();
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {
            @Override
            public void onPoseAvailable(TangoPoseData pose) {
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                if (cameraId == TangoCameraIntrinsics.TANGO_CAMERA_COLOR) {
                    mIsFrameAvailableTangoThread.set(true);
                    mSurfaceView.requestRender();
                }
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
            }

            @Override
            public void onPointCloudAvailable(TangoPointCloudData pointCloud) {
                mPointCloudManager.updatePointCloud(pointCloud);
            }

            @Override
            public void onTangoEvent(TangoEvent event) {
            }
        });
        mTango.experimentalConnectOnFrameListener(TangoCameraIntrinsics.TANGO_CAMERA_COLOR,
                new Tango.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(TangoImageBuffer tangoImageBuffer, int i) {
                        mCurrentImageBuffer = copyImageBuffer(tangoImageBuffer);
                    }

                    TangoImageBuffer copyImageBuffer(TangoImageBuffer imageBuffer) {
                        ByteBuffer clone = ByteBuffer.allocateDirect(imageBuffer.data.capacity());
                        imageBuffer.data.rewind();
                        clone.put(imageBuffer.data);
                        imageBuffer.data.rewind();
                        clone.flip();
                        return new TangoImageBuffer(imageBuffer.width, imageBuffer.height,
                                imageBuffer.stride, imageBuffer.frameNumber,
                                imageBuffer.timestamp, imageBuffer.format, clone);
                    }
                });
    }

    private void connectRenderer() {
        mRenderer.getCurrentScene().registerFrameCallback(new ASceneFrameCallback() {
            @Override
            public void onPreFrame(long sceneTime, double deltaTime) {
                try {
                    synchronized (MeasurementActivity.this) {
                        if (!mIsConnected) {
                            return;
                        }

                        if (!mRenderer.isSceneCameraConfigured()) {
                            TangoCameraIntrinsics intrinsics =
                                    TangoSupport.getCameraIntrinsicsBasedOnDisplayRotation(
                                            TangoCameraIntrinsics.TANGO_CAMERA_COLOR,
                                            mDisplayRotation);
                            mRenderer.setProjectionMatrix(
                                    projectionMatrixFromCameraIntrinsics(intrinsics));
                        }

                        if (mConnectedTextureIdGlThread != mRenderer.getTextureId()) {
                            mTango.connectTextureId(TangoCameraIntrinsics.TANGO_CAMERA_COLOR,
                                    mRenderer.getTextureId());
                            mConnectedTextureIdGlThread = mRenderer.getTextureId();
                        }

                        if (mIsFrameAvailableTangoThread.compareAndSet(true, false)) {
                            mRgbTimestampGlThread =
                                    mTango.updateTexture(TangoCameraIntrinsics.TANGO_CAMERA_COLOR);
                        }

                        if (mRgbTimestampGlThread > mCameraPoseTimestamp) {
                            TangoPoseData lastFramePose = TangoSupport.getPoseAtTime(
                                    mRgbTimestampGlThread,
                                    TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                                    TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                                    TangoSupport.TANGO_SUPPORT_ENGINE_OPENGL,
                                    mColorCameraToDisplayAndroidRotation);

                            if (lastFramePose.statusCode == TangoPoseData.POSE_VALID) {

                                mRenderer.updateRenderCameraPose(lastFramePose);
                                mCameraPoseTimestamp = lastFramePose.timestamp;

                                /*if(!isPoseValid) {
                                    isPoseValid = true;
                                    mHandler.post(mUpdateCrossHair);
                                }*/
                            } else {
                                /*if(isPoseValid) {
                                    isPoseValid = false;
                                    mHandler.post(mUpdateCrossHair);
                                }*/
                            }

                        }
                    }
                } catch (TangoErrorException e) {
                    Log.e(TAG, "Tango API call error within the OpenGL render thread", e);
                } catch (Throwable t) {
                    Log.e(TAG, "Exception on the OpenGL thread", t);
                }
            }

            @Override
            public void onPreDraw(long sceneTime, double deltaTime) {

            }

            @Override
            public void onPostFrame(long sceneTime, double deltaTime) {

            }

            @Override
            public boolean callPreFrame() {
                return true;
            }
        });
    }

    public void calculate(View view) {
        captureBitmap(new BitmapReadyCallbacks() {
            @Override
            public void onBitmapReady(Bitmap bitmap) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                Intent intent = new Intent(MeasurementActivity.this, SummaryActivity.class);
                intent.putExtra(CAPTURED_BITMAP, byteArray);
                intent.putExtra(MEASURED_HEIGHT, mHeight);
                intent.putExtra(MEASURED_WIDTH, mWidth);
                intent.putExtra(MEASURED_LENGTH, mLength);
                startActivity(intent);
            }
        });
    }

    public void undoPoint(View view) {
        if (mRenderer.mChildren.size() > 0) {
            updateLine((mPointSwitch ? mLinePoints[0] : mLinePoints[1]));
            mRenderer.undoLine(generateEndpoints());
        }
    }

    public void addVolumePoint(View view) {
        float u = .5f;
        float v = .5f;

        try {
            float[] rgbPoint;
            synchronized (this) {
                rgbPoint = getDepthAtTouchPosition(u, v);
            }
            markPoint(rgbPoint);
            mHeight = getPointSeparation();
        } catch (TangoException t) {
            Toast.makeText(getApplicationContext(),
                    R.string.failed_measurement,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.failed_measurement), t);
        } catch (SecurityException t) {
            Toast.makeText(getApplicationContext(),
                    R.string.failed_permissions,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.failed_permissions), t);
        }
    }

    public void addPoint(View view) {
        float u = .5f;
        float v = .5f;

        try {
            float[] rgbPoint;
            synchronized (this) {
                rgbPoint = getDepthAtTouchPosition(u, v);
            }
            markPoint(rgbPoint);

            if (rgbPoints.size() == 2) {
                mWidth = getPointSeparation();
            } else if (rgbPoints.size() == 3) {
                mLength = getPointSeparation();
            }
        } catch (TangoException t) {
            Toast.makeText(getApplicationContext(),
                    R.string.failed_measurement,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.failed_measurement), t);
        } catch (SecurityException t) {
            Toast.makeText(getApplicationContext(),
                    R.string.failed_permissions,
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.failed_permissions), t);
        }
    }

    private void markPoint(float[] rgbPoint) {
        if (rgbPoint != null) {
            float currRGBPointX = rgbPoint[0];
            float currRGBPointY = rgbPoint[1];

            if (rgbPoints != null && rgbPoints.size() > 0) {
                float[] firstRGBPoint = rgbPoints.get(0);
                float fstRGBPointX = firstRGBPoint[0];
                float fstRGBPointY = firstRGBPoint[1];

                if ((currRGBPointX > fstRGBPointX - 0.05 && currRGBPointX < fstRGBPointX + 0.05)
                        && (currRGBPointY > fstRGBPointY - 0.05 && currRGBPointY < fstRGBPointY + 0.05)) {
                    rgbPoint = firstRGBPoint;
                }
            }

            rgbPoints.add(rgbPoint);

            if (mLinePoints[0] != null) {
                float[] previousPoint;
                previousPoint = (mLinePoints[1] == null ? mLinePoints[0] : mLinePoints[1]);
                if (currRGBPointY > previousPoint[1] - 0.05 && currRGBPointY < previousPoint[1] + 0.05) {
                    rgbPoint[1] = previousPoint[1];
                } else if (currRGBPointX > previousPoint[0] - 0.05 && currRGBPointX < previousPoint[0] + 0.05) {
                    rgbPoint[0] = previousPoint[0];
                }
            }

            updateLine(rgbPoint);

            mRenderer.setLine(generateEndpoints());
            mPointAdded = true;


            Toast.makeText(getApplicationContext(), R.string.point_added, Toast.LENGTH_SHORT).show();
            for (float[] point : rgbPoints) {
                Log.d(TAG, "(" + point[0] + "," + point[1] + ")");
            }
            Log.d(TAG, "-------------------------------------");

        } else {
            Toast.makeText(getApplicationContext(), R.string.point_not_clear, Toast.LENGTH_SHORT).show();
        }
    }

    public void clearPoints(View view) {
        reset();
    }

    private float[] getDepthAtTouchPosition(float u, float v) {
        TangoPointCloudData pointCloud = mPointCloudManager.getLatestPointCloud();
        if (pointCloud == null) {
            return null;
        }

        double rgbTimestamp;
        rgbTimestamp = mRgbTimestampGlThread; // GPU.


        TangoPoseData colorTdepthPose = TangoSupport.calculateRelativePose(
                rgbTimestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                pointCloud.timestamp, TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH);

        float[] point;
        double[] identityTranslation = {0.0, 0.0, 0.0};
        double[] identityRotation = {0.0, 0.0, 0.0, 1.0};
        point = TangoSupport.getDepthAtPointNearestNeighbor(pointCloud,
                colorTdepthPose.translation, colorTdepthPose.rotation,
                u, v, mDisplayRotation, identityTranslation, identityRotation);
        if (point == null) {
            return null;
        }

        TangoSupport.TangoMatrixTransformData transform =
                TangoSupport.getMatrixTransformAtTime(pointCloud.timestamp,
                        TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                        TangoPoseData.COORDINATE_FRAME_CAMERA_COLOR,
                        TangoSupport.TANGO_SUPPORT_ENGINE_OPENGL,
                        TangoSupport.TANGO_SUPPORT_ENGINE_TANGO,
                        TangoSupport.ROTATION_IGNORED);
        if (transform.statusCode == TangoPoseData.POSE_VALID) {
            float[] depthPoint = new float[]{point[0], point[1], point[2], 1};
            float[] openGlPoint = new float[4];
            Matrix.multiplyMV(openGlPoint, 0, transform.matrix, 0, depthPoint, 0);
            return openGlPoint;
        } else {
            Log.w(TAG, "Could not get depth camera transform at time " + pointCloud.timestamp);
        }
        return null;
    }

    private synchronized void updateLine(float[] worldPoint) {
        if (mPointSwitch) {
            mPointSwitch = false;
            mLinePoints[0] = worldPoint;
            return;
        }
        mPointSwitch = true;
        mLinePoints[1] = worldPoint;
    }

    private synchronized Stack<Vector3> generateEndpoints() {

        if (mLinePoints[0] != null && mLinePoints[1] != null) {
            Stack<Vector3> points = new Stack<Vector3>();
            points.push(new Vector3(mLinePoints[0][0], mLinePoints[0][1], mLinePoints[0][2]));
            points.push(new Vector3(mLinePoints[1][0], mLinePoints[1][1], mLinePoints[1][2]));
            return points;
        }
        return null;
    }

    private synchronized void reset() {
        mRenderer.clearChildren();
        rgbPoints = new ArrayList<float[]>();
        mLinePoints[0] = null;
        mLinePoints[1] = null;
        mWidth = 0;
        mHeight = 0;
        mLength = 0;
        mRenderer.setLine(null);

    }

    private synchronized float getPointSeparation() {
        if (mLinePoints[0] == null || mLinePoints[1] == null) {
            return 0;
        }
        float[] p1 = mLinePoints[0];
        float[] p2 = mLinePoints[1];
        double separation = Math.sqrt(
                Math.pow(p1[0] - p2[0], 2) +
                        Math.pow(p1[1] - p2[1], 2) +
                        Math.pow(p1[2] - p2[2], 2));
        if (mPointAdded) {
            mPointAdded = false;
        }
        return metersToCentimeters((float) separation);
    }

    private synchronized float metersToCentimeters(float meters) {
        return meters * 100;
    }

    private synchronized void updateUi() {
        try {
            //       mResultTextView.setText(String.format("Volume = %.0fcm X %.0fcm X %.0fcm = %.0fcm^3", mWidth, mLength, mHeight, mVolume));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateCrossHair() {
        try {
            if (isPoseValid) {
                mCrosshair.setColorFilter(getResources().getColor(R.color.brightGreen));
            } else {
                mCrosshair.setColorFilter(getResources().getColor(R.color.brightRed));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDisplayRotation() {
        Display display = getWindowManager().getDefaultDisplay();
        mDisplayRotation = display.getRotation();

        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mIsConnected) {
                    mRenderer.updateColorCameraTextureUvGlThread(mDisplayRotation);
                }
            }
        });
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
                .setMessage("Java Point to point Example requires camera permission")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(MeasurementActivity.this,
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
                Toast.makeText(MeasurementActivity.this,
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
            Toast.makeText(this, "requires camera permission",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void captureBitmap(final BitmapReadyCallbacks bitmapReadyCallbacks) {
        mSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                EGL10 egl = (EGL10) EGLContext.getEGL();
                GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
                final Bitmap snapshotBitmap = Utils.createBitmapFromGLSurface(0, 0, mSurfaceView.getWidth(), mSurfaceView.getHeight(), gl);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bitmapReadyCallbacks.onBitmapReady(snapshotBitmap);
                    }
                });

            }
        });
    }

    private interface BitmapReadyCallbacks {
        void onBitmapReady(Bitmap bitmap);
    }
}