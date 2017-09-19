package gishan.volumemeasurementtool.notifications;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.List;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.models.ModelNotification;
import gishan.volumemeasurementtool.models.PrototypeData;
import gishan.volumemeasurementtool.notifications.adapters.NotificationsListAdapter;
import gishan.volumemeasurementtool.notifications.interfaces.NotificationClick;

/**
 * Created by Gishan Don Ranasinghe
 */

public class NotificationsActivity extends Activity implements NotificationClick {

    private ListView mLvNotifications;
    private NotificationsListAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        locateViews();
        initViews();

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void locateViews() {
        mLvNotifications = (ListView) findViewById(R.id.lvNotifications);
    }

    private void initViews() {
        List<ModelNotification> exampleData = PrototypeData.getNotificationDummyData();
        mListAdapter = new NotificationsListAdapter(getApplicationContext(), R.layout.listelement_notifications, exampleData, this);
        mLvNotifications.setAdapter(mListAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void clicked(String notificationId, boolean wasAccept) {

    }
}
