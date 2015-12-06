package ostukorv.qualitylab.ee.shoplist.utility;

public class Constants {

    /**
     * Base Api url
     */
    public static final String baseApiUrl = "https://shoppinglistapi.appspot.com/_ah/api/shoppingList/v1/";

    /**
     * Items Api url
     */
    public static final String itemsApiUrl = baseApiUrl + "item";

    /**
     * Family api url
     */
    public static final String familyApiUrl = baseApiUrl + "familiy/";

    /**
     * Shoppinglist api url
     */
    public static final String shoppinglistApiUrl = baseApiUrl + "shoppinglist/";

    /**
     * Get shoppinglists
     */
    public static final String getShoppinglistsUrl = baseApiUrl + "shoppinglists";

    /**
     * Login url
     * Post request with params username, password;
     */
    public static final String loginUrl = baseApiUrl + "login";

    /**
     * register url
     * Post request with params username, password;
     */
    public static final String registerUrl = baseApiUrl + "register";

    /**
     * Add family url
     * Post request with param familyName
     */
    public static final String addFamilyUrl = baseApiUrl + "familiy/add";

    /**
     * Add family member url
     * Have to add /{familyId}/add to url
     * Post request with param newUserId
     */
    public static final String addFamilyMember = baseApiUrl + "familiy/";
    /**
     * Update shoppinglist item url
     * Have to add itemid to url
     */
    public static final String shoppingListItemUpdateUrl = baseApiUrl + "update";
}

