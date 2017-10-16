package project.chronos.timewalk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LandmarkScreen extends AppCompatActivity {

    private static final String TAG = "LandmarkScreenTag";

    public static Bitmap current_selected_bitmap;

    private class Image {
        String text;
        Bitmap bitmap;
        String name;
    }

    private Client client = HomeScreen.client;
    private String landmark;
    private ArrayList<Image> images;
    private int currentImage = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_screen);

        landmark = getIntent().getStringExtra(DiscoverScreen.INTENT_LANDMARK_TAG);

        Data<ArrayList<String>> imageData = client.listImages(landmark);

        if (imageData.failed)
            Toast.makeText(LandmarkScreen.this,
                    "Failed to download any images",
                    Toast.LENGTH_LONG).show();
        else
        {
            images = new ArrayList<>();
            for (String image_name : imageData.result)
            {
                Image image = new Image();
                image.name = image_name;
                images.add(image);
            }

            loadImages();
            display(0);
            setupAddButton();
        }
    }

    private void setupAddButton() {
        ImageButton addBtn = (ImageButton) findViewById(R.id.landmark_screen_btn_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JourneyHelper.addLandmarkToJourney(landmark, LandmarkScreen.this);
            }
        });
    }

    private void loadImages() {
        for (Image image : images) {
            Data<Bitmap> bitmapData = client.getImage(landmark, image.name);
            Data<String> txtData = client.getImageText(landmark, image.name);

            if (bitmapData.failed && txtData.failed)
                images.remove(image);

            if (!bitmapData.failed)
                image.bitmap = bitmapData.result;
            if (!txtData.failed)
                image.text = txtData.result;
        }
    }

    private void display(int index)
    {
        currentImage = index;
        Image currImage = images.get(currentImage);

        ImageView image = new ImageView(this);
        image.setImageBitmap(currImage.bitmap);
        image.setDrawingCacheEnabled(true);
        current_selected_bitmap = currImage.bitmap;

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LandmarkScreen.this, SingleImageScreen.class));
            }
        });

        CardView card = (CardView) findViewById(R.id.landmark_screen_cv_image);
        card.removeAllViews();
        card.addView(image);

        Button lhs = (Button) findViewById(R.id.landmark_screen_tv_date_left);
        Button rhs = (Button) findViewById(R.id.landmark_screen_tv_date_right);
        Button main = (Button) findViewById(R.id.landmark_screen_tv_date_main);

        if (currentImage == 0) {
            lhs.setText("");
            lhs.setOnClickListener(null);
        } else {
            lhs.setText(images.get(currentImage - 1).name.split("_")[0]);
            class Listener implements View.OnClickListener {

                @Override
                public void onClick(View view) {
                    display(index - 1);
                }
                Integer index;
            }
            Listener listener = new Listener();
            listener.index = currentImage;
            lhs.setOnClickListener(listener);
        }
        if (currentImage == images.size() - 1) {
            rhs.setText("");
            rhs.setOnClickListener(null);
        }
        else {
            rhs.setText(images.get(currentImage + 1).name.split("_")[0]);
            class Listener implements View.OnClickListener {

                @Override
                public void onClick(View view) {
                    display(index + 1);
                }
                Integer index;
            }
            Listener listener = new Listener();
            listener.index = currentImage;
            rhs.setOnClickListener(listener);
        }

        if (currImage.name.contains("-"))
            main.setText(currImage.name.split("-")[0]);
        else // different styles of separator
            main.setText(currImage.name.split("_")[0]);

        TextView title = (TextView) findViewById(R.id.landmark_screen_tv_title);
        title.setText(landmark.replace('_', ' '));

        TextView txt = (TextView) findViewById(R.id.landmark_screen_tv_text);
        txt.setText(currImage.text);
    }
}