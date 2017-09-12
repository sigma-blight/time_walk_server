package project.chronos.timewalk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class SearchScreen extends AppCompatActivity {

    public static final String INTENT_TAG_LANDMARK = "INTENT_LANDMARK_TAG";

    private class Landmark {
        String name;
        Bitmap bitmap;
    }

    private Client client = HomeScreen.client;
    private static ArrayList<Landmark> cachedLandmarks = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        if (cachedLandmarks == null) {
            Data<ArrayList<String>> landmarkData = client.listLandmarks();


            if (landmarkData.failed)
                Utility.toastResultCodeError(landmarkData.resultCode, getApplicationContext());
            else {
                cachedLandmarks = new ArrayList<>();
                for (String landmarkName : landmarkData.result) {
                    Landmark landmark = new Landmark();
                    landmark.name = landmarkName;
                    cachedLandmarks.add(landmark);
                }

                getPostcards();
            }
        }

        setupCards();
    }

    private void getPostcards() {
        for (Landmark landmark : cachedLandmarks) {
            Data<Bitmap> bitmapData = client.getLandmarkPostcard(landmark.name);

            if (bitmapData.failed) {
                // add default "NOTHING FOUND" image
            } else
                landmark.bitmap = bitmapData.result;
        }
    }

    private ImageView createImage(Bitmap bits) {
        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setImageBitmap(bits);
        img.setDrawingCacheEnabled(true);
        return img;
    }

    private TextView createText(String landmark) {
        TextView name = new TextView(this);

        name.setText(Utility.displayName(landmark));
        name.setGravity(Gravity.RIGHT);
        name.setTextSize(18);
        name.setTypeface(null, Typeface.BOLD);
        name.setTextColor(Color.WHITE);
        name.setShadowLayer(15, 2, 2, Color.BLACK);
        name.setTypeface(Typeface.MONOSPACE);

        return name;
    }

    private CardView createCard(String landmark) {
        CardView card = new CardView(this);

        card.setUseCompatPadding(true);
        card.setRadius(20);
        card.setCardElevation(10);

        class Listener implements View.OnClickListener {

            private String landmark;

            private Listener(String landmark) {
                this.landmark = landmark;
            }

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchScreen.this, LandmarkScreen.class);
                intent.putExtra(INTENT_TAG_LANDMARK, landmark);
                startActivity(intent);
            }
        }

        card.setOnClickListener(new Listener(landmark));
        return card;
    }

    private void setupCards() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.search_screen_lyt_postcards);

        for (Landmark landmark : cachedLandmarks) {

            CardView card = createCard(landmark.name);
            card.addView(createImage(landmark.bitmap));
            card.addView(createText(landmark.name));

            layout.addView(card);
        }
    }
}
