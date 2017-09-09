package project.chronos.timewalk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    public static final String LANDMARK_TAG = "LANDMARK_TAG";

    private static final String LOG = "TIME_WALK_SEARCH";
    private static ArrayList<String> landmarks;

    private Client client = HomeScreenActivity.client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (landmarks == null)
            getLandmarks();

        // if getLandmarks didn't fail
        if (landmarks != null)
            setupLandmarkPostcards();
    }

    private void getLandmarks() {

        Log.d(LOG, "Getting Landmarks from Server");

        Data<ArrayList<String>> data = client.listLandmarks();
        if (data.failed)
            HomeScreenActivity.projectTimeWalkDown(getApplicationContext());
        else
            landmarks = data.result;
    }

    private void setupLandmarkPostcards() {

        Log.d(LOG, "Getting Postcards of all Landmarks");

        for (String landmark : landmarks) {
            Data<Bitmap> data = client.getLandmarkPostcardImage(landmark);

            if (!data.failed) {

                Button button = new Button(getApplicationContext());
                button.setGravity(Gravity.TOP);
                button.setText(landmark.replace('_', ' '));

                Drawable drawable = new BitmapDrawable(getResources(), data.result);
                button.setBackground(drawable);
                button.setDrawingCacheEnabled(true);
                button.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

                button.setOnClickListener(new LandmarkListener(landmark));

                LinearLayout layout = (LinearLayout) findViewById(R.id.search_screen_postcards_layout);
                layout.addView(button);

            } else {
                Log.d(LOG, "Failed to get Postcard for - " + landmark);
            }
        }
    }

    private class LandmarkListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(SearchActivity.this, LandmarkActivity.class);
            intent.putExtra(LANDMARK_TAG, landmark);
            startActivity(intent);
        }

        public String landmark;

        LandmarkListener(String landmark) {
            this.landmark = landmark;
        }
    }
}
