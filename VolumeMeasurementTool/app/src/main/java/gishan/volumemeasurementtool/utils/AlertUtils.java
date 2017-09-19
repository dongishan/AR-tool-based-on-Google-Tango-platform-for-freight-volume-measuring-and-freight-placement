package gishan.volumemeasurementtool.utils;

import android.app.AlertDialog;
import android.content.Context;

import gishan.volumemeasurementtool.R;

/**
 * Created by Gishan Don Ranasinghe
 */

public class AlertUtils {

    public static void showErrorAlert(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setTitle(R.string.error_title);
        builder.setCancelable(true);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showInfoAlert(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setTitle(R.string.version);
        builder.setCancelable(true);

        AlertDialog alert = builder.create();
        alert.show();
    }
}
