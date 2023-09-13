package com.example.proximitydemo

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.OutputStream
import java.net.ConnectException
import java.net.Socket
import java.net.UnknownHostException


class MainActivity : AppCompatActivity(), SensorEventListener {
    private var mClient: Socket? = null
    private val TAG = "ProximityDemoActivity"
    private lateinit var sensorManager: SensorManager
    private var mProximity: Sensor? = null
    private var mConstraintLayout: View? = null
    private var mAudioManager: AudioManager? = null
    private var mOutputStream: OutputStream? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.w(TAG, "onCreate")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            mClient = Socket("127.0.0.1", 8888)
            if (mClient!!.isConnected()) {
                Log.w(TAG, "Socket is connected: 127.0.0.1:8888")
                mOutputStream = mClient!!.getOutputStream()
            } else {
                Log.w(TAG, "Socket is not connected: 127.0.0.1:8888")
            }
        } catch (e: ConnectException) {
            // TODO Auto-generated catch block
            Log.w(TAG, "Socket is not connected: 127.0.0.1:8888")
        }

        mConstraintLayout = findViewById(R.id.layout)
        // Get an instance of the sensor service, and use that to get an instance of
        // a particular sensor.
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        mProximity?.also { proximity ->
            sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        Log.w(TAG, "onAccuracyChanged: $accuracy")

    }

    override fun onSensorChanged(event: SensorEvent) {
        val distance = event.values[0]
        Log.w(TAG, "onSensorChanged: $distance")
        if (distance > 1) {
            mConstraintLayout!!.setBackgroundColor(Color.rgb(255, 255, 255))
            mAudioManager!!.setParameters("VOICE_SPEAKERCHANGE_STATUS=0")
            Log.w(TAG, "setParameter: VOICE_SPEAKERCHANGE_STATUS=0")
            mOutputStream?.write("0".toByteArray())
        } else {
            mConstraintLayout!!.setBackgroundColor(Color.rgb(0, 0, 0))
            mAudioManager!!.setParameters("VOICE_SPEAKERCHANGE_STATUS=1")
            Log.w(TAG, "setParameter: VOICE_SPEAKERCHANGE_STATUS=1")
            mOutputStream?.write("1".toByteArray())
        }


        // Do something with this sensor data.
    }

    override fun onResume() {
        // Register a listener for the sensor.
        Log.w(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.w(TAG, "onPause")

        // Be sure to unregister the sensor when the activity pauses.
        super.onPause()
//        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        mClient?.close()

    }
}