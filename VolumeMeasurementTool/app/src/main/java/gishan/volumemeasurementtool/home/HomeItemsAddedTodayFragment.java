package gishan.volumemeasurementtool.home;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.home.adapters.ItemsAddedTodayParseQueryAdapter;
import gishan.volumemeasurementtool.home.interfaces.HomeClicker;

/**
 * Created by Gishan Don Ranasinghe
 */

public class HomeItemsAddedTodayFragment extends Fragment implements HomeClicker {

    private ListView lvAddedToday;
    private ItemsAddedTodayParseQueryAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_items_added, container, false);

        locateViews(v);
        initViews();

        return v;
    }

    private void locateViews(View v) {
        lvAddedToday = (ListView) v.findViewById(R.id.lvAddedToday);
    }

    private void initViews() {
        ParseUser user = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> relation = user.getRelation("truck");
        relation.getQuery().getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject truck, ParseException e) {
                if (truck != null) {
                    mAdapter = new ItemsAddedTodayParseQueryAdapter(getActivity(), new ItemsAddedTodayParseQueryAdapter.QueryFactory<ParseObject>() {
                        public ParseQuery<ParseObject> create() {
                            ParseQuery query = new ParseQuery("Item");
                            query.whereEqualTo("truck", truck);
                            query.orderByDescending("addedDateTime");
                            return query;
                        }
                    });
                    lvAddedToday.setAdapter(mAdapter);
                }
            }
        });
    }

    public void reloadItemsAddedToday() {
        mAdapter.loadObjects();
    }

    @Override
    public void optionsPressed(final String id, ImageButton view) {
        PopupMenu popup = new PopupMenu(getActivity(), view);
        popup.getMenuInflater().inflate(R.menu.dashboard_item_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_1:
                        Toast.makeText(getActivity(), "Clicked 1 for id [" + id + "]", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.action_2:
                        Toast.makeText(getActivity(), "Clicked 2 for id [" + id + "]", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });

        popup.show();
    }
}
