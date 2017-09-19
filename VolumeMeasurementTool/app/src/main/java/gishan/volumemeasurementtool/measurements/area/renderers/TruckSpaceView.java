package gishan.volumemeasurementtool.measurements.area.renderers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.atap.tango.reconstruction.TangoPolygon;

import java.util.ArrayList;
import java.util.List;

import gishan.volumemeasurementtool.R;

/**
 * Created by Gishan Don Ranasinghe
 */

public class TruckSpaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = TruckSpaceView.class.getSimpleName();

    private static final float SCALE = 100f;

    private volatile List<TangoPolygon> mPolygons = new ArrayList<>();

    private Paint mWallPaint;
    private Paint mSpacePaint;
    private Paint mFurniturePaint;
    private Paint mUserMarkerPaint;

    private Path mUserMarkerPath;

    private Matrix mCamera;
    private Matrix mCameraInverse;

    private boolean mIsDrawing = false;
    private SurfaceHolder mSurfaceHolder;
    private RenderThread mDrawThread;

    private DrawingCallback mCallback;

    public TruckSpaceView(Context context) {
        super(context);
        init();
    }

    public TruckSpaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TruckSpaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mWallPaint = new Paint();
        mWallPaint.setColor(getResources().getColor(android.R.color.black));
        mWallPaint.setStyle(Paint.Style.STROKE);
        mWallPaint.setStrokeWidth(3);
        mSpacePaint = new Paint();
        mSpacePaint.setColor(getResources().getColor(R.color.explored_space));
        mSpacePaint.setStyle(Paint.Style.FILL);
        mFurniturePaint = new Paint();
        mFurniturePaint.setColor(getResources().getColor(R.color.furniture));
        mFurniturePaint.setStyle(Paint.Style.FILL);
        mUserMarkerPaint = new Paint();
        mUserMarkerPaint.setColor(getResources().getColor(R.color.user_marker));
        mUserMarkerPaint.setStyle(Paint.Style.FILL);
        mUserMarkerPath = new Path();
        mUserMarkerPath.lineTo(-0.2f * SCALE, 0);
        mUserMarkerPath.lineTo(-0.2f * SCALE, -0.05f * SCALE);
        mUserMarkerPath.lineTo(0.2f * SCALE, -0.05f * SCALE);
        mUserMarkerPath.lineTo(0.2f * SCALE, 0);
        mUserMarkerPath.lineTo(0, 0);
        mUserMarkerPath.lineTo(0, -0.05f * SCALE);
        mUserMarkerPath.lineTo(-0.4f * SCALE, -0.5f * SCALE);
        mUserMarkerPath.lineTo(0.4f * SCALE, -0.5f * SCALE);
        mUserMarkerPath.lineTo(0, 0);
        mCamera = new Matrix();
        mCameraInverse = new Matrix();

        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        mIsDrawing = true;
        mDrawThread = new RenderThread();
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mIsDrawing = false;
    }

    private void doDraw(Canvas canvas) {
        if (mCallback != null) {
            mCallback.onPreDrawing();
        }

        canvas.drawColor(getResources().getColor(android.R.color.white));

        float translationX = canvas.getWidth() / 2f;
        float translationY = canvas.getHeight() / 2f;
        canvas.translate(translationX, translationY);

        canvas.concat(mCamera);

        List<TangoPolygon> drawPolygons = mPolygons;
        for (TangoPolygon polygon : drawPolygons) {
            if (polygon.vertices2d.size() > 1) {
                Paint paint;
                switch (polygon.layer) {
                    case TangoPolygon.TANGO_3DR_LAYER_FURNITURE:
                        paint = mFurniturePaint;
                        break;
                    case TangoPolygon.TANGO_3DR_LAYER_SPACE:
                        paint = mSpacePaint;
                        break;
                    case TangoPolygon.TANGO_3DR_LAYER_WALLS:
                        paint = mWallPaint;
                        break;
                    default:
                        Log.w(TAG, "Ignoring polygon with unknown layer value: " + polygon.layer);
                        continue;
                }
                Path path = new Path();
                float[] p = polygon.vertices2d.get(0);
                path.moveTo(p[0] * SCALE, p[1] * SCALE);
                for (int i = 1; i < polygon.vertices2d.size(); i++) {
                    float[] point = polygon.vertices2d.get(i);
                    path.lineTo(point[0] * SCALE, point[1] * SCALE);
                }
                if (polygon.isClosed) {
                    path.close();
                }
                canvas.drawPath(path, paint);
            }
        }

        canvas.concat(mCameraInverse);
        canvas.drawPath(mUserMarkerPath, mUserMarkerPaint);
    }

    public void setFloorplan(List<TangoPolygon> polygons) {
        mPolygons = polygons;
    }

    public void registerCallback(DrawingCallback callback) {
        mCallback = callback;
    }

    public void updateCameraMatrix(float translationX, float translationY, float yawRadians) {
        mCamera.setTranslate(-translationX * SCALE, translationY * SCALE);
        mCamera.preRotate((float) Math.toDegrees(yawRadians), translationX * SCALE, -translationY
                * SCALE);
        mCamera.invert(mCameraInverse);
    }

    public interface DrawingCallback {
        void onPreDrawing();
    }

    private class RenderThread extends Thread {
        @Override
        public void run() {
            while (mIsDrawing) {
                Canvas canvas = mSurfaceHolder.lockCanvas();
                if (canvas != null) {
                    doDraw(canvas);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
