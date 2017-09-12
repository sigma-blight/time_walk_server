package project.chronos.timewalk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Fletcher on 11/09/2017.
 */

public class Client {

    private static final String SERVER_ADDR = "deco3801-Chronos.uqcloud.net";
    private static final int SERVER_PORT = 5001;
    private static final int THREAD_TIMEOUT = 5; // 5 seconds
    private static final String SEPARATOR = "\\|";
    private static final boolean NO_ONLINE_CHECK = false; // macro for connect()
    private static final String DEFAULT_REGION = "Brisbane";

    private Socket _socket;
    private PrintWriter _output;
    private BufferedReader _input;


    private class TransferCode {
        //  ** CODES MUST BE CONSISTENT TO WHAT SERVER EXPECTS **

        public static final int LIST_REGIONS = 0;
        public static final int LIST_LANDMARKS = 1;
        public static final int LIST_IMAGES = 2;
        public static final int GET_REGION_GPS = 10;
        public static final int GET_LANDMARK_GPS = 11;
        public static final int GET_TEXT = 20;
        public static final int GET_IMAGE = 21;
        public static final int GET_POSTCARD_IMAGE_LANDMARK = 30;
        public static final int GET_POSTCARD_IMAGE_REGION = 31;
    }

    private void write(final int transferCode, String... args) {
        StringBuilder builder = new StringBuilder();

        builder.append(transferCode);
        builder.append(" ");

        for (String arg : args)
            builder.append(arg + " ");
        builder.deleteCharAt(builder.length() - 1); // remove last " "
        builder.append(";");

        _output.println(builder.toString());
    }

    private static <ResultType> Data<ResultType> threadErrorData(Exception e) {
        Data<ResultType> data = new Data<>();
        data.result = null;
        data.failed = true;
        data.errorMsg = e.getMessage();
        data.resultCode = ResultCode.THREAD_FAILURE;
        return data;
    }

    private <ResultType> void nonSuccessCode(Data<ResultType> data) throws IOException {
        data.failed = true;
        data.result = null;
        data.errorMsg = _input.readLine();
    }

    private <ResultType> void failedRead(Data<ResultType> data, Exception e) {
        data.failed = true;
        data.result = null;
        data.errorMsg = e.getMessage();
        data.resultCode = ResultCode.BAD_READ;
    }

    private boolean isOnline() {
        return _socket != null && _output != null
                && _input != null &&
                _socket.isConnected();
    }

    private Bitmap httpGetImage(String url_str) throws IOException {
        URL url = new URL(url_str);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        InputStream input = conn.getInputStream();

        return BitmapFactory.decodeStream(input);
    }

    private <ResultType, Params> Data<ResultType> asyncHandler(boolean onlineCheck, AsyncTask<Params, Void, Data<ResultType>> task, Params... params) {

        Data<ResultType> data;

        try {
            if (onlineCheck) {
                if (isOnline())
                    data = task.execute(params).get(THREAD_TIMEOUT, TimeUnit.SECONDS);
                else {
                    data = new Data<>();
                    data.result = null;
                    data.failed = true;
                    data.errorMsg = "Client is Disconnected";
                    data.resultCode = ResultCode.DISCONNECTED;
                }
            } else
                data = task.execute(params).get(THREAD_TIMEOUT, TimeUnit.SECONDS);

        } catch (InterruptedException | ExecutionException e) {
            data = threadErrorData(e);
        } catch (TimeoutException e) {
            data = threadErrorData(e);
            data.resultCode = ResultCode.THREAD_TIMEOUT;
        }

        return data;
    }

    private <ResultType, Params> Data<ResultType> asyncHandler(AsyncTask<Params, Void, Data<ResultType>> task, Params... params) {
        return asyncHandler(true, task, params);
    }

    Data<Void> connect() {
        return asyncHandler(NO_ONLINE_CHECK, new AsyncTask<Void, Void, Data<Void>>() {
            @Override
            protected Data<Void> doInBackground(Void... voids) {
                Data<Void> data = new Data<>();

                try {
                    InetAddress addr = InetAddress.getByName(SERVER_ADDR);
                    _socket = new Socket(addr, SERVER_PORT);
                    _output = new PrintWriter(_socket.getOutputStream(), true);
                    _input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

                } catch (UnknownHostException e) {
                    data.failed = true;
                    data.result = null;
                    data.errorMsg = e.getMessage();
                    data.resultCode = ResultCode.SERVER_DOWN;

                } catch (IOException e) {
                    data.failed = true;
                    data.result = null;
                    data.errorMsg = e.getMessage();
                    data.resultCode = ResultCode.CONNECTION_FAILED;
                }

                return data;
            }
        });
    }


