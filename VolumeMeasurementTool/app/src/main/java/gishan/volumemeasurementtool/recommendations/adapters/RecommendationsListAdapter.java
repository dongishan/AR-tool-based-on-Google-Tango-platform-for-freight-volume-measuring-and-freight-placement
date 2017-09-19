package gishan.volumemeasurementtool.recommendations.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.customviews.ProgressCircleView;
import gishan.volumemeasurementtool.models.ModelItem;
import gishan.volumemeasurementtool.recommendations.interfaces.RecommendationClick;

/**
 * Created by Gishan Don Ranasinghe
 */

public class RecommendationsListAdapter extends ArrayAdapter<ModelItem> {

    private LayoutInflater mInflater;
    private int mResource;
    private RecommendationClick mClickCallback;

    public RecommendationsListAdapter(Context context, int resource, List<ModelItem> objects, RecommendationClick clickCallback) {
        super(context, resource, objects);
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        mClickCallback = clickCallback;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listelement_order, null);
            holder = new ViewHolder();
            holder.mTvShopId = (TextView) convertView.findViewById(R.id.tvShopId);
            holder.mTvName = (TextView) convertView.findViewById(R.id.tvName);
            holder.mTvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
            holder.mTvProgress = (TextView) convertView.findViewById(R.id.tvProgress);
            holder.mCircleView = (ProgressCircleView) convertView.findViewById(R.id.circleView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ModelItem item = getItem(position);

        holder.mTvShopId.setText(item.name);
        holder.mTvName.setText(getContext().getString(R.string.carrier_id, item.carrierId));
        holder.mTvDistance.setText(getContext().getString(R.string.item_distance, String.valueOf(item.distanceInKm)));
        holder.mCircleView.setMax(100);
        holder.mCircleView.setProgress(item.carrierVolume);
        holder.mCircleView.setStrokeWidth(30);
        holder.mCircleView.setProgressColour(getContext().getResources().getColor(R.color.colorAccent));
        holder.mTvProgress.setText(String.format(Locale.ENGLISH, "%d%%",item.carrierVolume));
        return convertView;
    }

    static class ViewHolder {
        private TextView mTvShopId;
        private TextView mTvName;
        private TextView mTvProgress;
        private TextView mTvDistance;
        private ProgressCircleView mCircleView;
    }
}