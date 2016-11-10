package com.example.erick.awesomenotes;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerViewerAdapter recyclerViewAdapter;
    private EditText addTaskBox;
    private DatabaseReference databaseReference;
    private List<Note> allNotes;
    private GoogleSignInAccount account;
    private SwipeRefreshLayout swipeRefreshLayout;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        account = (GoogleSignInAccount) getIntent().getExtras().get("account");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                interstitialAd.show();
            }
        });

        allNotes = new ArrayList<Note>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        addTaskBox = (EditText)findViewById(R.id.add_task_box);
        recyclerView = (RecyclerView)findViewById(R.id.task_list);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        Button addTaskButton = (Button)findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredTask = addTaskBox.getText().toString();
                if(TextUtils.isEmpty(enteredTask)){
                    Toast.makeText(MainActivity.this, "You must enter a note first", Toast.LENGTH_LONG).show();
                    return;
                }
                Note note = new Note(enteredTask);
                databaseReference.push().setValue(note);
                addTaskBox.setText("");
            }
        });
        final ChildEventListener event = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                getAllTask(dataSnapshot);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //getAllTask(dataSnapshot);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                taskDeletion(dataSnapshot);
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        databaseReference.addChildEventListener(event);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                databaseReference.removeEventListener(event);
                allNotes.clear();
                recyclerViewAdapter = new RecyclerViewerAdapter(MainActivity.this, allNotes);
                recyclerView.setAdapter(recyclerViewAdapter);
                databaseReference.addChildEventListener(event);
            }
        });


        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequestInter = new AdRequest.Builder().build();
        mAdView.loadAd(adRequestInter);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navView = navigationView.getHeaderView(0);

        ImageView drawerImage = (ImageView) navView.findViewById(R.id.drawer_image);
        TextView drawerTitle = (TextView) navView.findViewById(R.id.drawer_title);
        TextView drawerEmail = (TextView) navView.findViewById(R.id.drawer_email);

        Picasso.with(this).load(account.getPhotoUrl()).transform(new CropCircleTransformation()).into(drawerImage);
        drawerEmail.setText(account.getEmail());
        drawerTitle.setText(account.getDisplayName());
    }


    private void getAllTask(DataSnapshot dataSnapshot){
        //allNotes = new ArrayList<Note>();
        for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
            String taskTitle = singleSnapshot.getValue(String.class);
            allNotes.add(new Note(taskTitle));
            recyclerViewAdapter = new RecyclerViewerAdapter(MainActivity.this, allNotes);
            recyclerView.setAdapter(recyclerViewAdapter);
        }

        TextView empty_message = (TextView) findViewById(R.id.empty_message);
        if (allNotes.isEmpty())
            empty_message.setVisibility(View.VISIBLE);
        else
            empty_message.setVisibility(View.GONE);

        swipeRefreshLayout.setRefreshing(false);
    }

    private void taskDeletion(DataSnapshot dataSnapshot){
        for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
            String taskTitle = singleSnapshot.getValue(String.class);
            for(int i = 0; i < allNotes.size(); i++){
                if(allNotes.get(i).getNoteContent().equals(taskTitle)){
                    allNotes.remove(i);
                }
            }
            Log.d(TAG, "Task tile " + taskTitle);
            recyclerViewAdapter.notifyDataSetChanged();
            recyclerViewAdapter = new RecyclerViewerAdapter(MainActivity.this, allNotes);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
        TextView empty_message = (TextView) findViewById(R.id.empty_message);
        if (allNotes.isEmpty())
            empty_message.setVisibility(View.VISIBLE);
        else
            empty_message.setVisibility(View.GONE);
    }



    @Override
    public void onBackPressed() {/*        else {
            super.onBackPressed();
        }*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.ic_logout) {
            Intent intent = new Intent();
            setResult(201, intent);
            finish();
        }
            // Handle the camera action
/*        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
