package ostukorv.qualitylab.ee.shoplist.service;

import org.json.JSONException;
import org.json.JSONObject;

public class ValidationService {
    /**
     * Check if JSON contains error
     */
    public static boolean isError(String json) throws JSONException {
        if (json.isEmpty()) {
            return true;
        }
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.has("error");
    }

    /**
     * Get error message from JSON
     */
    public static String getErrorMessage(String json) throws JSONException {
        if (!json.equals("")) {
            JSONObject jsonObj = new JSONObject(json);
            jsonObj = jsonObj.getJSONObject("error");
            return jsonObj.getString("message").replace("java.lang.AssertionError: ","");
        } else {
            return "Something went wrong, maybe you don´t have internet connection?";
        }
    }
}
