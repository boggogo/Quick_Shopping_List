package koemdzhiev.com.quickshoppinglist.utils;

/**
 * Created by koemdzhiev on 19/06/2015.
 */
public class Item {
    private String itemDescription;

    public Item(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public Item() {
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }
}
