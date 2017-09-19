package gishan.volumemeasurementtool.home.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import java.text.SimpleDateFormat;
import java.util.Locale;

import gishan.volumemeasurementtool.R;

/**
 * Created by Gishan on 25/02/2017.
 */

public class ItemsAddedTodayParseQueryAdapter extends ParseQueryAdapter {

    public ItemsAddedTodayParseQueryAdapter(Context context, Class clazz) {
        super(context, clazz);
    }

    public ItemsAddedTodayParseQueryAdapter(Context context, String className) {
        super(context, className);
    }

    public ItemsAddedTodayParseQueryAdapter(Context context, Class clazz, int itemViewResource) {
        super(context, clazz, itemViewResource);
    }

    public ItemsAddedTodayParseQueryAdapter(Context context, String className, int itemViewResource) {
        super(context, className, itemViewResource);
    }

    public ItemsAddedTodayParseQueryAdapter(Context context, QueryFactory queryFactory) {
        super(context, queryFactory);
    }

    public ItemsAddedTodayParseQueryAdapter(Context context, QueryFactory queryFactory, int itemViewResource) {
        super(context, queryFactory, itemViewResource);
    }

    @Override
    public View getItemView(final ParseObject item, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.listelement_added_today, null);
        }

        super.getItemView(item, v, parent);
        TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        TextView tvDestination = (TextView) v.findViewById(R.id.tvDestination);
        TextView tvVolume = (TextView) v.findViewById(R.id.tvVolume);
        ImageButton imgBtnOptions = (ImageButton) v.findViewById(R.id.imgbtnOptions);

        SimpleDateFormat df = new SimpleDateFormat("hh:mma");
        tvTitle.setText(String.format(Locale.ENGLISH, "%s at %s", item.getString("name"), df.format(item.getDate("addedDateTime"))));
        tvDestination.setText(String.format("%s: %s", getContext().getResources().getString(R.string.destination), item.getString("destinationPostcode")));
        tvVolume.setText(String.format(Locale.ENGLISH, "%.0fcm³", item.getDouble("volume")));
        imgBtnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = "google.navigation:q=" + item.getString("destinationPostcode");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
                getContext().startActivity(intent);
            }
        });
        return v;
    }

}
