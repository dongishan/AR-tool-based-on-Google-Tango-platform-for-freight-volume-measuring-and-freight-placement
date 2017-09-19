package gishan.volumemeasurementtool.recommendations;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
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

public class RecommendToAnotherActivity extends Activity implements RecommendationClick {

    private ListView mLvOrders;
    private RecommendationsListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        locateViews();
        initViews();

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void locateViews() {
        mLvOrders = (ListView) findViewById(R.id.lvOrders);
    }

    private void initViews() {
        List<ModelItem> orders = PrototypeData.getOrdersDummyData();
        mListAdapter = new RecommendationsListAdapter(getApplicationContext(), R.layout.listelement_order, orders, this);
        mLvOrders.setAdapter(mListAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void orderRequestedFrom(String shopId) {
        Toast.makeText(getApplicationContext(), "Request from [" + shopId + "]", Toast.LENGTH_SHORT).show();
    }
}