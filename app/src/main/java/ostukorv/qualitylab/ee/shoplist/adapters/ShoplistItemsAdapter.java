package ostukorv.qualitylab.ee.shoplist.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

import ostukorv.qualitylab.ee.shoplist.R;
import ostukorv.qualitylab.ee.shoplist.activities.FamiliesActivity;
import ostukorv.qualitylab.ee.shoplist.entities.ResponseObject;
import ostukorv.qualitylab.ee.shoplist.entities.ShoppingListItem;
import ostukorv.qualitylab.ee.shoplist.service.ShoplistService;

/**
 * Created by Marko on 2.12.2015.
 */
public class ShoplistItemsAdapter extends ArrayAdapter<ShoppingListItem> {
    private final Context context;
    private final List<ShoppingListItem> values;

    public ShoplistItemsAdapter(Context context, List<ShoppingListItem> values) {
        super(context, R.layout.item_shoplistitem, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView;
        rowView = inflater.inflate(R.layout.item_shoplistitem, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.shoplist_item_text);
        textView.setText(values.get(position).getItemName()+" ["+values.get(position).getQty()+"]");
        final CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.shotlist_item_checkbox);
        checkBox.setChecked(values.get(position).isCompleted());
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                }
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    if (checkBox.isChecked()) {
                                                        values.get(position).setIsCompleted(true);
                                                    } else {
                                                        values.get(position).setIsCompleted(false);
                                                    }
                                                    new UpdateItemTask().execute(values.get(position));
                                                }
                                            }
        );
        return rowView;
    }

    public class UpdateItemTask extends AsyncTask<ShoppingListItem, Void, ResponseObject> {

        ShoplistService service = new ShoplistService();

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ResponseObject doInBackground(ShoppingListItem... items) {
            try {
                ShoppingListItem item = items[0];
                return service.updateItem(FamiliesActivity.family.getId().toString(), item.getId().toString(), item.isCompleted());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            if (response.isError()) {
                Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
