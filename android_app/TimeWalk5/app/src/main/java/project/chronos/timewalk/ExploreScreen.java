package project.chronos.timewalk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ExploreScreen extends AppCompatActivity {

    public static final String INTENT_LANDMARK_TAG = "INTENT_LANDMARK";

    private static final String TAG = "ExploreActivityTag";
    private Client client = HomeScreen.client;


    private class Postcard {
        Bitmap bits;
        String landmark;
    };

    private static ArrayList<Postcard> postcards;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        if (postcards == null)
            getPostcards();
        setupCards();
    }

    private void getPostcards()
    {
        Data<ArrayList<String>> result = client.listLandmarks();

        if (result.failed) {
            Toast.makeText(this, "Failed to retrieve landmarks", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to get landmarks: " + result.error_message);
        } else {
            postcards = new ArrayList<>();
            for (String landmark : result.result) {
                Log.e(TAG, "Landmark: " + landmark);

                Data<Bitmap> bitmap = client.getLandmarkImage(landmark);
                if (bitmap.failed) {
                    Log.e(TAG, "Failed to get bitmap: " + bitmap.error_message);
                }

                Postcard postcard = new Postcard();
                postcard.landmark = landmark;
                postcard.bits = bitmap.result;

                postcards.add(postcard);
            }
        }
    }

    private View.OnClickListener createOnClickListener(Postcard postcard)
    {
        class Listener implements View.OnClickListener {

            private String landmark;

            private Listener(String landmark) {
                this.landmark = landmark;
            }

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExploreScreen.this, LandmarkScreen.class);
                intent.putExtra(INTENT_LANDMARK_TAG, landmark);
                startActivity(intent);
            }
        }

        return new Listener(postcard.landmark);
    }

    private void setupCards()
    {
        LinearLayout layout = (LinearLayout) findViewById(R.id.explore_screen_lyt_postcards);

        for (Postcard postcard : postcards) {

            // setup txt

            TextView txt = new TextView(this);
            txt.setText(postcard.landmark.replace('_', ' ').concat(" "));
            txt.setGravity(Gravity.RIGHT);
            txt.setTextSize(18);
            txt.setTypeface(Typeface.MONOSPACE);
            txt.setTextColor(Color.WHITE);
            txt.setShadowLayer(15, 2, 2, Color.BLACK);

            // setup image

            ImageView img = new ImageView(this);
            img.setImageBitmap(postcard.bits);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setDrawingCacheEnabled(true);

            // setup card

            CardView card = new CardView(this);
            card.setCardElevation(10);
            card.setRadius(20);
            card.setUseCompatPadding(true);

            card.addView(img);
            card.addView(txt);
            card.setOnClickListener(createOnClickListener(postcard));
            layout.addView(card);
        }
    }
}
