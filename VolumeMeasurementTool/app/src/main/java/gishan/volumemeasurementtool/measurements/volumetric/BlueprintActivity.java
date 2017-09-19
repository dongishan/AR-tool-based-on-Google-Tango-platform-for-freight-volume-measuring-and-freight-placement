package gishan.volumemeasurementtool.measurements.volumetric;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import java.util.Locale;

import gishan.volumemeasurementtool.R;

/**
 * Created by Gishan Don Ranasinghe
 */

public class BlueprintActivity extends Activity {

    private ImageView mDrawingImageView;
    private float mWidth = 0f;
    private float mLength = 0f;
    private Bitmap mBlueprintBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blueprint);

        mWidth = getIntent().getFloatExtra(SummaryActivity.INTENT_WIDTH, 0f);
        mLength = getIntent().getFloatExtra(SummaryActivity.INTENT_LENGTH, 0f);

        locateViews();
        generateBluePrint();
    }

    private void locateViews() {
        mDrawingImageView = (ImageView) this.findViewById(R.id.drawingImageView);
    }

    private void generateBluePrint() {
        mBlueprintBitmap = Bitmap.createBitmap(getWindowManager()
                .getDefaultDisplay().getWidth(), getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        mBlueprintBitmap.eraseColor(getResources().getColor(R.color.blueprintColor));

        Canvas canvas = new Canvas(mBlueprintBitmap);
        mDrawingImageView.setImageBitmap(mBlueprintBitmap);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[]{10, 15}, 0));

        int padding = 200;
        int textOffset = 15;

        Rect rectangle = new Rect(
                padding * 2,
                padding,
                canvas.getWidth() - padding * 2,
                canvas.getHeight() - padding
        );
        canvas.drawRect(rectangle, paint);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);
        textPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(String.format(Locale.ENGLISH, "%.0fcm", mWidth), canvas.getWidth() / 2, padding - textOffset, textPaint);
        canvas.drawText(String.format(Locale.ENGLISH, "%.0fcm", mLength), (padding * 2) - (textOffset * 6), canvas.getHeight() / 2, textPaint);
    }

    public void share(View view) {
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), mBlueprintBitmap, "Blueprint", null);
        Uri bmpUri = Uri.parse(path);
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Blueprint of the measurement");
        shareIntent.setType("image/png");
        startActivity(shareIntent);
    }
}
