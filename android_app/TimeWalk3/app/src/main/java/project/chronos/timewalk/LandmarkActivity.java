package project.chronos.timewalk;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class LandmarkActivity extends AppCompatActivity {

    private Client client = HomeScreenActivity.client;
    private String landmark;
    private ArrayList<String> image_names;
    private int current = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark);

        landmark = getIntent().getStringExtra(SearchActivity.LANDMARK_TAG);
        Data<ArrayList<String>> data = client.listImages(landmark);

        if (data.failed)
            HomeScreenActivity.projectTimeWalkDown(getApplicationContext());
        else {
            image_names = data.result;
            loadCurrent();
        }
    }

    private void loadCurrent() {

        Data<Bitmap> data = client.getImage(landmark, image_names.get(current));

        if (!data.failed) {
            ImageView image = new ImageView(getApplicationContext());
            image.setImageBitmap(data.result);

            LinearLayout layout = (LinearLayout) findViewById(R.id.landmark_screen_image_layout);
            layout.addView(image);
        }

        Data<String> txtData = client.getImageText(landmark, image_names.get(current));

        if (!txtData.failed) {
            TextView txtView = (TextView) findViewById(R.id.landmark_screen_text_txt);
            txtView.setText(txtData.result);
        }
    }
}