    Data<ArrayList<String>> listLandmarks() {
        return asyncHandler(new AsyncTask<String, Void, Data<ArrayList<String>>>() {
            @Override
            protected Data<ArrayList<String>> doInBackground(String... strings) {
                Data<ArrayList<String>> data = new Data<>();

                // send request
                write(TransferCode.LIST_LANDMARKS, strings[0]);

                try {
                    data.resultCode = Integer.parseInt(_input.readLine());

                    if (data.resultCode == ResultCode.SUCCESS) {
                        data.result = new ArrayList<>();
                        for (String landmark : _input.readLine().split(SEPARATOR))
                            data.result.add(landmark);
                    } else
                        nonSuccessCode(data);

                } catch (Exception e) {
                    failedRead(data, e);
                }

                return data;
            }
        }, DEFAULT_REGION);
    }

    Data<Bitmap> getLandmarkPostcard(String landmark) {
        return asyncHandler(new AsyncTask<String, Void, Data<Bitmap>>() {
            @Override
            protected Data<Bitmap> doInBackground(String... strings) {
                Data<Bitmap> data = new Data<>();

                // send request
                write(TransferCode.GET_POSTCARD_IMAGE_LANDMARK, strings[0], strings[1]);

                try {
                    data.resultCode = Integer.parseInt(_input.readLine());

                    if (data.resultCode == ResultCode.SUCCESS) {

                        String url_str = _input.readLine();
                        data.result = httpGetImage(url_str);

                    } else
                        nonSuccessCode(data);

                } catch (Exception e) {
                    failedRead(data, e);
                }

                return data;
            }
        }, DEFAULT_REGION, landmark);
    }

    Data<ArrayList<String>> listImages(String landmark) {
        return asyncHandler(new AsyncTask<String, Void, Data<ArrayList<String>>>() {
            @Override
            protected Data<ArrayList<String>> doInBackground(String... strings) {
                Data<ArrayList<String>> data = new Data<>();

                // send request
                write(TransferCode.LIST_IMAGES, strings[0], strings[1]);

                try {
                    data.resultCode = Integer.parseInt(_input.readLine());

                    if (data.resultCode == ResultCode.SUCCESS) {
                        data.result = new ArrayList<>();
                        for (String image_name : _input.readLine().split(SEPARATOR))
                            data.result.add(image_name);
                    } else
                        nonSuccessCode(data);

                } catch (Exception e) {
                    failedRead(data, e);
                }

                return data;
            }
        }, DEFAULT_REGION, landmark);
    }

    Data<Bitmap> getImage(String landmark, String image_name) {
        return asyncHandler(new AsyncTask<String, Void, Data<Bitmap>>() {
            @Override
            protected Data<Bitmap> doInBackground(String... strings) {
                Data<Bitmap> data = new Data<>();

                // send request
                write(TransferCode.GET_IMAGE, strings[0], strings[1], strings[2], strings[3]);

                try {
                    data.resultCode = Integer.parseInt(_input.readLine());

                    if (data.resultCode == ResultCode.SUCCESS) {

                        String url_str = _input.readLine();
                        data.result = httpGetImage(url_str);

                    } else
                        nonSuccessCode(data);

                } catch (Exception e) {
                    failedRead(data, e);
                }

                return data;
            }
        }, DEFAULT_REGION, landmark, image_name, "medium"); // TODO: settings -> size
    }

    Data<String> getText(String landmark, String image_name) {
        return asyncHandler(new AsyncTask<String, Void, Data<String>>() {
            @Override
            protected Data<String> doInBackground(String... strings) {
                Data<String> data = new Data<>();

                // send request
                write(TransferCode.GET_TEXT, strings[0], strings[1], strings[2]);

                try {
                    data.resultCode = Integer.parseInt(_input.readLine());

                    if (data.resultCode == ResultCode.SUCCESS) {

                        int txtSize = Integer.parseInt(_input.readLine());
                        StringBuilder builder = new StringBuilder();
                        while (txtSize >= 0) {
                            builder.append((char)_input.read());
                            --txtSize;
                        }

                        data.result = builder.toString();

                    } else {
                        nonSuccessCode(data);
                    }
                } catch (Exception e) {
                    failedRead(data, e);
                }

                return data;
            }
        }, DEFAULT_REGION, landmark, image_name);
    }
}
