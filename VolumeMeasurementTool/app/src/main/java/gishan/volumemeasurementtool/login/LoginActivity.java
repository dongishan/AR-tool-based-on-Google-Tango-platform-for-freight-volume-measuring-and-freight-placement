package gishan.volumemeasurementtool.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import gishan.volumemeasurementtool.R;
import gishan.volumemeasurementtool.home.HomeActivity;
import gishan.volumemeasurementtool.utils.AlertUtils;
import gishan.volumemeasurementtool.utils.ProgressUtils;

/**
 * Created by Gishan Don Ranasinghe
 */

public class LoginActivity extends Activity {

    private EditText edDriverId;
    private EditText edPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ParseUser.getCurrentUser() != null) {
            goToHome();
        } else {
            locateViews();
        }
    }

    private void locateViews() {
        edDriverId = (EditText) findViewById(R.id.edDriverId);
        edPassword = (EditText) findViewById(R.id.edPassword);
    }

    public void onLogin(View view) {
        String driverId = edDriverId.getText().toString();
        String password = edPassword.getText().toString();

        ProgressUtils.showDialog(LoginActivity.this);

        ParseUser.logInInBackground(driverId, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                ProgressUtils.dismissDialog();
                if (user != null) {
                    goToHome();
                } else {
                    AlertUtils.showErrorAlert(LoginActivity.this, e.getMessage());
                }
            }
        });
    }

    public void onAboutPressed(View view) {
        AlertUtils.showInfoAlert(LoginActivity.this, getResources().getString(R.string.version_number));
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}