package com.example.amit.minesweeper;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LeaderBoardActivity extends AppCompatActivity {

    private static final String API_KEY = "AIzaSyCVyXqNtRzG5HYTbIJxUn0d_FoD-SGHNnM";
    private static final String TAG = "amit";

    private GoogleMap googleMap;
    private LeaderBoard leaderBoard;
    private RadioGroup difficultyRadioGroup;
    private TextView[][] leadersTextView = new TextView[3][5];
    private int[] numOfLeaderPerDifficulty = new int[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);

        difficultyRadioGroup = (RadioGroup) findViewById(R.id.difficulty_radio_group);
        difficultyRadioGroup.check(difficultyRadioGroup.getChildAt(0).getId());

        difficultyRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                showLeaders();
            }
        });

        if (isGoogleMapsInstalled()) {
            // Add the Google Maps fragment dynamically
            final FragmentTransaction transaction = getFragmentManager().beginTransaction();
            MapFragment mapFragment = MapFragment.newInstance();
            transaction.add(R.id.mapsPlaceHolder, mapFragment);
            transaction.commit();

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    setGoogleMap(googleMap);
                }
            });
        } else {

            // Notify the user he should install GoogleMaps (after installing Google Play Services)
            FrameLayout mapsPlaceHolder = (FrameLayout) findViewById(R.id.mapsPlaceHolder);
            TextView errorMessageTextView = new TextView(getApplicationContext());
            errorMessageTextView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            errorMessageTextView.setText(R.string.maps_notification_error);
            errorMessageTextView.setTextColor(Color.RED);
            mapsPlaceHolder.addView(errorMessageTextView);
        }

        loadLeadersFromFireBase();


    }


    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // Unmark to see the changes...

        boolean isAllowedToUseLocation = hasPermissionForLocationServices(getApplicationContext());
        if (isAllowedToUseLocation) {
            try {
                // Allow to (try to) set
                googleMap.setMyLocationEnabled(true);
            } catch (SecurityException exception) {
                Toast.makeText(this, "Error getting location" , Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Location is blocked in this app", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean hasPermissionForLocationServices(Context context) {
        // Because the user's permissions started only from Android M and on...
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    public void loadLeadersFromFireBase() {
        leaderBoard = LeaderBoard.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        final String[] difficulties = {"easy" , "medium", "hard"};
        for (int i = 0; i < difficulties.length; i++) {
            DatabaseReference myRef = database.getReference(difficulties[i]);
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int difficultyIndex = getDifficultyIndex(difficulties, dataSnapshot);
                    if(difficultyIndex != -1) {
                        for (int rankIndex = 0; rankIndex < dataSnapshot.getChildrenCount(); rankIndex++) {
                            DataSnapshot rank = dataSnapshot.child((rankIndex + 1) + "");

                            //GenericTypeIndicator<Map<String,String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>(){};
                            //Map<String, String> leader = rank.getValue(genericTypeIndicator); not working
                            Map<String, String> leader = (Map<String, String>) rank.getValue();
                            leadersTextView[difficultyIndex][rankIndex] = new TextView(getApplicationContext());
                            //rank
                            leadersTextView[difficultyIndex][rankIndex].append("\t\t\t\t " + rank.getKey() + "\t\t\t\t ");
                            Iterator<Map.Entry<String,String>> leaderDetails = leader.entrySet().iterator();

                            //name
                            String name = leaderDetails.next().getValue();
                            leadersTextView[difficultyIndex][rankIndex].append("\t\t\t\t\t\t " + name + "\t\t\t\t ");

                            //score
                            String score = leaderDetails.next().getValue();
                            leadersTextView[difficultyIndex][rankIndex].append("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t " + score + "\t\t\t\t");

                            //time
                            String time = leaderDetails.next().getValue();
                            leadersTextView[difficultyIndex][rankIndex].append("\t\t\t\t\t\t\t\t" + time + "\t\t\t\t ");

                            //location
                            double latitude = Double.parseDouble(leaderDetails.next().getValue());
                            double longitude = Double.parseDouble(leaderDetails.next().getValue());


                            googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)));

                            Location location = new Location("");
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);

                            leaderBoard.addPlayer(MainActivity.eDifficulty.values()[difficultyIndex], new PlayerScore(name,Integer.parseInt(score),location, Integer.parseInt(time)), false);

                            numOfLeaderPerDifficulty[difficultyIndex]++;
                        }

                        showLeaders();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private int getDifficultyIndex(String[] difficulties, DataSnapshot dataSnapshot) {
        for (int i = 0; i < difficulties.length; i++) {
            if (difficulties[i].equals(dataSnapshot.getKey()))
                return i;
        }
        return -1;
    }


    public void showLeaders() {
        int difficultyIndex = difficultyRadioGroup.getCheckedRadioButtonId() - 1;
        TableLayout tableLayout = (TableLayout) findViewById(R.id.leader_table);
        ScrollView scrollView = (ScrollView) findViewById(R.id.leader_board_scroll_scores);
        scrollView.removeView(tableLayout);
        tableLayout.removeAllViews();
        scrollView.addView(tableLayout);

        for (int i = 0; i < numOfLeaderPerDifficulty[difficultyIndex]; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT));
            leadersTextView[difficultyIndex][i].setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            if(leadersTextView[difficultyIndex][i].getParent() != null)
                ((TableRow)leadersTextView[difficultyIndex][i].getParent()).removeAllViews();
            tableRow.addView(leadersTextView[difficultyIndex][i]);
            tableLayout.addView(tableRow);
        }
    }
}
