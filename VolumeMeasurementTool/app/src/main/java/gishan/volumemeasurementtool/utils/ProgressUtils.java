package gishan.volumemeasurementtool.utils;

import android.app.ProgressDialog;
import android.content.Context;

import gishan.volumemeasurementtool.R;

/**
 * Created by Gishan Don Ranasinghe
 */

public class ProgressUtils {
    public static ProgressDialog progressDialog;

    public static void showDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(context.getString(R.string.please_wait));
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void dismissDialog() {
        progressDialog.dismiss();
    }
}
