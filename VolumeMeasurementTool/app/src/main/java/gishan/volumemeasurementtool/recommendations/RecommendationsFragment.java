package gishan.volumemeasurementtool.recommendations;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.models.ModelItem;
import gishan.volumemeasurementtool.models.PrototypeData;
import gishan.volumemeasurementtool.recommendations.adapters.RecommendationsListAdapter;
import gishan.volumemeasurementtool.recommendations.interfaces.RecommendationClick;

/**
 * Created by Gishan Don Ranasinghe
 */

public class RecommendationsFragment extends Fragment implements RecommendationClick {

    private ListView mLvOrders;
    private RecommendationsListAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_recommendations, container, false);

        locateViews(v);
        initViews();

        return v;
    }

    private void locateViews(View v) {
        mLvOrders = (ListView) v.findViewById(R.id.lvOrders);
    }

    private void initViews() {
        List<ModelItem> orders = PrototypeData.getOrdersDummyData();
        mListAdapter = new RecommendationsListAdapter(getActivity(), R.layout.listelement_order, orders, this);
        mLvOrders.setAdapter(mListAdapter);
    }

    @Override
    public void orderRequestedFrom(String shopId) {
        Toast.makeText(getActivity(), "Request from [" + shopId + "]", Toast.LENGTH_SHORT).show();
    }
}
