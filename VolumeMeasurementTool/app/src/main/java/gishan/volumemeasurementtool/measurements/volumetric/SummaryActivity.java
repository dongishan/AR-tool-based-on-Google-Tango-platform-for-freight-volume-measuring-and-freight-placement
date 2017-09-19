package gishan.volumemeasurementtool.measurements.volumetric;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.recommendations.RecommendToAnotherActivity;
import gishan.volumemeasurementtool.simulation.SimulationActivity;

/**
 * Created by Gishan Don Ranasinghe
 */

public class SummaryActivity extends Activity {

    public static final String INTENT_WIDTH = "width";
    public static final String INTENT_LENGTH = "length";
    public static final String INTENT_HEIGHT = "height";

    private Bitmap mCapturedBitmap;
    private TextView tvWidth, tvHeight, tvLength, tvItemVolume, tvRemainingVolume, tvSummary;
    private ImageView imageView;
    private float mWidth, mHeight, mLength, mItemVolume, mDimensionalWeight, mShippingGirth;
    private float mRemainingVolume = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        byte[] byteArray = getIntent().getByteArrayExtra(MeasurementActivity.CAPTURED_BITMAP);
        mCapturedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        mWidth = getIntent().getFloatExtra(MeasurementActivity.MEASURED_WIDTH, 0);
        mHeight = getIntent().getFloatExtra(MeasurementActivity.MEASURED_HEIGHT, 0);
        mLength = getIntent().getFloatExtra(MeasurementActivity.MEASURED_LENGTH, 0);


        locateViews();
        initViews();
    }

    private void locateViews() {
        imageView = (ImageView) findViewById(R.id.imageView);
        tvHeight = (TextView) findViewById(R.id.tvHeight);
        tvWidth = (TextView) findViewById(R.id.tvWidth);
        tvLength = (TextView) findViewById(R.id.tvLength);
        tvItemVolume = (TextView) findViewById(R.id.tvItemVolume);
        tvRemainingVolume = (TextView) findViewById(R.id.tvRemainingVolume);
        tvSummary = (TextView) findViewById(R.id.tvSummary);
    }

    private void initViews() {
        imageView.setImageBitmap(mCapturedBitmap);

        mItemVolume = mWidth * mLength * mHeight;
        mDimensionalWeight = mItemVolume/ 194;
        mShippingGirth = (mWidth + mHeight) * 2;

        if (hasEnoughVolume()) {
            tvRemainingVolume.setBackgroundColor(getResources().getColor(R.color.green));
            tvItemVolume.setBackgroundColor(getResources().getColor(R.color.green));
            tvSummary.setTextColor(getResources().getColor(R.color.green));
            tvSummary.setText(getResources().getString(R.string.summary_success));
        }

        tvRemainingVolume.setText(String.format(Locale.ENGLISH, "%s %.0fcm³", getResources().getString(R.string.remaining_truck_volume), mRemainingVolume));
        tvItemVolume.setText(String.format(Locale.ENGLISH, "%s %.0fcm³", getResources().getString(R.string.cubic_size), mItemVolume));
        tvHeight.setText(String.format(Locale.ENGLISH, "%s %.0fkg", getResources().getString(R.string.dimensional_weight), mDimensionalWeight));
        tvWidth.setText(String.format(Locale.ENGLISH, "%s %.0fcm", getResources().getString(R.string.shipping_girth), mShippingGirth));
        tvLength.setText(String.format(Locale.ENGLISH, "%s %.0fcm, Width: %.0fcm, Height: %.0fcm", getResources().getString(R.string.item_length), mLength, mWidth, mHeight));
    }

    private boolean hasEnoughVolume() {

        return mRemainingVolume >= mItemVolume;
    }

    public void addToTruck(View View) {
        //Collect name, addedDateTime, volume, destinationPostcode
        //TODO Add item to with pointer
    }

    public void suggestToAnother(View View) {
        Intent intent = new Intent(SummaryActivity.this, RecommendToAnotherActivity.class);
        startActivity(intent);
    }

    public void generateBlueprint(View view) {
        Intent intent = new Intent(SummaryActivity.this, BlueprintActivity.class);
        intent.putExtra(INTENT_WIDTH, mWidth);
        intent.putExtra(INTENT_LENGTH, mLength);
        startActivity(intent);
    }

    public void simulate(View View) {
        Intent intent = new Intent(SummaryActivity.this, SimulationActivity.class);
        intent.putExtra(INTENT_WIDTH, mWidth);
        intent.putExtra(INTENT_LENGTH, mLength);
        intent.putExtra(INTENT_HEIGHT, mHeight);
        startActivity(intent);
    }

    public void recalculate(View View) {
        Intent intent = new Intent(SummaryActivity.this, MeasurementActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
    }
}