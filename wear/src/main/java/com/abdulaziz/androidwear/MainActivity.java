package com.abdulaziz.androidwear;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements SensorEventListener,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final long CONNECTION_TIME_OUT_MS = 100;
    private static final String DEBUG_TAG = "Gestures";
    private static final String MESSAGE1 = "Hello Watch! You're in Control";
    private static final String MESSAGE2 = "I'm running late, but I'll be there soon.";
    private static final String MESSAGE3 = "Sorry, I missed your call.";
    private static final String MESSAGE4 = "Please call me when you get this message.";
    private static final String MESSAGE5 = "I'm in the airport now, I'll talk to you later.";
    private static final String MESSAGE6 = "Good Bye Phone!";



    private static final float SHAKE_THRESHOLD = 2.1f;
    private static final int SHAKE_WAIT_TIME_MS = 250;
    private static final float ROTATION_THRESHOLD = 3.0f;
    private static final int ROTATION_WAIT_TIME_MS = 100;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private GestureDetectorCompat mDetector;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long mShakeTime = 0;
    private long mRotationTime = 0;

    private GoogleApiClient client;
    private String nodeId;

    private DismissOverlayView mDismissOverlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initApi();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {


            @Override
            public void onLayoutInflated(WatchViewStub stub) {
               // setupWidgets();
            }
        });



        mSensorManager = (SensorManager) MainActivity.this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = new GestureDetectorCompat(this,this);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);


        // Obtain the DismissOverlayView element
        mDismissOverlayView = (DismissOverlayView) findViewById(R.id.dismiss_overlay);



            mDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {

                @Override
            public void onLongPress(MotionEvent e){
                mDismissOverlayView.show(); // exit wear app
                //Toast.makeText(getBaseContext(),"LongPress", Toast.LENGTH_SHORT).show();

            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                sendMessage2();
                Toast.makeText(getBaseContext(),"Message2 Sent", Toast.LENGTH_SHORT).show();
                Log.d(DEBUG_TAG, "onDoubleTap: " + e.toString());
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                sendMessage4();
                                Toast.makeText(getBaseContext(), "Message4 Sent", Toast.LENGTH_SHORT).show();

                            } else {
                                sendMessage3();
                                Toast.makeText(getBaseContext(), "Message3 Sent", Toast.LENGTH_SHORT).show();

                            }
                        }
                        result = true;

                    } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            sendMessage5();
                            Toast.makeText(getBaseContext(), "Message5 Sent", Toast.LENGTH_SHORT).show();
                        } else {
                            //SwipeTop
                        }
                    }

                    result = true;

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }


            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                sendMessage1();
                Toast.makeText(getBaseContext(),"Lunching App...", Toast.LENGTH_SHORT).show();
                Log.d(DEBUG_TAG, "onSingleTapUp: " + e.toString());
                return true;
            }

        });
    }

    /**
     * Initializes the GoogleApiClient and gets the Node ID of the connected device.
     */
    private void initApi() {
        client = getGoogleApiClient(this);
        retrieveDeviceNode();
    }


    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // If sensor is unreliable, then just return
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            return;
        }



        if( event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //detectRotation(event);
            //detectShake(event);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    // References:
    //  - http://jasonmcreynolds.com/?p=388
    //  - http://code.tutsplus.com/tutorials/using-the-accelerometer-on-android--mobile-22125
    private void detectShake(SensorEvent event) {
        long now = System.currentTimeMillis();

        if((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
            mShakeTime = now;

            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement
            float gForce = FloatMath.sqrt(gX * gX + gY * gY + gZ * gZ);

            // Change background color if gForce exceeds threshold;
            // otherwise, reset the color
            if(gForce > SHAKE_THRESHOLD) {
                //mView.setBackgroundColor(Color.rgb(0, 100, 100));
                sendMessage2();
            }
            else {
                //mView.setBackgroundColor(Color.BLACK);
            }
        }
    }

    private void detectRotation(SensorEvent event) {
        long now = System.currentTimeMillis();

        if((now - mRotationTime) > ROTATION_WAIT_TIME_MS) {
            mRotationTime = now;

            // Change background color if rate of rotation around any
            // axis and in any direction exceeds threshold;
            // otherwise, reset the color
            if(Math.abs(event.values[0]) > ROTATION_THRESHOLD){
                //mView.setBackgroundColor(Color.rgb(100, 100, 0));

            } else if (Math.abs(event.values[1]) > ROTATION_THRESHOLD ){
                // mView.setBackgroundColor(Color.rgb(0, 100, 100));
                sendMessage5();

            }else if (Math.abs(event.values[2]) > ROTATION_THRESHOLD) {
                //mView.setBackgroundColor(Color.rgb(0, 100, 0));
                //sendMessage3();
            }
            else {
                //mView.setBackgroundColor(Color.BLACK);

            }
        }
    }


    /**
     * Returns a GoogleApiClient that can access the Wear API.
     * @param context
     * @return A GoogleApiClient that can make calls to the Wear API
     */
    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    /**
     * Connects to the GoogleApiClient and retrieves the connected device's Node ID. If there are
     * multiple connected devices, the first Node ID is returned.
     */
    private void retrieveDeviceNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    nodeId = nodes.get(0).getId();
                }
                client.disconnect();
            }
        }).start();
    }

    /**
     * Sends a message to the connected mobile device, telling it to show a Toast. Reset the image
     */
    private void sendMessage1() {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, MESSAGE1, null);
                    client.disconnect();
                }
            }).start();
        }
    }

    /**
     * Sends a message to the connected mobile device, telling it to show a Toast.
     */
    private void sendMessage2() {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, MESSAGE2, null);
                    client.disconnect();
                }
            }).start();
        }
    }

    /**
     * Sends a message to the connected mobile device, telling it to show a Toast.
     */
    private void sendMessage3() {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, MESSAGE3, null);
                    client.disconnect();
                }
            }).start();
        }
    }

    /**
     * Sends a message to the connected mobile device, telling it to show a Toast.
     */
    private void sendMessage4() {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, MESSAGE4, null);
                    client.disconnect();
                }
            }).start();
        }
    }

    /**
     * Sends a message to the connected mobile device, telling it to show a Toast.
     */
    private void sendMessage5() {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, MESSAGE5, null);
                    client.disconnect();
                }
            }).start();
        }
    }

    /**
     * Sends a message to the connected mobile device, telling it to show a Toast.
     */
    private void sendMessage6() {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, MESSAGE6, null);
                    client.disconnect();
                }
            }).start();
        }
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent e) {
        return mDetector.onTouchEvent(e) || super.dispatchTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        return mDetector.onTouchEvent(e) || super.onTouchEvent(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onLongPress (MotionEvent e){
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        return true;
    }
    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return true;
    }



}

