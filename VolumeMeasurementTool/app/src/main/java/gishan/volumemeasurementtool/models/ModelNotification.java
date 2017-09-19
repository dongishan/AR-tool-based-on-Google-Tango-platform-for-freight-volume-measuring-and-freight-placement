package gishan.volumemeasurementtool.models;

import java.util.Date;

/**
 * Created by Gishan Don Ranasinghe
 */

public class ModelNotification {
    public String id;
    public String title;
    public String address;
    public String from;
    public Date date;

    public static ModelNotification create(String id, String title, String address, String from, Date date) {
        ModelNotification notification = new ModelNotification();
        notification.id = id;
        notification.title = title;
        notification.address = address;
        notification.from = from;
        notification.date = date;
        return notification;
    }
}
