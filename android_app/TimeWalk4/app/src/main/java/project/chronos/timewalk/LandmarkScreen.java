package project.chronos.timewalk;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

public class LandmarkScreen extends AppCompatActivity {

    private class Image {
        String text;
        Date date;
        Bitmap bitmap;
        String name;
    }

    private Client client = HomeScreen.client;
    private String landmark;
    private ArrayList<Image> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_screen);

        landmark = getIntent().getStringExtra(SearchScreen.INTENT_TAG_LANDMARK);
        Data<ArrayList<String>> imageData = client.listImages(landmark);

        if (imageData.failed)
            Utility.toastResultCodeError(imageData.resultCode, getApplicationContext());
        else {
            images = new ArrayList<>();
            for (String image_name : imageData.result) {
                Image image = new Image();
                image.name = image_name;
                // TODO: image.date
                images.add(image);
            }

            loadImages();
        }

        displayImages();
    }

    private void loadImages() {
        for (Image image : images) {
            Data<Bitmap> bitmapData = client.getImage(landmark, image.name);
            Data<String> txtData = client.getText(landmark, image.name);

            if (bitmapData.failed &&
                    txtData.failed)
                images.remove(image);

            if (!bitmapData.failed)
                image.bitmap = bitmapData.result;
            if (!txtData.failed)
                image.text = txtData.result;
        }
    }

    private CardView createCardView(Image image) {

        CardView card = new CardView(this);

        class Listener implements View.OnClickListener {

            Image image;
            Listener(Image image) {
                this.image = image;
            }

            @Override
            public void onClick(View view) {
                TextView text = (TextView) findViewById(R.id.landmark_screen_txt_description);
                text.setText(image.text);
            }
        }

        card.setUseCompatPadding(true);
        card.setRadius(20);
        card.setCardElevation(10);

        card.setOnClickListener(new Listener(image));
        return card;
    }

    private ImageView createImage(Bitmap bitmap) {
        ImageView image = new ImageView(this);
        image.setImageBitmap(bitmap);
        image.setDrawingCacheEnabled(true);
        return image;
    }

    private void displayImages() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.landmark_screen_lyt_images);

        for (Image image : images) {

            CardView card = createCardView(image);
            card.addView(createImage(image.bitmap));

            layout.addView(card);
        }

        if (images.size() > 0) {
            TextView text = (TextView) findViewById(R.id.landmark_screen_txt_description);
            text.setText(images.get(0).text);
        }
    }
}
