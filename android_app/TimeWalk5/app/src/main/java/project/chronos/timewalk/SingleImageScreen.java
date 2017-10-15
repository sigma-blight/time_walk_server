package project.chronos.timewalk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import uk.co.senab.photoview.PhotoViewAttacher;

public class SingleImageScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image_screen);

        ImageView image = new ImageView(this);
        image.setImageBitmap(LandmarkScreen.current_selected_bitmap);
        image.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        PhotoViewAttacher photoAttacher;
        photoAttacher= new PhotoViewAttacher(image);
        photoAttacher.update();

        LinearLayout layout = (LinearLayout) findViewById(R.id.single_image_screen_lyt_image);
        layout.addView(image);
    }
}
