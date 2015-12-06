package ostukorv.qualitylab.ee.shoplist.utility;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by Marko on 2.12.2015.
 */
public class Utils {

    private static ProgressDialog mDialog;
    public static final String MyPREFERENCES = "MyPrefs";

    public static void showProgressIndicator(Context context, String message) {
        mDialog = new ProgressDialog(context);
        mDialog.setMessage(message);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    public static void cancelProgressIndicator() {
        if (mDialog != null) {
            mDialog.cancel();
        }
    }

}
