package ostukorv.qualitylab.ee.shoplist.service;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import ostukorv.qualitylab.ee.shoplist.entities.Session;
import ostukorv.qualitylab.ee.shoplist.entities.User;
import ostukorv.qualitylab.ee.shoplist.utility.Constants;

public class HttpService {

    public static Session session = new Session();
    public static User currentUser = new User();

    /**
     * Login and register
     */
    public String authentication(String url, String username, String password) throws IOException {
        List<NameValuePair> valuePairs = new ArrayList<>();
        valuePairs.add(new BasicNameValuePair("username", username));
        valuePairs.add(new BasicNameValuePair("password", password));
        return doPost(url, valuePairs);
    }

    /**
     * Add family
     **/
    public String addFamily(String familyName) throws IOException {
        List<NameValuePair> valuePairs = new ArrayList<>();
        valuePairs.add(new BasicNameValuePair("familyName", familyName));
        return doPost(Constants.addFamilyUrl, valuePairs);
    }

    /**
     * Add member to family
     */
    public String addFamilyMember(String familyId, String userId) throws IOException {
        String url = Constants.addFamilyMember + familyId + "/add";
        List<NameValuePair> valuePairs = new ArrayList<>();
        valuePairs.add(new BasicNameValuePair("newUserId", userId));
        return doPost(url, valuePairs);
    }

    /**
     * Add item to shoppinglist
     */
    public String addItem(String famId, String shoppingListId, int count, String itemName) throws IOException {
        String url = Constants.familyApiUrl + famId + "/shoppinglist/" + shoppingListId + "/add/" + count;
        List<NameValuePair> valuePairs = new ArrayList<>();
        valuePairs.add(new BasicNameValuePair("itemName", itemName));
        return doPost(url, valuePairs);
    }

    /**
     * Add shoppinglist to family
     */
    public String addShoppingList(String famId, String named) throws IOException {
        String url = Constants.familyApiUrl + famId + "/shoppinglist/add";
        List<NameValuePair> valuePairs = new ArrayList<>();
        valuePairs.add(new BasicNameValuePair("named", named));
        return doPost(url, valuePairs);
    }

    /**
     * Get items from shoppinglist
     */
    public String getItems(String shoppinglistId) throws IOException, JSONException {
        String url = Constants.shoppinglistApiUrl + shoppinglistId + "/getItems";
        String response = doGet(url);
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.toString();
    }

    /**
     * Get User (without password)
     */
    public String getUser(String name) throws IOException, JSONException {
        String url = Constants.baseApiUrl + "user/" + name;
        String response = doGet(url);
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.toString();
    }

    /**
     * Get members of the family
     */
    public String getMembers(String familyId) throws IOException, JSONException {
        String url = Constants.familyApiUrl + familyId + "/members";
        String response = doGet(url);
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.toString();
    }

    /**
     * Get families and shoppinglist for logged in user
     */
    public String getShoppingLists() throws IOException, JSONException {
        String response = doGet(Constants.getShoppinglistsUrl);
        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.toString();
    }

    /**
     * Remove family member
     */
    public String removeFamilyMember(String famId, String userId) throws IOException {
        String url = Constants.familyApiUrl + famId + "/remove";
        List<NameValuePair> valuePairs = new ArrayList<>();
        valuePairs.add(new BasicNameValuePair("userId", userId));
        return doPost(url, valuePairs);
    }

    /**
     * Remove family
     */
    public String removeFamily(String famId) throws IOException {
        String url = Constants.familyApiUrl + "remove";
        List<NameValuePair> valuePairs = new ArrayList<>();
        valuePairs.add(new BasicNameValuePair("famId", famId));
        return doPost(url, valuePairs);
    }

    /**
     * Remove shoppinglist item
     */
    public String removeItem(String famId, Long listId, Long itemId) throws IOException {
        String url = Constants.familyApiUrl + famId + "/shoppinglist/" + listId + "/remove/" + itemId;
        return doGet(url);
    }

    /**
     * Remove shoppinglist
     */
    public String removeShoppingList(String famId, String listId) throws IOException {
        String url = Constants.familyApiUrl + famId + "/shoppinglist/remove/" + listId;
        return doGet(url);
    }

    /**
     * Update item
     */
    public String updateItem(String famId, String shoppinItemId, boolean complete) throws JSONException, IOException {
        List<NameValuePair> valuePairs = new ArrayList<>();
        valuePairs.add(new BasicNameValuePair("famId", famId));
        valuePairs.add(new BasicNameValuePair("shoppingItemId", shoppinItemId));
        if (complete) {
            valuePairs.add(new BasicNameValuePair("completed", "True"));
        }else {
            valuePairs.add(new BasicNameValuePair("completed", "False"));
        }

        return doPost(Constants.shoppingListItemUpdateUrl, valuePairs);
    }

    private String doGet(String url) throws IOException {

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        // replace with your url

        HttpResponse response = null;
        try {
            if (session.getSessionKey() != null) {
                request.setHeader("X-API-KEY", session.getSessionKey());
            }
            response = client.execute(request);
            request.setHeader("Content-Type", "application/json; charset=UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return EntityUtils.toString(response.getEntity());
    }


    private String doPost(String url, List<NameValuePair> nameValuePairList) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        try {
            if (session.getSessionKey() != null) {
                httpPost.setHeader("X-API-KEY", session.getSessionKey());
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EntityUtils.toString(response.getEntity());
    }

}
