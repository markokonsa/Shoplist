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
import ostukorv.qualitylab.ee.shoplist.entities.ResponseObject;
import ostukorv.qualitylab.ee.shoplist.entities.ShoppingList;
import ostukorv.qualitylab.ee.shoplist.service.HttpService;
import ostukorv.qualitylab.ee.shoplist.service.ShoplistService;
import ostukorv.qualitylab.ee.shoplist.utility.Utils;

public class ShoplistsAdapter extends ArrayAdapter<ShoppingList> {

    private Context context;
    private List<ShoppingList> values;

    public ShoplistsAdapter(Context context, List<ShoppingList> values) {
        super(context, R.layout.item_shoplist, values);
        this.context = context;
        this.values = values;
    }

    public void setList(List<ShoppingList> list){
        this.values = list;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        rowView = inflater.inflate(R.layout.item_shoplist, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.shoplist_item_text);
        textView.setText(values.get(position).getShoppingListName());
        return rowView;
    }

    public void showAlertDialog(final ShoppingList shoppingList) {
        if (HttpService.currentUser.getId() != null) {

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

            alert.setTitle("Remove list");
            alert.setMessage("Are you sure you want to remove "+shoppingList.getShoppingListName());

            alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    new RemoveListTask().execute(shoppingList);
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

    public class RemoveListTask extends AsyncTask<ShoppingList, Void, ResponseObject> {

        ShoplistService service = new ShoplistService();
        ShoppingList l;

        @Override
        protected void onPreExecute() {
            Utils.showProgressIndicator(getContext(),"Please wait...");
        }

        @Override
        protected ResponseObject doInBackground(ShoppingList... lists) {
            try {
                l = lists[0];
                return service.removeShoppinglist(l.getId().toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            if (response.isError()) {
                Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_LONG).show();
            }else {
                values.remove(l);
                notifyDataSetChanged();
            }
            Utils.cancelProgressIndicator();
        }
    }
}
