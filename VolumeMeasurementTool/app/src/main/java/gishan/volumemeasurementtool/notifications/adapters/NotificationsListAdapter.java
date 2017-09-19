package gishan.volumemeasurementtool.notifications.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.models.ModelNotification;
import gishan.volumemeasurementtool.notifications.interfaces.NotificationClick;

/**
 * Created by Gishan Don Ranasinghe
 */

public class NotificationsListAdapter extends ArrayAdapter<ModelNotification> {

    private LayoutInflater mInflater;
    private int mResource;
    private NotificationClick mClickCallback;

    public NotificationsListAdapter(Context context, int resource, List<ModelNotification> objects, NotificationClick clicker) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        mClickCallback = clicker;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(mResource, null);
            holder = new ViewHolder();
            holder.mTvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.mTvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
            holder.mTvDetails = (TextView) convertView.findViewById(R.id.tvDetails);
            holder.mBtnAccept = (ImageButton) convertView.findViewById(R.id.btnAccept);
            holder.mBtnReject = (ImageButton) convertView.findViewById(R.id.btnReject);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy hh:mma");
        ModelNotification notification = getItem(position);
        holder.mTvTitle.setText(notification.title);
        holder.mTvAddress.setText(String.format(Locale.ENGLISH, "%s: %s", getContext().getResources().getString(R.string.pickup_location), notification.address));
        holder.mTvDetails.setText(getContext().getString(R.string.notifications_from_format,
              notification.from, df.format(notification.date)));
        holder.mBtnAccept.setOnClickListener(ClickReturner.registerAccept(notification.id, mClickCallback));
        holder.mBtnReject.setOnClickListener(ClickReturner.registerReject(notification.id, mClickCallback));

        return convertView;
    }

    private static class ViewHolder {
        private TextView mTvTitle;
        private TextView mTvAddress;
        private TextView mTvDetails;
        private ImageButton mBtnAccept;
        private ImageButton mBtnReject;
    }

    private static class ClickReturner implements View.OnClickListener {

        private String mId;
        private boolean isAccept;
        private NotificationClick mClickCallback;

        private static ClickReturner registerAccept(String id, NotificationClick clickCallback) {
            ClickReturner returner = new ClickReturner();
            returner.mId = id;
            returner.isAccept = true;
            returner.mClickCallback = clickCallback;
            return returner;
        }

        private static ClickReturner registerReject(String id, NotificationClick clickCallback) {
            ClickReturner returner = new ClickReturner();
            returner.mId = id;
            returner.isAccept = false;
            returner.mClickCallback = clickCallback;
            return returner;
        }

        @Override
        public void onClick(View v) {
            mClickCallback.clicked(mId, isAccept);
        }
    }
}