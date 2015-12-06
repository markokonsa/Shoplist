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
import ostukorv.qualitylab.ee.shoplist.adapters.MembersAdapter;
import ostukorv.qualitylab.ee.shoplist.entities.ResponseObject;
import ostukorv.qualitylab.ee.shoplist.entities.User;
import ostukorv.qualitylab.ee.shoplist.service.ShoplistService;
import ostukorv.qualitylab.ee.shoplist.utility.Utils;

/**
 * Created by Marko on 2.12.2015.
 */
public class MembersActivity  extends AppCompatActivity {

    List<User> users = new ArrayList<>();
    MembersAdapter adapter;
    ListView notificationList;
    Date lastTry;
    private SwipeRefreshLayout swipeContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Members");
        Intent currentIntent = getIntent(); // gets the previously created intent
        String familyId = currentIntent.getStringExtra("id");
        new MembersTask().execute(familyId);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_member);
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
                new MembersTask().execute(getIntent().getStringExtra("id"));
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (lastTry != null) {
            new MembersTask().execute(getIntent().getStringExtra("id"));
        }
    }

    private void showAlertDialog(String message){
        AlertDialog.Builder alert = new AlertDialog.Builder(MembersActivity.this);

        alert.setTitle("Add new member");
        alert.setMessage("Enter username to add");

        LinearLayout ll=new LinearLayout(MembersActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);

        // Set an EditText view to get user input
        final EditText input = new EditText(MembersActivity.this);
        final TextView errorText = new TextView(MembersActivity.this);
        if (!message.isEmpty()){
            errorText.setText(message);
            errorText.setTextColor(Color.RED);
            errorText.setPadding(10,0,10,0);
        }

        ll.addView(input);
        ll.addView(errorText);

        alert.setView(ll);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                new AddMemberTask().execute(input.getText().toString());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    public class MembersTask extends AsyncTask<String, Void, ResponseObject> {

        ShoplistService service;

        @Override
        protected void onPreExecute() {
            Utils.showProgressIndicator(MembersActivity.this, "Please wait...");
            service = new ShoplistService();
        }

        @Override
        protected ResponseObject doInBackground(String... strings) {

            try {
                return service.getMembers(strings[0]);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            swipeContainer.setRefreshing(false);
            lastTry = new Date();
            if (response.isError()){
                Toast.makeText(MembersActivity.this,response.getErrorMessage(),Toast.LENGTH_LONG).show();
            }else {
                users = (List<User>) response.getObjects();
                adapter = new MembersAdapter(MembersActivity.this, users);
                notificationList = (ListView) findViewById(R.id.members_listview);
                notificationList.setAdapter(adapter);
            }
            Utils.cancelProgressIndicator();
        }
    }

    public class AddMemberTask extends AsyncTask<String, Void, ResponseObject> {

        ShoplistService service;

        @Override
        protected void onPreExecute() {
            Utils.showProgressIndicator(MembersActivity.this, "Please wait...");
            service = new ShoplistService();
        }

        @Override
        protected ResponseObject doInBackground(String... strings) {

            try {
                if (strings[0].isEmpty()){
                    return new ResponseObject(true,"Name cannot be empty",null);
                }
                return service.addMember(FamiliesActivity.family.getId().toString(), strings[0]);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseObject response) {
            Utils.cancelProgressIndicator();
            if (response.isError()){
                showAlertDialog(response.getErrorMessage());
            }else {
                new MembersTask().execute(getIntent().getStringExtra("id"));
            }
        }
    }


}
