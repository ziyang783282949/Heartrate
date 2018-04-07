package me.chinamao.heartrate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static android.hardware.Sensor.TYPE_HEART_RATE;
import static java.lang.String.valueOf;

public class MainActivity extends WearableActivity implements View.OnClickListener{

    private SensorManager sm;
    private Sensor aSensor;  //加速度传感器
    private Sensor mSensor; //方向传感器
    private Sensor gSensor; //重力传感器
    private Sensor linearAc;//线性加速度传感器
    private Sensor gyroscopeSensor;//陀螺仪传感器

    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    float[] gValues = new float[3];
    float[] gyoValues = new float[3];

    float[] temp = new float[3];
    float[] gAbsValue=new float[3];
    float[] lineAcc=new float[3];
    long timeStamp = 0;
    float[] rotate = new float[9];
    private static final String TAG = "sensor";

    private Button start;
    private Button stop;
    //追加内容
    String beforeLinearAccAndGyo = "";
    //String beforeGyo = "";
    String afterLinearAccAndGyo = "";
    //String afterGyo = "";
    //存储位置
    String beforeFileName = "before.txt";
    String afterFileName = "after.txt";

    private File beforeFile;
    private File afterFile;
    private BufferedWriter beforeBw;
    private BufferedWriter afterBw;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
        getButton();
        start.setOnClickListener(this);
        stop.setOnClickListener(this);



    }

    void startRegist(){
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gSensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
        linearAc = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroscopeSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, gSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, linearAc, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(myListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //更新显示数据的方法
        calculateOrientation();
    }
    public void onPause() {
        //sm.unregisterListener(myListener);
        super.onPause();
    }

    final SensorEventListener myListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                magneticFieldValues = sensorEvent.values;
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                accelerometerValues = sensorEvent.values;
                //Log.i(TAG, "时间戳：" + timeStamp + "," + accelerometerValues[0] + "," + accelerometerValues[1] + "," + accelerometerValues[2]);
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY){
                gValues = sensorEvent.values;
                //Log.i(TAG, "时间戳：" + timeStamp + "," + gValues[0] + "," + gValues[1] + "," + gValues[2]);
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                lineAcc = sensorEvent.values;
                //Log.i(TAG, "时间戳：" + timeStamp + "," + lineAcc[0] + "," + lineAcc[1] + "," + lineAcc[2]);
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                gyoValues = sensorEvent.values;
                //Log.i(TAG, "时间戳：" + timeStamp + "," + lineAcc[0] + "," + lineAcc[1] + "," + lineAcc[2]);
            }
            timeStamp = sensorEvent.timestamp;
            calculateOrientation();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void calculateOrientation() {
        beforeLinearAccAndGyo = timeStamp + "," + lineAcc[0] + "," + lineAcc[1] + "," + lineAcc[2]+","+gyoValues[0]+","+gyoValues[1]+","+gyoValues[2];
        Log.i(TAG, "时间戳：" + timeStamp + "," + gyoValues[0]+","+gyoValues[1]+","+gyoValues[2]);
        writeDealInfo(beforeLinearAccAndGyo,beforeBw);
        SensorManager.getRotationMatrix(rotate, null, gValues, magneticFieldValues);
        temp[0] = rotate[0] * lineAcc[0] + rotate[1] * lineAcc[1] + rotate[2] * lineAcc[2];
        temp[1] = rotate[3] * lineAcc[0] + rotate[4] * lineAcc[1] + rotate[5] * lineAcc[2];
        temp[2] = rotate[6] * lineAcc[0] + rotate[7] * lineAcc[1] + rotate[8] * lineAcc[2];

        //gAbsValue[0] = rotate[0] * gValues[0] + rotate[1] * gValues[1] + rotate[2] * gValues[2];
        //gAbsValue[1] = rotate[3] * gValues[0] + rotate[4] * gValues[1] + rotate[5] * gValues[2];
        //gAbsValue[2] = rotate[6] * gValues[0] + rotate[7] * gValues[1] + rotate[8] * gValues[2];

        afterLinearAccAndGyo = timeStamp + "," + temp[0] + "," + temp[1] + "," + temp[2]+","+gAbsValue[0]+","+gAbsValue[1]+","+gAbsValue[2];
        //Log.i(TAG, "时间戳：" + timeStamp + "," + temp[0] + " " + temp[1] + " " + temp[2]);
        //writeDealInfo(content);
    }

    public void writeDealInfo(String content,BufferedWriter bw) {
        try {
            /**
             * 为了提高写入的效率，使用了字符流的缓冲区。
             * 创建了一个字符写入流的缓冲区对象，并和指定要被缓冲的流对象相关联。
             */
            //使用缓冲区中的方法将数据写入到缓冲区中。
            bw.write(content);
            bw.newLine();
            //使用缓冲区中的方法，将数据刷新到目的地文件中去。
            bw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPermission() {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void getButton() {
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start: {
                try {
                    Toast.makeText(this, "start", Toast.LENGTH_LONG).show();
                    startRegist();
                    beforeFile = new File(Environment.getExternalStorageDirectory(), beforeFileName);
                    afterFile = new File(Environment.getExternalStorageDirectory(), afterFileName);
                    //第二个参数意义是说是否以append方式添加内容
                    beforeBw = new BufferedWriter(new FileWriter(beforeFile, true));
                    afterBw = new BufferedWriter(new FileWriter(afterFile, true));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.stop: {
                try {
                    Toast.makeText(this, "stop", Toast.LENGTH_LONG).show();
                    //关闭缓冲区,同时关闭了FileWriter流对象
                    beforeBw.close();
                    afterBw.close();
                    sm.unregisterListener(myListener);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }

        }
    }
}
