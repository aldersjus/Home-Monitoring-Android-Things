package com.apps.motiondetector;

import android.util.Log;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Justin Alderson
 * Class to represent the date and time of motion.
 * MUST HAVE GETTERS FOR FIREBASE TO WORK CORRECTLY
 */
public class Motion {

    //Variables. If not private will show up in Firebase for example LocalDateTime as an object.
    private LocalDateTime localDateTime;
    private int count;
    private String date;
    private String time;

    //Constants
    private DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private DateTimeFormatter TIME = DateTimeFormatter.ISO_TIME;
    private String THIS_CLASS = "Motion class count: " + getCount();
    private String TAG = "DEBUG";



    Motion(int count) {

        this.localDateTime = LocalDateTime.now();
        this.count = count;
        this.date = localDateTime.format(DATE);
        this.time = localDateTime.format(TIME);

        Log.d(TAG, "Motion class........ This date and time was created. Count: " + count + " Date: " + date + " Time: " + time);
        Log.d(TAG, "Motion class........ LocalDateTime class returned this: " + localDateTime);
    }


    public String toString() {
        return THIS_CLASS;
    }

    //Package level access getters.. MUST HAVE GETTERS FOR FIREBASE TO WORK CORRECTLY
    int getCount() {
        return count;
    }

    String getDate(){
        return date;
    }

    String getTime(){
        return time;
    }

}
