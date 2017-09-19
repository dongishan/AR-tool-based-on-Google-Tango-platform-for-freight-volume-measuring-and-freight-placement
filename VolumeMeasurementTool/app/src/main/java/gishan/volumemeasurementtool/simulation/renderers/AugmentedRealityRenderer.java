package gishan.volumemeasurementtool.simulation.renderers;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import com.google.atap.tangoservice.TangoPoseData;
import com.projecttango.tangosupport.TangoSupport;

import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.RectangularPrism;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.renderer.Renderer;

import javax.microedition.khronos.opengles.GL10;

import gishan.volumemeasurementtool.R;

/**
 * Created by Gishan Don Ranasinghe
 */

public class AugmentedRealityRenderer extends Renderer {
    private static final String TAG = AugmentedRealityRenderer.class.getSimpleName();

    private float[] textureCoords0 = new float[]{0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F};

    private ATexture mTangoCameraTexture;

    private boolean mSceneCameraConfigured;

    private ScreenQuad mBackgroundQuad;
    private float mWidth, mHeight, mLength;

    public AugmentedRealityRenderer(Context context, float width, float height, float length) {
        super(context);
        this.mHeight = height;
        this.mLength = length;
        this.mWidth = width;
    }

    @Override
    protected void initScene() {
        Material tangoCameraMaterial = new Material();
        tangoCameraMaterial.setColorInfluence(0);

        if (mBackgroundQuad == null) {
            mBackgroundQuad = new ScreenQuad();
            mBackgroundQuad.getGeometry().setTextureCoords(textureCoords0);
        }
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

        Material itemMaterial = new Material();
        try {
            Texture t = new Texture("earth", R.drawable.package_texture);
            itemMaterial.addTexture(t);
        } catch (ATexture.TextureException e) {
            Log.e(TAG, "Exception generating earth texture", e);
        }
        itemMaterial.setColorInfluence(0);
        itemMaterial.enableLighting(true);
        itemMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

        RectangularPrism item = new RectangularPrism(mWidth / 100, mHeight / 100, mLength / 100);
        item.setMaterial(itemMaterial);
        item.setPosition(0, 0, -5);
        getCurrentScene().addChild(item);

        Animation3D animation = new RotateOnAxisAnimation(Vector3.Axis.Y, 0, -360);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDurationMilliseconds(30000);
        animation.setRepeatMode(Animation.RepeatMode.INFINITE);
        animation.setTransformable3D(item);
        getCurrentScene().registerAnimation(animation);
        animation.play();
    }

    public void updateColorCameraTextureUvGlThread(int rotation) {
        if (mBackgroundQuad == null) {
            mBackgroundQuad = new ScreenQuad();
        }

        float[] textureCoords =
                TangoSupport.getVideoOverlayUVBasedOnDisplayRotation(textureCoords0, rotation);
        mBackgroundQuad.getGeometry().setTextureCoords(textureCoords, true);
        mBackgroundQuad.getGeometry().reload();
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