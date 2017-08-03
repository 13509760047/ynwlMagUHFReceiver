package com.fxb.receiver;

import android.app.Application;
import android.hardware.uhf.magic.DevBeep;
import android.hardware.uhf.magic.reader;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class App extends Application {
    private static RequestQueue rq;
    static String C5U = "/dev/ttyMT1";
    static String C7DU = "/dev/ttyMT2";
    static String CM550 = "/dev/ttyMT2";
    static String CM398M = "/dev/ttyMSM0";

    @Override
    public void onCreate() {
        super.onCreate();
        InitUHF(CM398M);
        rq = Volley.newRequestQueue(getApplicationContext());
    }

    /**
     * Module initialization
     * Scanning sound initialization
     *
     * @param type
     */
    public void InitUHF(String type) {
        reader.init(type);
        reader.Open(type);
        reader.SetTransmissionPower(1950);
        DevBeep.init(App.this);
    }

    public static RequestQueue getRequestQueue() {
        return rq;
    }

}
