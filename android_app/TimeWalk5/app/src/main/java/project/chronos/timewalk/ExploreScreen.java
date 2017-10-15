package project.chronos.timewalk;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class ExploreScreen extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "EXPLORE_SCREEN_TAG";

    private Client client = HomeScreen.client;
    private GoogleMap mMap;
    private LatLng regionMarker;

    private abstract class Run {
        abstract void run();
    }

    Run back;

    @Override
    public void onBackPressed() {
        if (back == null)
            super.onBackPressed();
        else
            back.run();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_screen);

        // Add a marker in Brisbane and move the camera
        Data<GPSCoord> brisbaneGPS = client.getRegionGPS();
        if (brisbaneGPS.failed || brisbaneGPS.result == null) {
            Log.e(TAG, "Brisbane GPS failed");
            // TODO: Toast error
        }

        regionMarker = new LatLng(brisbaneGPS.result.latitude, brisbaneGPS.result.longitude);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loadRoot();
    }

    private LinearLayout getLayout() {
        LinearLayout layout = findViewById(R.id.explore_screen_lyt_scroll);
        layout.removeAllViews();
        return layout;
    }

    private CardView createCard(View.OnClickListener listener) {
        CardView card = new CardView(this);
        card.setCardElevation(10);
        card.setRadius(20);
        card.setUseCompatPadding(true);
        card.setOnClickListener(listener);
        card.setCardBackgroundColor(getResources().getColor(R.color.commonWhite));
        return card;
    }

    private TextView createText(String txt) {
        TextView tv = new TextView(this);

        tv.setText(txt);
        tv.setGravity(Gravity.CENTER);
        tv.setAllCaps(false);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        tv.setTypeface(Typeface.SERIF);
        tv.setTextSize(24);
        tv.setPadding(20, 20, 20, 20);

        return tv;
    }

    private void loadRoot() {
        LinearLayout layout = getLayout();

        CardView popularCV = createCard(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPopular();
            }
        });

        CardView themesCV = createCard(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadThemes();
            }
        });
        CardView journeysCV = createCard(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadJourneys();
            }
        });

        TextView popularTV = createText("Popular");
        TextView themesTV = createText("Themes");
        TextView journeysTV = createText("My Journeys");

        popularCV.addView(popularTV);
        themesCV.addView(themesTV);
        journeysCV.addView(journeysTV);

        layout.addView(popularCV);
        layout.addView(themesCV);
        layout.addView(journeysCV);

        back = null;
    }

    private void setBackToRoot() {
        back = new Run() {
            @Override
            void run() {
                mMap.clear();
                loadRoot();
            }
        };
    }

    private void loadLandmarks(ArrayList<String> landmarks) {

        CameraPosition cameraPosition = new CameraPosition.Builder().
                target(regionMarker).
                tilt(0).
                zoom(14).
                bearing(0).
                build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        LinearLayout layout = getLayout();
        for (String landmark : landmarks) {

            Data<GPSCoord> gpsData = client.getLandmarkGPS(landmark);
            CardView card = createCard(null);

            TextView txt = createText(landmark.replace('_', ' '));
            txt.setText(landmark.replace('_', ' '));
            txt.setTypeface(Typeface.SERIF);
            txt.setTextSize(20);
            txt.setPadding(20, 20, 20, 20);
            txt.setGravity(Gravity.CENTER);
            txt.setBackground(getResources().getDrawable(R.color.commonWhite));

            ImageButton btn = new ImageButton(this);
            btn.setImageResource(R.drawable.ic_info);
            btn.setBackground(getResources().getDrawable(R.color.commonWhite));

            class BtnListener implements View.OnClickListener {

                String landmark;

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ExploreScreen.this, LandmarkScreen.class);
                    intent.putExtra(DiscoverScreen.INTENT_LANDMARK_TAG, landmark);
                    startActivity(intent);
                }
            }

            BtnListener btnListener = new BtnListener();
            btnListener.landmark = landmark;
            btn.setOnClickListener(btnListener);

            ImageButton btn2 = new ImageButton(this);
            btn2.setImageResource(R.drawable.ic_add);
            btn2.setBackground(getResources().getDrawable(R.color.commonWhite));

            class BtnListener2 implements View.OnClickListener {

                String landmark;

                @Override
                public void onClick(View view) {
                    JourneyHelper.addLandmarkToJourney(landmark, ExploreScreen.this);
                }
            }

            BtnListener2 btnListener2 = new BtnListener2();
            btnListener2.landmark = landmark;
            btn2.setOnClickListener(btnListener2);

            LinearLayout singleLayout = new LinearLayout(this);
            singleLayout.setWeightSum(100);
            LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 80);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 10);

            singleLayout.addView(txt, txtParams);
            singleLayout.addView(btn, btnParams);
            singleLayout.addView(btn2, btnParams);

            card.addView(singleLayout);
            layout.addView(card);

            if (gpsData.failed || gpsData.result == null) {
                Log.e(TAG, "Failed to get GPS: " + landmark);
            } else {
                LatLng marker = new LatLng(gpsData.result.latitude, gpsData.result.longitude);
                mMap.addMarker(new MarkerOptions().position(marker).title(landmark.replace('_', ' ')));

                class Listener implements View.OnClickListener {

                    LatLng marker;

                    @Override
                    public void onClick(View view) {
                        CameraPosition cameraPosition = new CameraPosition.Builder().
                                target(marker).
                                tilt(60).
                                zoom(17).
                                bearing(0).
                                build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        mMap.setBuildingsEnabled(true);
                    }
                }

                Listener listener = new Listener();
                listener.marker = marker;
                card.setOnClickListener(listener);
            }
        }
    }

    private void loadPopular() {
        Data<ArrayList<String>> popularData = client.getPopular();

        if (popularData.failed || popularData.result == null) {
            Log.e(TAG, "popular Data failed");
            // TODO: Toast msg
        } else {
            loadLandmarks(popularData.result);
            setBackToRoot();
        }
    }

    private void loadThemes() {
        Data<ArrayList<String>> themesData = client.listThemes();

        if (themesData.failed || themesData.result == null) {
            Log.e(TAG, "Failed to get Themes");
            // TODO: toast error
            return;
        }

        LinearLayout layout = getLayout();
        for (String theme : themesData.result) {
            CardView card = createCard(null);
            TextView txt = createText(theme.replace('_', ' '));
            card.addView(txt);
            layout.addView(card);

            class Listener implements View.OnClickListener {

                String theme;

                @Override
                public void onClick(View view) {
                    loadThemeList(theme);
                }
            }

            Listener listener = new Listener();
            listener.theme = theme;
            card.setOnClickListener(listener);

            setBackToRoot();
        }
    }

    private void loadThemeList(String theme) {
        Data<ArrayList<String>> landmarksData = client.getTheme(theme);

        if (landmarksData.failed || landmarksData.result == null) {
            Log.e(TAG, "Failed to get theme: " + theme);
            // TODO: toast error
        } else {
            loadLandmarks(landmarksData.result);

            back = new Run() {
                @Override
                void run() {
                    loadThemes();
                    mMap.clear();
                }
            };
        }
    }

    private void loadJourneyList(ArrayList<String> landmarks) {
        LinearLayout layout = getLayout();
        back = new Run() {
            @Override
            void run() {
                loadJourneys();
            }
        };

        loadLandmarks(landmarks);
    }

    private void loadJourneys() {
        LinearLayout layout = getLayout();
        setBackToRoot();

        ArrayList<String> journeys = JourneyHelper.getJourneys(this);

        for (String journey : journeys) {

            class Listener implements View.OnClickListener {

                String journey;

                @Override
                public void onClick(View view) {
                    loadJourneyList(JourneyHelper.getLandmarks(journey, ExploreScreen.this));
                }
            }
            Listener listener = new Listener();
            listener.journey = journey;
            CardView card = createCard(listener);
            TextView txt = createText(journey);
            card.addView(txt);
            layout.addView(card);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(regionMarker, 14));
    }
}
