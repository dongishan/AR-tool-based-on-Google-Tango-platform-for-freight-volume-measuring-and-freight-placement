package gishan.volumemeasurementtool;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.parse.interceptors.ParseLogInterceptor;

/**
 * Created by Gishan Don Ranasinghe
 */

public class SmartTruckApplication extends Application {
    private static final String TAG = SmartTruckApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("volume-measurement-tool")
                .clientKey(null)
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server("https://vmt-tango.herokuapp.com/parse").build());

        //registerUserForParse("TDR-0001", "0001", "dongishan@gmail.com", "Gishan Don Ranasinghe");
        //registerUserForParse("TDR-0002", "0002", "psygd@exmail.nottingham.ac.uk", "Daniel Anderson");
    }

    private void registerUserForParse(String username, String password, String email, String name) {

        final ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("name", name);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Log.d(TAG, "user registration failed: " + e.getMessage());
                } else {
                    final ParseObject truck = new ParseObject("Truck");
                    truck.put("area", 500f);
                    truck.put("maximumVolume", 1000f);
                    truck.put("currentVolume", 0f);

                    truck.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.d(TAG, "truck registration failed: " + e.getMessage());
                            } else {
                                ParseRelation<ParseObject> relation = user.getRelation("truck");
                                relation.add(truck);
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.d(TAG, "truck registration failed: " + e.getMessage());
                                        } else {
                                            Log.d(TAG, "Truck add to the <truck> relation");
                                        }
                                    }
                                });

                            }
                        }
                    });

                }
            }
        });
    }
}