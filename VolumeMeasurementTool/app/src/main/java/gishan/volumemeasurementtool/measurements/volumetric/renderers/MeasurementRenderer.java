package gishan.volumemeasurementtool.measurements.volumetric.renderers;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

import com.google.atap.tangoservice.TangoPoseData;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.Renderer;

import java.util.ArrayList;
import java.util.Stack;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Gishan Don Ranasinghe
 */

public class MeasurementRenderer extends Renderer {
    private static final String TAG = MeasurementRenderer.class.getSimpleName();
    public ArrayList<Object3D> mChildren;
    private float[] textureCoords0 = new float[]{0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F};
    private float[] textureCoords270 = new float[]{1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F};
    private float[] textureCoords180 = new float[]{1.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0F};
    private float[] textureCoords90 = new float[]{0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F};
    private Object3D mLine;
    private Stack<Vector3> mPoints;
    private boolean mLineUpdated = false;
    private ATexture mTangoCameraTexture;
    private boolean mSceneCameraConfigured;
    private ScreenQuad mBackgroundQuad;

    public MeasurementRenderer(Context context) {
        super(context);
    }

    @Override
    protected void initScene() {
        mChildren = new ArrayList<Object3D>();

        if (mBackgroundQuad == null) {
            mBackgroundQuad = new ScreenQuad();
            mBackgroundQuad.getGeometry().setTextureCoords(textureCoords0);
        }
        Material tangoCameraMaterial = new Material();
        tangoCameraMaterial.setColorInfluence(0);
        mTangoCameraTexture =
                new StreamingTexture("camera", (StreamingTexture.ISurfaceListener) null);
        try {
            tangoCameraMaterial.addTexture(mTangoCameraTexture);
            mBackgroundQuad.setMaterial(tangoCameraMaterial);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Exception creating texture for RGB camera contents", e);
        }
        getCurrentScene().addChildAt(mBackgroundQuad, 0);

        DirectionalLight light = new DirectionalLight(1, 0.2, -1);
        light.setColor(1, 1, 1);
        light.setPower(0.8f);
        light.setPosition(3, 2, 4);
        getCurrentScene().addLight(light);
    }

    public void updateColorCameraTextureUvGlThread(int rotation) {
        if (mBackgroundQuad == null) {
            mBackgroundQuad = new ScreenQuad();
        }

        switch (rotation) {
            case Surface.ROTATION_90:
                mBackgroundQuad.getGeometry().setTextureCoords(textureCoords90, true);
                break;
            case Surface.ROTATION_180:
                mBackgroundQuad.getGeometry().setTextureCoords(textureCoords180, true);
                break;
            case Surface.ROTATION_270:
                mBackgroundQuad.getGeometry().setTextureCoords(textureCoords270, true);
                break;
            default:
                mBackgroundQuad.getGeometry().setTextureCoords(textureCoords0, true);
                break;
        }
        mBackgroundQuad.getGeometry().reload();
    }

    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        synchronized (this) {
            if (mLineUpdated) {
                if (mPoints != null) {
                    mLine = new Line3D(mPoints, 50, Color.BLUE);

                    Material m = new Material();
                    m.setColor(Color.BLUE);
                    mLine.setMaterial(m);
                    getCurrentScene().addChild(mLine);
                    mChildren.add(mLine);
                } else {
                    mLine = null;
                }
                mLineUpdated = false;
            }
        }

        super.onRender(elapsedRealTime, deltaTime);
    }

    public synchronized void clearChildren() {
        for (Object3D child : mChildren) {
            getCurrentScene().removeChild(child);
        }
        mChildren = new ArrayList<Object3D>();
    }

    public synchronized void setLine(Stack<Vector3> points) {
        mPoints = points;
        mLineUpdated = true;
    }

    public synchronized void undoLine(Stack<Vector3> points) {
        mPoints = points;
        if (mLine != null) {
            getCurrentScene().removeChild(mLine);
            mChildren.remove(mChildren.size() - 1);
            if (mChildren.size() > 0) {
                mLine = mChildren.get(mChildren.size() - 1);
            }
        }
    }

    public void updateRenderCameraPose(TangoPoseData cameraPose) {
        float[] rotation = cameraPose.getRotationAsFloats();
        float[] translation = cameraPose.getTranslationAsFloats();
        Quaternion quaternion = new Quaternion(rotation[3], rotation[0], rotation[1], rotation[2]);
        getCurrentCamera().setRotation(quaternion.conjugate());
        getCurrentCamera().setPosition(translation[0], translation[1], translation[2]);
    }

    public int getTextureId() {
        return mTangoCameraTexture == null ? -1 : mTangoCameraTexture.getTextureId();
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
        mSceneCameraConfigured = false;
    }

    public boolean isSceneCameraConfigured() {
        return mSceneCameraConfigured;
    }

    public void setProjectionMatrix(float[] matrixFloats) {
        getCurrentCamera().setProjectionMatrix(new Matrix4(matrixFloats));
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset,
                                 float xOffsetStep, float yOffsetStep,
                                 int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }
}