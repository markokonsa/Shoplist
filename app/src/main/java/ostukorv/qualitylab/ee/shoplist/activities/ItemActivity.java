package ostukorv.qualitylab.ee.shoplist.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ostukorv.qualitylab.ee.shoplist.R;
import ostukorv.qualitylab.ee.shoplist.adapters.ShoplistItemsAdapter;
import ostukorv.qualitylab.ee.shoplist.entities.Family;
import ostukorv.qualitylab.ee.shoplist.entities.ResponseObject;
import ostukorv.qualitylab.ee.shoplist.entities.ShoppingListItem;
import ostukorv.qualitylab.ee.shoplist.service.ShoplistService;
import ostukorv.qualitylab.ee.shoplist.utility.Utils;

/**
 * Created by Marko on 2.12.2015.
 */
public class ItemActivity extends AppCompatActivity {

    ShoplistItemsAdapter adapter;
    ListView itemsList;
    List<ShoppingListItem> items = new ArrayList<>();
    Date lastTry;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        setTitle(intent.getStringExtra("name"));
        new ShoppinglistItemsTask().execute(intent.getStringExtra("id"));
        itemsList = (ListView) findViewById(R.id.shoppinglistsitems_listview);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_item);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog("");
            }
        });

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new ShoppinglistItemsTask().execute(getIntent().getStringExtra("id"));
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        if (lastTry != null) {
            new ShoppinglistItemsTask().execute(getIntent().getStringExtra("id"));
        }
    }

    private void showAlertDialog(String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ItemActivity.this);

        alert.setTitle("Add new item");
        alert.setMessage("Enter item name and count");

        LinearLayout ll = new LinearLayout(ItemActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);

        // Set an EditText view to get user input
        final EditText name = new EditText(ItemActivity.this);
        name.setHint("Item name");
        final EditText count = new EditText(ItemActivity.this);
        count.setHint("How many?");
        count.setInputType(InputType.TYPE_CLASS_NUMBER);
        final TextView errorText = new TextView(ItemActivity.this);
        if (!message.isEmpty()) {
            errorText.setText(message);
            errorText.setTextColor(Color.RED);
            errorText.setPadding(10, 0, 10, 0);
        }

        ll.addView(name);
        ll.addView(count);
        ll.addView(errorText);

        alert.setView(ll);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ShoppingListItem i = new ShoppingListItem();
                if (!count.getText().toString().isEmpty()){
                    i.setQty(Integer.valueOf(count.getText().toString()));
                }
                i.setItemName(name.getText().toString());
                i.setListId(Long.valueOf(getIntent().getStringExtra("id")));
                new ShoppinglistItemAddTask().execute(i);

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear) {
            List<ShoppingListItem> checkedItems = new ArrayList<>();
            for (ShoppingListItem i : items) {
                if (i.isCompleted()) checkedItems.add(i);
            }
            new ShoppinglistItemsRemoveTask().execute(checkedItems);
        } else if (id == R.id.action_refresh) {
            new ShoppinglistItemsTask().execute(getIntent().getStringExtra("id"));
        }
        return super.onOptionsItemSelected(item);
    }

    public class ShoppinglistItemsTask extends AsyncTask<String, Void, ResponseObject> {

        ShoplistService service = new ShoplistService();

        @Override
        protected void onPreExecute() {
            Utils.showProgressIndicator(ItemActivity.this, "Please wait...");
        }

        @Override
        protected ResponseObject doInBackground(String... params) {
            try {
                return service.getItems(params[0]);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            swipeContainer.setRefreshing(false);
            lastTry = new Date();
            if (response != null) {
                if (response.isError()) {

                    Toast.makeText(ItemActivity.this, response.getErrorMessage(), Toast.LENGTH_LONG).show();
                } else {
                    items = (List<ShoppingListItem>) response.getObjects();
                    adapter = new ShoplistItemsAdapter(ItemActivity.this, items);
                    itemsList.setAdapter(adapter);
                }
            } else {
                items = new ArrayList<>();
                adapter = new ShoplistItemsAdapter(ItemActivity.this, items);
                itemsList.setAdapter(adapter);
            }
            Utils.cancelProgressIndicator();
        }
    }

    public class ShoppinglistItemsRemoveTask extends AsyncTask<List<ShoppingListItem>, Void, ResponseObject> {

        ShoplistService service = new ShoplistService();

        @Override
        protected void onPreExecute() {
            Utils.showProgressIndicator(ItemActivity.this, "Please wait...");
        }

        @Override
        protected ResponseObject doInBackground(List<ShoppingListItem>... params) {
            try {
                Family family = FamiliesActivity.family;
                List<ShoppingListItem> items = params[0];
                return service.removeCheckedItems(family.getId().toString(), items);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            Utils.cancelProgressIndicator();
            if (response.isError()) {
                Toast.makeText(ItemActivity.this, response.getErrorMessage(), Toast.LENGTH_LONG).show();
            } else {
                new ShoppinglistItemsTask().execute(getIntent().getStringExtra("id"));
            }
        }
    }

    public class ShoppinglistItemAddTask extends AsyncTask<ShoppingListItem, Void, ResponseObject> {

        ShoplistService service = new ShoplistService();

        @Override
        protected void onPreExecute() {
            Utils.showProgressIndicator(ItemActivity.this, "Please wait...");
        }

        @Override
        protected ResponseObject doInBackground(ShoppingListItem... params) {
            try {
                Family family = FamiliesActivity.family;
                ShoppingListItem i = params[0];
                if (i.getQty()==0 || i.getItemName().isEmpty()){
                    return new ResponseObject(true,"Name is empty or count is equal to 0",null);
                }
                return service.addItem(family.getId().toString(), i.getListId().toString(), i.getQty(), i.getItemName());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            Utils.cancelProgressIndicator();
            if (response != null) {
                if (response.isError()) {
                    showAlertDialog(response.getErrorMessage());
                } else {
                    new ShoppinglistItemsTask().execute(getIntent().getStringExtra("id"));
                }
            }
        }
    }
}
