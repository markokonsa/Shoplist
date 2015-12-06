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
import ostukorv.qualitylab.ee.shoplist.entities.User;
import ostukorv.qualitylab.ee.shoplist.service.HttpService;
import ostukorv.qualitylab.ee.shoplist.service.ShoplistService;

/**
 * Created by Marko on 2.12.2015.
 */
public class MembersAdapter extends ArrayAdapter<User> {

    private final Context context;
    private final List<User> values;

    public MembersAdapter(Context context, List<User> values) {
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
        TextView textView = (TextView) rowView.findViewById(R.id.shoplist_item_text);
        textView.setText(values.get(position).getUserName());
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(values.get(position));
            }
        });
        return rowView;
    }

    public void showAlertDialog(final User user) {
        if (HttpService.currentUser.getId() != null) {

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

            alert.setTitle("Remove user");
            alert.setMessage("Are you sure you want to remove " + user.getUserName());

            alert.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    new RemoveMemberTask().execute(user);
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

    public class RemoveMemberTask extends AsyncTask<User, Void, ResponseObject> {

        ShoplistService service = new ShoplistService();
        User user;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ResponseObject doInBackground(User... users) {
            try {
                user = users[0];
                return service.removeMember(user.getId().toString());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            if (response.isError()) {
                Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_LONG).show();
            } else {
                values.remove(user);
                notifyDataSetChanged();
            }
        }
    }


}
