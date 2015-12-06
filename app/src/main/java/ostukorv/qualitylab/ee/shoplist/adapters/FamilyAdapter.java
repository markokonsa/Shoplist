package ostukorv.qualitylab.ee.shoplist.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import ostukorv.qualitylab.ee.shoplist.R;
import ostukorv.qualitylab.ee.shoplist.entities.Family;
import ostukorv.qualitylab.ee.shoplist.entities.ResponseObject;
import ostukorv.qualitylab.ee.shoplist.service.HttpService;
import ostukorv.qualitylab.ee.shoplist.service.ShoplistService;
import ostukorv.qualitylab.ee.shoplist.utility.Utils;

/**
 * Created by Marko on 2.12.2015.
 */
public class FamilyAdapter extends ArrayAdapter<Family> {

    private Context context;
    private List<Family> values;

    public FamilyAdapter(Context context, List<Family> values) {
        super(context, R.layout.item_shoplist, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        rowView = inflater.inflate(R.layout.item_shoplist, parent, false);
        rowView.setTag(values.get(position));
        TextView textView = (TextView) rowView.findViewById(R.id.shoplist_item_text);
        textView.setText(values.get(position).getFamilyName());

        return rowView;
    }

    public void showAlertDialog(final Family family) {
        if (HttpService.currentUser.getId() != null) {

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

            alert.setTitle("Remove family");
            alert.setMessage("Are you sure you want to remove "+family.getFamilyName());

            alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                  new RemoveFamilyTask().execute(family);
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        }
    }

    public class RemoveFamilyTask extends AsyncTask<Family, Void, ResponseObject> {

        ShoplistService service = new ShoplistService();
        Family fam;

        @Override
        protected void onPreExecute() {
            Utils.showProgressIndicator(getContext(),"Please wait...");
        }

        @Override
        protected ResponseObject doInBackground(Family... families) {
            try {
                fam = families[0];
                return service.removeFamily(fam.getId().toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            Utils.cancelProgressIndicator();
            if (response.isError()) {
                Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_LONG).show();
            }else {
                values.remove(fam);
                notifyDataSetChanged();
            }
        }
    }

}
