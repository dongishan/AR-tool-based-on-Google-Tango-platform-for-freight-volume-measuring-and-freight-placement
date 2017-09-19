package gishan.volumemeasurementtool.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.models.ModelMenuItem;

/**
 * Created by Gishan Don Ranasinghe
 */

public class HomeListAdapter extends ArrayAdapter<ModelMenuItem> {

    private LayoutInflater mInflater;
    private int mLayoutResource;

    public HomeListAdapter(Context context, int resource, List<ModelMenuItem> objects) {
        super(context, resource, objects);
        this.mInflater = LayoutInflater.from(context);
        this.mLayoutResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResource, null);
            holder = new ViewHolder();
            holder.mTvOption = (TextView) convertView.findViewById(R.id.tvOption);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ModelMenuItem item = getItem(position);
        holder.mTvOption.setText(item.name);

        return convertView;
    }

    public static class ViewHolder {
        public TextView mTvOption;
    }
}
