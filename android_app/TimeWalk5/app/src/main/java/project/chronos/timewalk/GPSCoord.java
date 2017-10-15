package project.chronos.timewalk;

/**
 * Created by Fletcher on 11/10/2017.
 */

public class GPSCoord {
    double longitude;
    double latitude;

    GPSCoord() {}
    GPSCoord(double la, double lg) {
        this.longitude = lg;
        this.latitude = la;
    }
}
