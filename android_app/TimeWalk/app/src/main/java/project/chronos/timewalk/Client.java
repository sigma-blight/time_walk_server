package project.chronos.timewalk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Fletcher on 5/09/2017.
 */

public class Client {

    public static final String CLIENT_LOG = "TIME_WALK_CLIENT_LOG";
    private static final String SERVER_ADDR = "deco3801-Chronos.uqcloud.net";
    private static final int SERVER_PORT = 5001;
    private static final String SEPERATOR = " ";

    public enum Transfer {
        TEXT(0),
        IMAGE(1),
        EMPTY_DIRECTORY(2),
        INVALID_REQUEST(10),
        INVALID_DIRECTORY(11),
        INVALID_FILE(12),
        EXCEPTION_FAIL(20),
        THREAD_FAIL(21),
        UNKNOWN(30)
        ;

        public final int code;
        Transfer(int i) {
            this.code = i;
        }
    }

    private enum Request {
        LIST_REGIONS(0),
        LIST_LANDMARKS(1),
        LIST_IMAGES(2),
        GET_GPS(10),
        GET_TEXT(11),
        TRANSFER_IMAGE(12)
        ;

        public final int code;
        Request(int i) {
            this.code = i;
        }
    }

    private String to_string(Request request) {
        return Integer.toString(request.code);
    }

    private Transfer to_code(String code_str) {
        int code = Integer.parseInt(code_str);
        switch(code)
        {
            case 0: return Transfer.TEXT;
            case 1: return Transfer.IMAGE;
            case 2: return Transfer.EMPTY_DIRECTORY;
            case 10: return Transfer.INVALID_REQUEST;
            case 11: return Transfer.INVALID_DIRECTORY;
            case 12: return Transfer.INVALID_FILE;
            default: return Transfer.UNKNOWN;
        }
    }

    public class Data<T> {
        public T result;
        public Transfer code;
        public boolean failed = false;
        public String error_msg;
    }

    Socket _socket;
    PrintWriter _output;
    BufferedReader _input;

    //  **  CONNECTION **

