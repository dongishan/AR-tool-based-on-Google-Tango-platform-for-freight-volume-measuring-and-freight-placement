package gishan.volumemeasurementtool.measurements.area;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.atap.tango.reconstruction.Tango3dReconstruction;
import com.google.atap.tango.reconstruction.Tango3dReconstructionConfig;
import com.google.atap.tango.reconstruction.TangoPolygon;
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoCameraIntrinsics;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.projecttango.tangosupport.TangoPointCloudManager;
import com.projecttango.tangosupport.TangoSupport;

import java.util.List;

public class TruckSpacePlanner extends Tango.OnTangoUpdateListener {

    private static final String TAG = TruckSpacePlanner.class.getSimpleName();
    private final TangoPointCloudManager mPointCloudBuffer;

    private Tango3dReconstruction mTango3dReconstruction = null;
    private OnFloorplanAvailableListener mCallback = null;
    private HandlerThread mHandlerThread = null;
    private volatile Handler mHandler = null;

    private volatile boolean mIsFloorplanningActive = false;

    private Runnable mRunnableCallback = null;

    public TruckSpacePlanner(OnFloorplanAvailableListener callback) {
        mCallback = callback;
        Tango3dReconstructionConfig config = new Tango3dReconstructionConfig();
        config.putBoolean("use_floorplan", true);
        config.putBoolean("generate_color", false);
        config.putDouble("floorplan_max_error", 0.05);
        mTango3dReconstruction = new Tango3dReconstruction(config);
        mPointCloudBuffer = new TangoPointCloudManager();

        mHandlerThread = new HandlerThread("mesherCallback");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        if (callback != null) {
            mRunnableCallback = new Runnable() {
                @Override
                public void run() {
                    synchronized (TruckSpacePlanner.this) {
                        if (!mIsFloorplanningActive) {
                            return;
                        }

                        if (mPointCloudBuffer.getLatestPointCloud() == null) {
                            return;
                        }

                        TangoPointCloudData cloudData = mPointCloudBuffer.getLatestPointCloud();
                        TangoPoseData depthPose = TangoSupport.getPoseAtTime(cloudData.timestamp,
                                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                                TangoPoseData.COORDINATE_FRAME_CAMERA_DEPTH,
                                TangoSupport.TANGO_SUPPORT_ENGINE_TANGO,
                                TangoSupport.ROTATION_IGNORED);
                        if (depthPose.statusCode != TangoPoseData.POSE_VALID) {
                            Log.e(TAG, "couldn't extract a valid depth pose");
                            return;
                        }

                        mTango3dReconstruction.updateFloorplan(cloudData, depthPose);

                        List<TangoPolygon> polygons = mTango3dReconstruction.extractFloorplan();

                        mCallback.onFloorplanAvailable(polygons);
                    }
                }
            };
        }
    }

    public synchronized void release() {
        mIsFloorplanningActive = false;
        mTango3dReconstruction.release();
    }

    public void startFloorplanning() {
        mIsFloorplanningActive = true;
    }

    public void stopFloorplanning() {
        mIsFloorplanningActive = false;
    }

    public synchronized void resetFloorplan() {
        mTango3dReconstruction.clear();
    }

    public synchronized void setDepthCameraCalibration(TangoCameraIntrinsics calibration) {
        mTango3dReconstruction.setDepthCameraCalibration(calibration);
    }

    @Override
    public void onPoseAvailable(TangoPoseData var1) {

    }

    @Override
    public void onXyzIjAvailable(final TangoXyzIjData var1) {
    }

    @Override
    public void onPointCloudAvailable(final TangoPointCloudData tangoPointCloudData) {
        if (!mIsFloorplanningActive || tangoPointCloudData == null ||
                tangoPointCloudData.points == null) {
            return;
        }
        mPointCloudBuffer.updatePointCloud(tangoPointCloudData);
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(mRunnableCallback);
    }

    @Override
    public void onFrameAvailable(int var1) {

    }

    @Override
    public void onTangoEvent(TangoEvent var1) {

    }

    public interface OnFloorplanAvailableListener {
        void onFloorplanAvailable(List<TangoPolygon> polygons);
    }
}
