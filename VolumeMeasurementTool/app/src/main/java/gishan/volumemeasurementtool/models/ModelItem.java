package gishan.volumemeasurementtool.models;

/**
 * Created by Gishan Don Ranasinghe
 */

public class ModelItem {
    public String carrierId;
    public String name;
    public double distanceInKm;
    public int carrierVolume;

    public static ModelItem create(String carrierId, String name, double distance, int carrierVolume) {
        ModelItem order = new ModelItem();
        order.carrierId = carrierId;
        order.name = name;
        order.distanceInKm = distance;
        order.carrierVolume = carrierVolume;
        return order;
    }
}
