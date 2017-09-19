package gishan.volumemeasurementtool.notifications;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class NotificationsFragment extends Fragment implements NotificationClick {

    private ListView mLvNotifications;
    private NotificationsListAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_notification, container, false);

        locateViews(v);
        initViews(v.getContext());

        return v;
    }

    private void locateViews(View v) {
        mLvNotifications = (ListView) v.findViewById(R.id.lvNotifications);
    }

    private void initViews(Context context) {
        List<ModelNotification> exampleData = PrototypeData.getNotificationDummyData();
        mListAdapter = new NotificationsListAdapter(context, R.layout.listelement_notifications, exampleData, this);
        mLvNotifications.setAdapter(mListAdapter);
    }

    @Override
    public void clicked(String notificationId, boolean wasAccept) {

    }
}
