package chetan.datatocsv;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SensorService extends Service implements SensorEventListener {

    private FileWriter mFileWriter;
    private static final int REQUEST_LOCATION = 1;
    private File dir;
    private CSVWriter writer;
    private String[] dataContainer = {"Acceleration X", "Acceleration Y", "Acceleration Z", "...", "Latitude", "Longitude"};

    // TAG to identify notification
    private static final int NOTIFICATION = 007;
    LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    String tt;

    // IBinder object to allow Activity to connect
    private final IBinder mBinder = new LocalBinder();
    //private GPSTracker gpsTracker;
    public double lat,lon;
    String lattitude;
    String longitude;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("SERVICE DEBUG", "Service Bound");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        tt  = dateFormat.format(new Date());

        String fileName = intent.getStringExtra("filename");
        String description = intent.getStringExtra("description");

        // Get the root directory
        // .. This is the Device Storage
        File root = android.os.Environment.getExternalStorageDirectory();
        dir = new File(root.getAbsolutePath() + "/DataToCSV");              // Get the path to the new directory to create
        if (!dir.exists()) {                                                // Check if this directory exists
            dir.mkdirs();                                                   // Create a new directory called DataToCSV
        }

        File f = new File(dir, fileName);                                   // Create a new file
        String filePath = f.getAbsolutePath();                              // Get the absolute path to the file
        writer = null;                                                      // Create the CSVWrite object which is from the opencsv library
        if (f.exists() && !f.isDirectory()) {
            try {
                mFileWriter = new FileWriter(filePath, true);              // Initialize file writer
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(mFileWriter);                            // Initialize opencsv writer
        } else {
            try {
                writer = new CSVWriter(new FileWriter(filePath));           // Initialize opencsv writer
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dataContainer[3] = description;
        writer.writeNext(dataContainer);

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("SERVICE DEBUG", "Service UnBound");
        try {
            writer.close();                                                 // Close writer stream
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }

    private Sensor accelerometer;
    private SensorManager mSensorManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SERVICE DEBUG", "Service Created");

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SERVICE DEBUG", "Service Destroyed");

        mSensorManager.unregisterListener(this);                            // Unregister sensor when not in use

        mNotificationManager.cancel(NOTIFICATION);
        stopSelf();

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Get acceleration values
        //getLocation();
        //gpsTracker.getLocation();
        dataContainer[0] = "" + (Math.round(sensorEvent.values[0]*1000)/1000.0);
        dataContainer[1] = "" + (Math.round(sensorEvent.values[1]*1000)/1000.0);
        dataContainer[2] = "" + (Math.round(sensorEvent.values[2]*1000)/1000.0);
        dataContainer[3] = "" + tt;
        dataContainer[4] = "" + MainActivity.lat;
        dataContainer[5] = "" + MainActivity.lon;
        writer.writeNext(dataContainer);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private NotificationManager mNotificationManager;

    private void showNotification() {
        Log.d("SERVICE DEBUG", "Notification Shown");
        CharSequence text = "Started Data Collection";

        // PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        // PendingIntent deleteIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Notification mNotification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.bell)
                    .setTicker(text)
                    .setContentTitle("Hello there!")
                    .setContentText(text)
                    .setAutoCancel(false)
                    .build();

            mNotificationManager.notify(NOTIFICATION, mNotification);
        }
    }
}