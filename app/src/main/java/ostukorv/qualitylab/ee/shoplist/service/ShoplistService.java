package ostukorv.qualitylab.ee.shoplist.service;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ostukorv.qualitylab.ee.shoplist.activities.FamiliesActivity;
import ostukorv.qualitylab.ee.shoplist.entities.Family;
import ostukorv.qualitylab.ee.shoplist.entities.ResponseObject;
import ostukorv.qualitylab.ee.shoplist.entities.Session;
import ostukorv.qualitylab.ee.shoplist.entities.ShoppingList;
import ostukorv.qualitylab.ee.shoplist.entities.ShoppingListItem;
import ostukorv.qualitylab.ee.shoplist.entities.User;

public class ShoplistService {

    private HttpService service;
    private ObjectMapper mapper;

    public ShoplistService() {
        service = new HttpService();
        mapper = new ObjectMapper();
    }

    public ResponseObject auth(String url, String username, String password) throws IOException, JSONException {
        String json = service.authentication(url, username, password);
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        Session session = mapper.readValue(json, Session.class);
        HttpService.session = session;
        HttpService.currentUser = mapper.readValue(service.getUser(username),User.class);

        return new ResponseObject(false,"", Collections.singletonList(session));
    }

    public ResponseObject getShoppinglists() throws IOException, JSONException {
        String json = service.getShoppingLists();
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        json = new JSONObject(json).getJSONArray("items").toString();
        List<Family> families = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Family.class));
        return new ResponseObject(false,"",families);
    }

    public ResponseObject getMembers(String familyId) throws IOException, JSONException {
        String json = service.getMembers(familyId);
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        json = new JSONObject(json).getJSONArray("items").toString();
        List<User> members = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, User.class));
        return new ResponseObject(false,"",members);
    }

    public ResponseObject getItems(String shoppinglistId) throws IOException, JSONException {
        String json = service.getItems(shoppinglistId);
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        json = new JSONObject(json).getJSONArray("items").toString();
        List<ShoppingListItem> items = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, ShoppingListItem.class));
        return new ResponseObject(false,"", items);
    }

    public ResponseObject updateItem(String famId, String itemId, boolean complete) throws IOException, JSONException {
        String json = service.updateItem(famId, itemId, complete);
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        ShoppingListItem item =  mapper.readValue(json,ShoppingListItem.class);
        return new ResponseObject(false,"", Collections.singletonList(item));
    }

    public ResponseObject removeCheckedItems(String famId,List<ShoppingListItem> items) throws IOException, JSONException {
        String json;
        for (ShoppingListItem item : items) {
            json = service.removeItem(famId, item.getListId(), item.getId());
            if (ValidationService.isError(json)){
                String message = ValidationService.getErrorMessage(json);
                return new ResponseObject(true,message,null);
            }
        }
        return new ResponseObject(false,"", null);
    }

    public ResponseObject addItem(String famId,String listId,int count,String itemName) throws JSONException, IOException {
        String json = service.addItem(famId,listId,count,itemName);
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        ShoppingListItem item = mapper.readValue(json,ShoppingListItem.class);
        return new ResponseObject(false,"", Collections.singletonList(item));
    }

    public ResponseObject addFamily(String familyName) throws JSONException, IOException {
        String json = service.addFamily(familyName);
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        Family family = mapper.readValue(json, Family.class);
        return new ResponseObject(false,"", Collections.singletonList(family));
    }

    public ResponseObject addShoppinglist(String famId,String name) throws JSONException, IOException {
        String json = service.addShoppingList(famId, name);
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        ShoppingList list = mapper.readValue(json, ShoppingList.class);
        return new ResponseObject(false,"", Collections.singletonList(list));
    }

    public ResponseObject addMember(String famId,String username) throws IOException, JSONException {
        String userJson = service.getUser(username);
        if (ValidationService.isError(userJson)){
            String message = ValidationService.getErrorMessage(userJson);
            return new ResponseObject(true,message,null);
        }
        User user = mapper.readValue(userJson, User.class);
        String json = service.addFamilyMember(famId, user.getId().toString());
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        User u = mapper.readValue(json, User.class);
        return new ResponseObject(false,"", Collections.singletonList(u));
    }

    public ResponseObject removeFamily(String famId) throws IOException, JSONException {
        String json = service.removeFamily(famId);
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        return new ResponseObject(false,"",null);
    }

    public ResponseObject removeShoppinglist(String listId) throws IOException, JSONException {
        String json = service.removeShoppingList(FamiliesActivity.family.getId().toString(), listId);
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        return new ResponseObject(false,"",null);
    }

    public ResponseObject removeMember(String userId) throws IOException, JSONException {
        String json = service.removeFamilyMember(FamiliesActivity.family.getId().toString(),userId);
        if (ValidationService.isError(json)){
            String message = ValidationService.getErrorMessage(json);
            return new ResponseObject(true,message,null);
        }
        return new ResponseObject(false,"",null);
    }
}
