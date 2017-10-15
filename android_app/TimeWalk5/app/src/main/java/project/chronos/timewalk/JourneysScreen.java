package project.chronos.timewalk;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class JourneysScreen extends AppCompatActivity {

    private static final String TAG = "JourneysScreenTag";
    private boolean atRoot = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journeys_screen);

        setupUpAddNewJourney();
        displayJourneys();
    }

    @Override
    public void onBackPressed() {
        if (atRoot)
            super.onBackPressed();
        else
            displayJourneys();
    }

    private LinearLayout getLayout() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.journey_screen_lyt_playlists);
        layout.removeAllViews();
        return layout;
    }

    private CardView createCard(View.OnClickListener listener) {
        CardView card = new CardView(this);
        card.setRadius(20);
        card.setUseCompatPadding(true);
        card.setOnClickListener(listener);
        return card;
    }

    private void displayLandmarksOfJourney(String journey) {
        LinearLayout layout = getLayout();
        atRoot = false;

        final ArrayList<String> landmarks = JourneyHelper.getLandmarks(journey, this);

        for (final String landmark : landmarks) {
            class Listener implements View.OnClickListener {
                String landmark;

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(JourneysScreen.this, LandmarkScreen.class);
                    intent.putExtra(DiscoverScreen.INTENT_LANDMARK_TAG, landmark);
                    startActivity(intent);
                }
            }

            Listener listener = new Listener();
            listener.landmark = landmark;
            CardView card = createCard(listener);

            TextView txt = new TextView(this);
            txt.setText(landmark.replace('_', ' '));
            txt.setTypeface(Typeface.SERIF);
            txt.setTextSize(20);
            txt.setPadding(20, 20, 20, 20);
            txt.setGravity(Gravity.CENTER);
            txt.setBackground(getResources().getDrawable(R.color.commonWhite));

            ImageButton btn = new ImageButton(this);
            btn.setImageResource(R.drawable.ic_delete);
            btn.setBackground(getResources().getDrawable(R.color.commonWhite));

            class DeleteListener implements View.OnClickListener {

                String toRemove;
                String journey;
                ArrayList<String> landmarks;

                @Override
                public void onClick(View view) {
                    landmarks.remove(toRemove);
                    JourneyHelper.setLandmarks(journey, landmarks, JourneysScreen.this);
                    displayJourneys();
                }
            }

            DeleteListener deleteListener = new DeleteListener();
            deleteListener.toRemove = journey;
            deleteListener.journey = journey;
            deleteListener.landmarks = landmarks;
            btn.setOnClickListener(deleteListener);

            LinearLayout singleLayout = new LinearLayout(this);
            singleLayout.setWeightSum(100);
            LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 90);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 10);

            singleLayout.addView(txt, txtParams);
            singleLayout.addView(btn, btnParams);

            card.addView(singleLayout);
            layout.addView(card);
        }
    }

    private void displayJourneys() {
        LinearLayout layout = getLayout();
        atRoot = true;

        final ArrayList<String> journeys = JourneyHelper.getJourneys(this);

        for (final String journey : journeys) {
            class Listener implements View.OnClickListener {
                String journey;

                @Override
                public void onClick(View view) {
                    displayLandmarksOfJourney(journey);
                }
            }

            Listener listener = new Listener();
            listener.journey = journey;
            CardView card = createCard(listener);

            TextView txt = new TextView(this);
            txt.setText(journey);
            txt.setTypeface(Typeface.SERIF);
            txt.setTextSize(20);
            txt.setPadding(20, 20, 20, 20);
            txt.setGravity(Gravity.CENTER);
            txt.setBackground(getResources().getDrawable(R.color.commonWhite));


            ImageButton btn = new ImageButton(this);
            btn.setImageResource(R.drawable.ic_delete);
            btn.setBackground(getResources().getDrawable(R.color.commonWhite));

            class DeleteListener implements View.OnClickListener {

                String toRemove;
                ArrayList<String> journeys;

                @Override
                public void onClick(View view) {
                    journeys.remove(toRemove);
                    JourneyHelper.setJourneys(journeys, JourneysScreen.this);
                    displayJourneys();
                }
            }

            DeleteListener deleteListener = new DeleteListener();
            deleteListener.toRemove = journey;
            deleteListener.journeys = journeys;
            btn.setOnClickListener(deleteListener);

            LinearLayout singleLayout = new LinearLayout(this);
            singleLayout.setWeightSum(100);
            LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 90);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 10);

            singleLayout.addView(txt, txtParams);
            singleLayout.addView(btn, btnParams);

            card.addView(singleLayout);
            layout.addView(card);
        }
    }

    private void setupUpAddNewJourney() {
        Button btn = (Button) findViewById(R.id.journey_screen_btn_create);
        ImageButton img = (ImageButton) findViewById(R.id.journey_screen_imbtn_create);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JourneyHelper.createNewJourney(JourneysScreen.this,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                displayJourneys();
                            }
                        });
            }
        };

        btn.setOnClickListener(listener);
        img.setOnClickListener(listener);
    }
}