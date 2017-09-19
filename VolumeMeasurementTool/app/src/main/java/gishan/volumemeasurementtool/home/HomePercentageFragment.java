package gishan.volumemeasurementtool.home;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.Locale;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.customviews.ProgressCircleView;

/**
 * Created by Gishan Don Ranasinghe
 */

public class HomePercentageFragment extends Fragment {
    private static final String TAG = HomePercentageFragment.class.getSimpleName();

    private ProgressCircleView mCircleView;
    private TextView tvPercentage;
    private TextView tvRatio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_percentage, container, false);

        locateViews(view);

        return view;
    }

    private void locateViews(View view) {
        mCircleView = (ProgressCircleView) view.findViewById(R.id.circleView);
        tvPercentage = (TextView) view.findViewById(R.id.tvPercentage);
        tvRatio = (TextView) view.findViewById(R.id.tvRatio);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProgress();
    }

    public void updateProgress() {

        ParseUser user = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> relation = user.getRelation("truck");
        relation.getQuery().getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject truck, ParseException e) {
                if (truck != null) {
                    double maxVolume = truck.getDouble("maximumVolume");
                    double currVolume = truck.getDouble("currentVolume");
                    double percentage = currVolume / maxVolume * 100;
                    mCircleView.setProgress((int) percentage);
                    mCircleView.setProgressColour(getResources().getColor(R.color.colorPrimaryDark));
                    tvPercentage.setText(String.format(Locale.ENGLISH, "%.0f%%", percentage));
                    tvRatio.setText(String.format(Locale.ENGLISH, "%.0f/%.0fcm³", currVolume, maxVolume));
                }
            }
        });


    }
}