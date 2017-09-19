package gishan.volumemeasurementtool.models;

import java.util.Date;

/**
 * Created by Gishan Don Ranasinghe
 */

public class ModelSale {
    public String id;
    public String title;
    public Date time;
    public double price;

    public static ModelSale create(String id, String title, Date time, double price) {
        ModelSale sale = new ModelSale();
        sale.id = id;
        sale.title = title;
        sale.time = time;
        sale.price = price;
        return sale;
    }
}