    private class Connection extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try
            {
                InetAddress addr = InetAddress.getByName(SERVER_ADDR);
                _socket = new Socket(addr, SERVER_PORT);
                _output = new PrintWriter(_socket.getOutputStream(), true);
                _input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            } catch (Exception e) {
                Log.e(CLIENT_LOG, "Connection Failed: " + e.getMessage());
                return false;
            }
            return true;
        }
    }

    public boolean connect()
    {
        try {
            Connection conn = new Connection();
            conn.execute().get();
            return true;
        } catch (Exception e) {
            Log.e(CLIENT_LOG, "connect interrupted: " + e.getMessage());
        }
        return false;
    }

    //  **  LANDMARKS  **

    private class Landmarks extends AsyncTask<String, Void, Data<ArrayList<String>>> {

        @Override
        protected Data<ArrayList<String>> doInBackground(String... strings) {
            Data<ArrayList<String>> data = new Data<>();

            _output.println(to_string(Request.LIST_LANDMARKS) + " " + strings[0] + ";");

            try {
                Transfer code = to_code(_input.readLine());
                data.code = code;

                if (code != Transfer.TEXT) {
                    data.error_msg = _input.readLine();
                    data.failed = true;
                } else {
                    data.result = new ArrayList<>();
                    for (String land : _input.readLine().split(SEPERATOR))
                        data.result.add(land);
                }

            } catch (Exception e) {
                Log.e(CLIENT_LOG, "Landmarks Failed: " + e.getMessage());
                data.result = null;
                data.failed = true;
                data.error_msg = e.getMessage();
                data.code = Transfer.EXCEPTION_FAIL;
            }

            return data;
        }
    }

    public Data<ArrayList<String>> landmarks()
    {
        try {
            Landmarks land = new Landmarks();
            return land.execute("Brisbane").get();
        } catch (Exception e) {
            Log.e(CLIENT_LOG, "landmarks Interrupted: " + e.getMessage());

            Data<ArrayList<String>> data = new Data<>();
            data.result = null;
            data.failed = true;
            data.error_msg = e.getMessage();
            data.code = Transfer.THREAD_FAIL;

            return data;
        }
    }

    //  ** IMAGE NAMES **

    private class ImageNames extends AsyncTask<String, Void, Data<ArrayList<String>>> {

        @Override
        protected Data<ArrayList<String>> doInBackground(String... strings) {
            Data<ArrayList<String>> data = new Data<>();

            _output.println(to_string(Request.LIST_IMAGES) + " " +
                    strings[0] + " " + strings[1] + ";");

            try {
                Transfer code = to_code(_input.readLine());
                data.code = code;

                if (code != Transfer.TEXT) {
                    data.error_msg = _input.readLine();
                    data.failed = true;
                } else {
                    data.result = new ArrayList<>();
                    for (String image_name : _input.readLine().split(SEPERATOR))
                        data.result.add(image_name);
                }

            } catch (Exception e) {
                Log.e(CLIENT_LOG, "ImageNames Failed: " + e.getLocalizedMessage());
                data.result = null;
                data.failed = true;
                data.error_msg = e.getMessage();
                data.code = Transfer.EXCEPTION_FAIL;
            }

            return data;
        }
    }

    public Data<ArrayList<String>> imageNames(String landmark) {
        try {
            ImageNames images = new ImageNames();
            return images.execute("Brisbane", landmark).get();
        } catch (Exception e) {
            Log.e(CLIENT_LOG, "imageNames Interrupted: " + e.getMessage());

            Data<ArrayList<String>> data = new Data<>();
            data.result = null;
            data.failed = true;
            data.error_msg = e.getMessage();
            data.code = Transfer.THREAD_FAIL;

            return data;
        }
    }

    //  ** GPS **

    public static class GPSCoord {
        public double longitude;
        public double latitude;
    }

    private class GPS extends AsyncTask<String, Void, Data<GPSCoord>> {

        @Override
        protected Data<GPSCoord> doInBackground(String... strings) {
            Data<GPSCoord> data = new Data<>();

            _output.println(to_string(Request.GET_GPS) + " " +
                strings[0] + " " + strings[1] + ";");

            try {
                Transfer code = to_code(_input.readLine());
                data.code = code;

                if (code != Transfer.TEXT) {
                    data.failed = true;
                    data.result = null;
                    data.error_msg = _input.readLine();
                } else {

                    ArrayList<String> gps_str_arr = new ArrayList<>();
                    for (String gps_str : _input.readLine().split(" "))
                        gps_str_arr.add(gps_str);

                    data.result = new GPSCoord();
                    data.result.longitude = Double.parseDouble(gps_str_arr.get(0));
                    data.result.latitude = Double.parseDouble(gps_str_arr.get(1));
                }

            } catch (Exception e) {
                Log.e(CLIENT_LOG, "GPS Failed: " + e.getLocalizedMessage());
                data.result = null;
                data.failed = true;
                data.error_msg = e.getMessage();
                data.code = Transfer.EXCEPTION_FAIL;
            }

            return data;
        }
    }

    public Data<GPSCoord> gpsCoord(String landmark) {
        try {
            GPS gps = new GPS();
            return gps.execute("Brisbane", landmark).get();
        } catch (Exception e) {
            Log.e(CLIENT_LOG, "gpsCoord Interrupted: " + e.getMessage());

            Data<GPSCoord> data = new Data<>();

            data.result = null;
            data.failed = true;
            data.error_msg = e.getMessage();
            data.code = Transfer.THREAD_FAIL;

            return data;
        }
    }

    //  ** TEXT **

    private class Text extends AsyncTask<String, Void, Data<String>> {

        @Override
        protected Data<String> doInBackground(String... strings) {
            Data<String> data = new Data<>();

            _output.println(to_string(Request.GET_TEXT) + " " +
                strings[0] + " " + strings[1] + " " + strings[2] + ";");

            try {

                Transfer code = to_code(_input.readLine());
                data.code = code;

                if (code != Transfer.TEXT) {
                    data.failed = true;
                    data.error_msg = _input.readLine();
                } else {
                    int size = Integer.parseInt(_input.readLine());
                    Log.d(CLIENT_LOG, "Text Size: " + Integer.toString(size));
                    StringBuilder builder = new StringBuilder();
                    while (size >= 0) {
                        builder.append((char)_input.read());
                        --size;
                    }
                    data.result = builder.toString();
                }

            } catch (Exception e) {
                Log.e(CLIENT_LOG, "Text Failed: " + e.getMessage());
                data.result = null;
                data.failed = true;
                data.error_msg = e.getMessage();
                data.code = Transfer.EXCEPTION_FAIL;
            }

            return data;
        }
    }

    public Data<String> getText(String landmark, String image_name) {

        try {
            Text txt = new Text();
            return txt.execute("Brisbane", landmark, image_name).get();
        } catch (Exception e) {
            Log.e(CLIENT_LOG, "getText Interrupted: " + e.getMessage());

            Data<String> data = new Data<>();

            data.result = null;
            data.failed = true;
            data.error_msg = e.getMessage();
            data.code = Transfer.THREAD_FAIL;

            return data;
        }
    }

    //  ** Transfer Image **

    private class TransferImage extends AsyncTask<String, Void, Data<Bitmap>> {

        @Override
        protected Data<Bitmap> doInBackground(String... strings) {
            Data<Bitmap> data = new Data<>();

            _output.println(to_string(Request.TRANSFER_IMAGE) + " " +
                strings[0] + " " + strings[1] + " " + strings[2] + ";");

            try {

                Transfer code = to_code(_input.readLine());
                data.code = code;

                if (code != Transfer.TEXT) {
                    data.failed = true;
                    data.error_msg = _input.readLine();
                    return data;
                }

                String url = _input.readLine();

                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                data.result = BitmapFactory.decodeStream(input);
                return data;

            } catch (Exception e) {
                Log.e(CLIENT_LOG, "TransferImage Failed: " + e.getMessage());

                data.result = null;
                data.failed = true;
                data.error_msg = e.getMessage();
                data.code = Transfer.EXCEPTION_FAIL;
            }

            return data;
        }
    }

    public Data<ImageView> transferImage(String landmark, String image_name, Context context) {

        Data<ImageView> data = new Data<>();

        try {
            TransferImage transferImage = new TransferImage();
            Data<Bitmap> bitData = transferImage.execute("Brisbane", landmark, image_name).get();

            data.code = bitData.code;
            data.failed = bitData.failed;
            data.error_msg = bitData.error_msg;

            if (data.failed) {
                data.result = null;
                return data;
            }

            data.result = new ImageView(context);
            data.result.setImageBitmap(bitData.result);

        } catch (Exception e) {
            Log.e(CLIENT_LOG, "transferImage Failed: " + e.getMessage());
            data.result = null;
            data.failed = true;
            data.error_msg = e.getMessage();
            data.code = Transfer.THREAD_FAIL;
        }

        return data;
    }
}
