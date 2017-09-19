package gishan.volumemeasurementtool.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Gishan Don Ranasinghe
 */

public class PrototypeData {

    public static List<ModelNotification> getNotificationDummyData() {
        List<ModelNotification> notifications = new ArrayList<ModelNotification>();
        notifications.add(ModelNotification.create("1", "2 Tables with total volume of 100cm³", "No 62, Player Street, Nottingham, NG9 2BW", "Gishan Don Ranasinghe", new Date()));
        notifications.add(ModelNotification.create("2", "Sideboard with total volume of 30cm³", "Premium outlets, NG2 1DG", "Michael Smith", new Date(1454414400000L)));
        notifications.add(ModelNotification.create("3", "6 Chairs with total volume of 20cm³", "Generic Hardware store, NG7 9NB", "John Doe", new Date(1454321400000L)));
        return notifications;
    }

    public static List<ModelItem> getOrdersDummyData() {
        List<ModelItem> notifications = new ArrayList<ModelItem>();
        notifications.add(ModelItem.create("0003", "John Hamilton has 35cm³ of remaining truck space", 10.5, 70));
        notifications.add(ModelItem.create("0004", "Jacob Smith has 20cm³ of remaining truck space", 11.7, 80));
        return notifications;
    }
}
