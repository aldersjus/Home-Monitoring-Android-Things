package com.apps.motiondetector;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;


/**
 * Base Android Things activity.
 * @author Justin Alderson
 * Application to monitor movement. Uses PIR sensor and several LEDs.
 * Detects movement, lights LEDs, and uploads movements to Firebase.
 */

public class MainActivity extends Activity{

    private static final String TAG = "DEBUG";

    //Setup pins to use
    public static final String PIR_PIN = "BCM17";
    public static final String LED_PIN_MOVEMENT = "BCM13";
    public static final String LED_PIN_SUCCESS = "BCM5";
    public static final String LED_PIN_FAILURE = "BCM6";

    //Setup some Gpio variables
    private Gpio mPirGpio;
    private Gpio mLedGpioMovement;
    private Gpio mLedGpioSuccess;
    private Gpio mLedGpioFailure;


    //ints
    private int count = 0;

    //Firebase
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    //Manages sensors etc..
    private PeripheralManager service;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("your_database_name");

        Log.d(TAG, " Write to here" + myRef);


        service = PeripheralManager.getInstance();
        try {
            // set PIR sensor as button for LED
            // Create GPIO connection.
            mPirGpio = service.openGpio(PIR_PIN);
            // Configure as an input.
            mPirGpio.setDirection(Gpio.DIRECTION_IN);
            // Enable edge trigger events for both falling and rising edges. This will make it a toggle button.
            mPirGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            // Register an event callback. Used as PIR values changed and lights LED.
            mPirGpio.registerGpioCallback(mSetLEDCallback);

            // set LED as output
            // Create GPIO connection.
            mLedGpioMovement = service.openGpio(LED_PIN_MOVEMENT);
            mLedGpioSuccess = service.openGpio(LED_PIN_SUCCESS);
            mLedGpioFailure = service.openGpio(LED_PIN_FAILURE);
            // Configure as output.
            mLedGpioMovement.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioSuccess.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            mLedGpioFailure.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // Close the resource
        if (mPirGpio != null) {
            mPirGpio.unregisterGpioCallback(mSetLEDCallback);
            try {
                mPirGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (mLedGpioMovement != null) {
            try {
                mLedGpioMovement.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (mLedGpioSuccess != null) {
            try {
                mLedGpioSuccess.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        if (mLedGpioFailure != null) {
            try {
                mLedGpioFailure.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }

    // Register an event callback.
    private GpioCallback mSetLEDCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            Log.i(TAG, "GPIO callback....... " + count);
            count += 1;

            if (mLedGpioMovement == null) {
                return true;
            }
            try {
                Log.i(TAG, "GPIO callback and value " + gpio.getValue());
                // set the LED state to opposite of input state
                mLedGpioMovement.setValue(gpio.getValue());

                if(mLedGpioMovement.getValue()){
                    Log.e(TAG, "somebody walked past the sensor.....................");
                }
                //Delay so not to get too much movement.
                Thread.sleep(2000);
            } catch (Exception e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
            // Return true to keep callback active.
            onMotion();
            return true;

        }

        //MOTION METHOD
        private void onMotion(){
            Log.d(TAG,"MainActivity created a Firebase: " + myRef.toString());

            //Create Motion and upload to Firebase.
            Motion motion = new Motion(count);
            Log.d(TAG,"MainActivity created a Motion Class: " + motion.toString());

            myRef.child(motion.toString() + motion.getCount()).setValue(motion)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {

                        @Override
                        public void onSuccess(Void aVoid) {
                            // Write was successful!  Could light a green LED to show success.//
                            Log.d(TAG, " Write was successful." );
                            try{


                                mLedGpioSuccess.setValue(true);
                                Thread.sleep(1000);
                                mLedGpioSuccess.setValue(false);

                            }catch (Exception e){
                                Log.d(TAG, " onSuccessLED() LED" + e.getMessage());
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {

                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Write failed Could light a Red LED to show failure.//
                            Log.d(TAG, " Write FAILED." );
                            try{


                                mLedGpioFailure.setValue(true);
                                Thread.sleep(1000);
                                mLedGpioFailure.setValue(false);

                            }catch (Exception ee){
                                Log.d(TAG, " onFalure() LED " + ee.getMessage());
                            }
                        }
                    });

        }
    };

}
