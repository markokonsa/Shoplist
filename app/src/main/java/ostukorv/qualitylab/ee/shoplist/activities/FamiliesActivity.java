package ostukorv.qualitylab.ee.shoplist.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import ostukorv.qualitylab.ee.shoplist.adapters.FamilyAdapter;
import ostukorv.qualitylab.ee.shoplist.adapters.ShoplistsAdapter;
import ostukorv.qualitylab.ee.shoplist.entities.Family;
import ostukorv.qualitylab.ee.shoplist.entities.ResponseObject;
import ostukorv.qualitylab.ee.shoplist.entities.Session;
import ostukorv.qualitylab.ee.shoplist.entities.ShoppingList;
import ostukorv.qualitylab.ee.shoplist.entities.User;
import ostukorv.qualitylab.ee.shoplist.service.HttpService;
import ostukorv.qualitylab.ee.shoplist.service.ShoplistService;
import ostukorv.qualitylab.ee.shoplist.utility.Constants;
import ostukorv.qualitylab.ee.shoplist.utility.Utils;

public class FamiliesActivity extends AppCompatActivity {

    FamilyAdapter familyAdapter;
    ShoplistsAdapter adapter;
    ListView familiesList;
    ListView notificationList;
    public static Family family;
    TextView usernameTW;
    Button authBtn;
    List<Family> families = new ArrayList<>();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String username;
    String password;
    Date lastTry;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_families);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = getSharedPreferences(Utils.MyPREFERENCES, MODE_PRIVATE);

        username = prefs.getString("username", null);
        password = prefs.getString("password", null);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_family);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HttpService.currentUser.getId() == null) {
                    showLoginDialog("Please login first");
                } else if (families == null || families.isEmpty()) {
                    showAlertDialog("Add new family", "Enter family name", "Please enter family first");
                } else {
                    showAlertDialog("Add new list", "Enter list name", "");
                }
            }
        });

        familiesList = (ListView) findViewById(R.id.list_view_inside_nav);
        notificationList = (ListView) findViewById(R.id.shoppinglists_listview);
        familiesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                setActiveFamily(families.get(i));
            }
        });

        familiesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                familyAdapter.showAlertDialog(families.get(position));
                return true;
            }
        });

        notificationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FamiliesActivity.this, ItemActivity.class);
                intent.putExtra("id", family.getShoppingLists().get(position).getId().toString());
                intent.putExtra("name", family.getShoppingLists().get(position).getShoppingListName());
                FamiliesActivity.this.startActivity(intent);
            }
        });

        notificationList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.showAlertDialog(family.getShoppingLists().get(position));
                return true;
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        usernameTW = (TextView) findViewById(R.id.username_textview);
        authBtn = (Button) findViewById(R.id.auth_button);

        if (HttpService.currentUser.getId() != null) {
            User user = HttpService.currentUser;
            usernameTW.setText(user.getUserName());
            authBtn.setText("Logout");
        } else {
            usernameTW.setText("");
            authBtn.setText("Login");
        }
        if (username != null && password != null) {
            new AuthenticationTask().execute(Constants.loginUrl, username, password);
        }

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (family == null){
                    swipeContainer.setRefreshing(false);
                    return;
                }
                String familyId = family.getId().toString();
                new AuthenticationTask().execute(Constants.loginUrl, username, password);
                for (Family fam : families){
                    if (fam.getId().toString().equals(familyId)){
                        family = fam;
                        setActiveFamily(fam);
                        break;
                    }

                }
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        if (lastTry != null) {
            if (HttpService.session.getId() != null) {
                if (HttpService.session.getSessionEnd().before(new Date())) {
                    if (username != null && password != null) {
                        new AuthenticationTask().execute(Constants.loginUrl, username, password);
                    }
                } else {
                    new FamiliesTask().execute();
                }
            } else {
                showLoginDialog("Please log in");
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.families, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_members) {
            if (HttpService.currentUser.getId() == null) {
                showLoginDialog("Please login first");
            } else if (family.getId() == null) {
                showAlertDialog("Add new family", "Enter family name", "Please enter family first");
            } else {
                Intent intent = new Intent(this, MembersActivity.class);
                intent.putExtra("id", family.getId().toString());
                this.startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLoginDialog(String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(FamiliesActivity.this);

        alert.setTitle("Authenticate");
        alert.setMessage("Enter username or password and press login/register");

        LinearLayout ll = new LinearLayout(FamiliesActivity.this);
        ll.setPadding(10, 20, 10, 20);
        ll.setOrientation(LinearLayout.VERTICAL);

        // Set an EditText view to get user input
        final EditText username = new EditText(FamiliesActivity.this);
        username.setHint("Username");
        username.setSingleLine();
        final EditText password = new EditText(FamiliesActivity.this);
        password.setHint("Password");
        password.setSingleLine();
        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        final TextView errorText = new TextView(FamiliesActivity.this);
        if (!message.isEmpty()) {
            errorText.setText(message);
            errorText.setTextColor(Color.RED);
            errorText.setPadding(10, 0, 10, 0);
        }

        ll.addView(username);
        ll.addView(password);
        ll.addView(errorText);

        alert.setView(ll);

        alert.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String[] params = new String[3];
                params[0] = Constants.loginUrl;
                params[1] = username.getText().toString();
                params[2] = password.getText().toString();
                new AuthenticationTask().execute(params);
            }
        });

        alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Register", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String[] params = new String[3];
                params[0] = Constants.registerUrl;
                params[1] = username.getText().toString();
                params[2] = password.getText().toString();
                new AuthenticationTask().execute(params);
            }
        });

        alert.show();
    }

    private void showAlertDialog(final String title, String message, String error) {
        if (HttpService.currentUser.getId() != null) {

            AlertDialog.Builder alert = new AlertDialog.Builder(FamiliesActivity.this);

            alert.setTitle(title);
            alert.setMessage(message);

            LinearLayout ll = new LinearLayout(FamiliesActivity.this);
            ll.setOrientation(LinearLayout.VERTICAL);

            // Set an EditText view to get user input
            final EditText input = new EditText(FamiliesActivity.this);
            final TextView errorText = new TextView(FamiliesActivity.this);
            if (!message.isEmpty()) {
                errorText.setText(error);
                errorText.setTextColor(Color.RED);
                errorText.setPadding(10, 0, 10, 0);
            }

            ll.addView(input);
            ll.addView(errorText);

            alert.setView(ll);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (title.equals("Add new list")) {
                        new AddListTask().execute(input.getText().toString());
                    } else if (title.equals("Add new family")) {
                        new AddFamilyTask().execute(input.getText().toString());
                    }
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


    public void authenticateOnClick(View view) {
        Button b = (Button) view;
        String title = b.getText().toString();
        if (title.equals("Logout")) {
            HttpService.session = new Session();
            HttpService.currentUser = new User();
            editor.clear();
            familyAdapter.clear();
            adapter.clear();
            editor = prefs.edit();
            editor.clear().apply();
            setTitle("Shoplist");
            usernameTW.setText("");
            authBtn.setText("Login");

        } else {
            showLoginDialog("");
        }
    }

    public void setActiveFamily(Family fam) {
        family = fam;
        setTitle(family.getFamilyName());
        if (family.getShoppingLists() == null) {
            family.setShoppingLists(new ArrayList<ShoppingList>());
        }
        adapter = new ShoplistsAdapter(FamiliesActivity.this, family.getShoppingLists());
        notificationList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void addNewFamily(View view) {
        if (HttpService.currentUser.getId() == null) {
            showLoginDialog("Please login first");
        } else {
            showAlertDialog("Add new family", "Enter family name", "");
        }
    }

    public class AuthenticationTask extends AsyncTask<String, Void, ResponseObject> {

        ShoplistService service = new ShoplistService();

        @Override
        protected void onPreExecute() {
            Utils.showProgressIndicator(FamiliesActivity.this, "Please wait...");
        }

        @Override
        protected ResponseObject doInBackground(String... params) {
            String url = params[0];
            username = params[1];
            password = params[2];
            try {
                if (username.isEmpty() && password.isEmpty()){
                    return new ResponseObject(true,"Username or password is emtpy",null);
                }
                return service.auth(url, username, password);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            if (response.isError()) {
                Utils.cancelProgressIndicator();
                showLoginDialog(response.getErrorMessage());
            } else {
                User user = HttpService.currentUser;
                usernameTW.setText(user.getUserName());
                authBtn.setText("Logout");
                editor = prefs.edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.apply();
                new FamiliesTask().execute();
            }
        }
    }

    public class FamiliesTask extends AsyncTask<Void, Void, ResponseObject> {

        ShoplistService service;

        @Override
        protected void onPreExecute() {
            service = new ShoplistService();
        }

        @Override
        protected ResponseObject doInBackground(Void... voids) {

            try {
                return service.getShoppinglists();
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
                    Toast.makeText(FamiliesActivity.this, response.getErrorMessage(), Toast.LENGTH_LONG).show();
                } else {
                    families = (List<Family>) response.getObjects();
                    familyAdapter = new FamilyAdapter(FamiliesActivity.this, families);
                    if (family == null) {
                        family = families.get(0);
                    }
                    setTitle(family.getFamilyName());
                    List<ShoppingList> shoppingLists = null;
                    for (Family fy : families) {
                        if (fy.getId().toString().equals(family.getId().toString())) {
                            shoppingLists = family.getShoppingLists();
                            break;
                        }
                    }
                    if (shoppingLists == null) {
                        shoppingLists = new ArrayList<>();
                    }
                    adapter = new ShoplistsAdapter(FamiliesActivity.this, shoppingLists);
                    familiesList.setAdapter(familyAdapter);
                    notificationList.setAdapter(adapter);
                }
            } else {
                showAlertDialog("Add new family", "Enter family name", "Please enter family first");
            }
            Utils.cancelProgressIndicator();
        }
    }

    public class AddFamilyTask extends AsyncTask<String, Void, ResponseObject> {

        ShoplistService service;

        @Override
        protected void onPreExecute() {
            Utils.showProgressIndicator(FamiliesActivity.this, "Please wait...");
            service = new ShoplistService();
        }

        @Override
        protected ResponseObject doInBackground(String... params) {

            try {
                String name = params[0];
                if (name.isEmpty()){
                    return new ResponseObject(true,"Name cannot be empty",null);
                }
                return service.addFamily(name);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            if (response.isError()) {
                showAlertDialog("Add new family", "Enter family name", response.getErrorMessage());
            } else {
                new FamiliesTask().execute();
            }
        }
    }

    public class AddListTask extends AsyncTask<String, Void, ResponseObject> {

        ShoplistService service;

        @Override
        protected void onPreExecute() {
            Utils.showProgressIndicator(FamiliesActivity.this, "Please wait...");
            service = new ShoplistService();
        }

        @Override
        protected ResponseObject doInBackground(String... params) {

            try {
                String name = params[0];
                if (name.isEmpty()){
                    return new ResponseObject(true,"Name cannot be empty",null);
                }
                return service.addShoppinglist(family.getId().toString(), name);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            if (response.isError()) {
                showAlertDialog("Add new list", "Enter list name", response.getErrorMessage());
            } else {
                ShoppingList list = (ShoppingList) response.getObjects().get(0);
                Family current = new Family();
                for (Family f : families) {
                    if (family.getId().equals(f.getId())) {
                        if (f.getShoppingLists() == null){
                            f.setShoppingLists(new ArrayList<ShoppingList>());
                        }
                        f.getShoppingLists().add(list);
                        current = f;
                    }
                }
                adapter.setList(current.getShoppingLists());
                adapter.notifyDataSetChanged();
            }
            Utils.cancelProgressIndicator();
        }
    }
}
