package ostukorv.qualitylab.ee.shoplist.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShoppingListItem {

    public ShoppingListItem(){}

    Long id;
    String itemName;
    int qty;
    boolean completed;
    Long listId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.completed = isCompleted;
    }

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }
}
