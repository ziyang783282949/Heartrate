package me.chinamao.heartrate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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
    float[] temp2 = new float[3];

    float[] gAbsValue = new float[3];
    float[] lineAcc = new float[3];
    float[] gyoTemp = new float[3];
    float[][] reverseMatrix = new float[3][3];
    //float[] outRotateMatix = new float[9];
    float[][] inWatchRotateMatix = new float[3][3];
    float[][] inPhoneRotateMatrix = new float[3][3];
    long timeStamp = 0;
    float[] rotate = new float[9];
    float[] watchAcc = new float[3];
    float[] phoneAcc = new float[3];
    float[] devicePhoneAcc=new float[3];
    float[] watchLinearAcc = new float[3];
    float[] phoneLinearAcc = new float[3];
    float[] watchG = new float[3];
    float[] phoneG = new float[3];
    float[] devicePhoneG = new float[3];
    private static final String TAG = "sensor";

    private Button start;
    private Button stop;
    private Button process;
    private Button subtract;
    //追加内容
    String beforeLinearAccAndGyo = "";
    //String beforeGyo = "";
    String afterLinearAccAndGyo = "";
    //String afterGyo = "";
    //存储位置
    String beforeFileName = "before.txt";
    String afterFileName = "after.txt";
    String transferFileName = "projection.txt";
    String subTractName = "subtraction.txt";
    String sd = "";

    private long thread = 10;
    private File beforeFile;
    private File afterFile;
    private BufferedWriter beforeBw;
    private BufferedWriter afterBw;

    //projection输出流
    private File transFile;
    private BufferedWriter transBW;
    //sub输出流
    private File subFile;
    private BufferedWriter subBW;

    private TextView tv_time;
    private TextView processResult;
    private static final int msgKey1 = 1;
    private static final int msgKey2 = 2;

    private String fileToBeProceedWatch =
            Environment.getExternalStorageDirectory().getPath() + File.separator + "watch.txt";
    private String fileToBeProceedPhone =
            Environment.getExternalStorageDirectory().getPath() + File.separator + "phone.txt";
    ;
    float[][] isdo=new float[3][3];
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
        getButton();
        new TimeThread().start();
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        process.setOnClickListener(this);
    }

    void startRegist() {
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gSensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
        linearAc = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroscopeSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        sm.registerListener(myListener, aSensor, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(myListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(myListener, gSensor, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(myListener, linearAc, SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(myListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);
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
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = sensorEvent.values;
                //Log.i(TAG, "时间戳：" + timeStamp + "," + accelerometerValues[0] + "," + accelerometerValues[1] + "," + accelerometerValues[2]);
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
                gValues = sensorEvent.values;
                //Log.i(TAG, "时间戳：" + timeStamp + "," + gValues[0] + "," + gValues[1] + "," + gValues[2]);
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                lineAcc = sensorEvent.values;
                //Log.i(TAG, "时间戳：" + timeStamp + "," + lineAcc[0] + "," + lineAcc[1] + "," + lineAcc[2]);
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
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

        SensorManager.getRotationMatrix(rotate, null, gValues, magneticFieldValues);
        temp[0] = rotate[0] * accelerometerValues[0] + rotate[1] * accelerometerValues[1] + rotate[2] * accelerometerValues[2];
        temp[1] = rotate[3] * accelerometerValues[0] + rotate[4] * accelerometerValues[1] + rotate[5] * accelerometerValues[2];
        temp[2] = rotate[6] * accelerometerValues[0] + rotate[7] * accelerometerValues[1] + rotate[8] * accelerometerValues[2];

        temp2[0] = rotate[0] * lineAcc[0] + rotate[1] * lineAcc[1] + rotate[2] * lineAcc[2];
        temp2[1] = rotate[3] * lineAcc[0] + rotate[4] * lineAcc[1] + rotate[5] * lineAcc[2];
        temp2[2] = rotate[6] * lineAcc[0] + rotate[7] * lineAcc[1] + rotate[8] * lineAcc[2];

        gyoTemp[0] = rotate[0] * gyoValues[0] + rotate[1] * gyoValues[1] + rotate[2] * gyoValues[2];
        gyoTemp[1] = rotate[3] * gyoValues[0] + rotate[4] * gyoValues[1] + rotate[5] * gyoValues[2];
        gyoTemp[2] = rotate[6] * gyoValues[0] + rotate[7] * gyoValues[1] + rotate[8] * gyoValues[2];


        //Log.i(TAG, "时间戳：" + tv_time.getText().toString() + ";" + gyoTemp[0] + "," + gyoTemp[1] + "," + gyoTemp[2]);
        sd = timeStampToDate(timeStamp);
        beforeLinearAccAndGyo =
                tv_time.getText().toString() + ";" +
                        String.valueOf(System.currentTimeMillis()) + ";" +
                        accelerometerValues[0] + "," + accelerometerValues[1] + "," + accelerometerValues[2] + ";" +
                        lineAcc[0] + "," + lineAcc[1] + "," + lineAcc[2] + ";" +
                        gyoValues[0] + "," + gyoValues[1] + "," + gyoValues[2] + ";" +
                        temp[0] + "," + temp[1] + "," + temp[2] + ";" +
                        temp2[0] + "," + temp2[1] + "," + temp2[2] + ";" +
                        rotate[0] + "," + rotate[1] + "," + rotate[2] + "," +
                        rotate[3] + "," + rotate[4] + "," + rotate[5] + "," +
                        rotate[6] + "," + rotate[7] + "," + rotate[8]+";"+
                        gyoTemp[0] + "," + gyoTemp[1] + "," + gyoTemp[2];

        writeDealInfo(beforeLinearAccAndGyo, beforeBw);
        //gAbsValue[0] = rotate[0] * gValues[0] + rotate[1] * gValues[1] + rotate[2] * gValues[2];
        //gAbsValue[1] = rotate[3] * gValues[0] + rotate[4] * gValues[1] + rotate[5] * gValues[2];
        //gAbsValue[2] = rotate[6] * gValues[0] + rotate[7] * gValues[1] + rotate[8] * gValues[2];

        afterLinearAccAndGyo = timeStamp + "," + temp[0] + "," + temp[1] + "," + temp[2] + "," + gAbsValue[0] + "," + gAbsValue[1] + "," + gAbsValue[2];
        //Log.i(TAG, "时间戳：" + timeStamp + "," + temp[0] + " " + temp[1] + " " + temp[2]);
        //writeDealInfo(content);
    }

    public void writeDealInfo(String content, BufferedWriter bw) {
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

    String timeStampToDate(long tiestamp) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        return sdf.format(Long.parseLong(String.valueOf(timeStamp) + "000"));
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
        tv_time = findViewById(R.id.timeId);
        process = findViewById(R.id.process);
        processResult = findViewById(R.id.processResult);
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
                    beforeBw = new BufferedWriter(new FileWriter(beforeFile, false));
                    afterBw = new BufferedWriter(new FileWriter(afterFile, false));
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
            case R.id.process: {
                try {
                    Toast.makeText(this, "start process", Toast.LENGTH_LONG).show();
                    processResult.setText("正在处理");
                    //projection
                    transFile = new File(Environment.getExternalStorageDirectory(), transferFileName);
                    transBW = new BufferedWriter(new FileWriter(transFile, false));
                    //sub
                    subFile = new File(Environment.getExternalStorageDirectory(), subTractName);
                    subBW = new BufferedWriter(new FileWriter(subFile, false));
                    new Calculate().start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = msgKey1;
                    mHandler.sendMessage(msg);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    public class Calculate extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                //手表输入流
                FileInputStream inputStreamW = new FileInputStream(fileToBeProceedWatch);
                BufferedReader bufferedReaderW = new BufferedReader(new InputStreamReader(inputStreamW));
                //手机输入流
                FileInputStream inputStreamP = new FileInputStream(fileToBeProceedPhone);
                BufferedReader bufferedReaderP = new BufferedReader(new InputStreamReader(inputStreamP));

                String strW = "";
                String strP = "";
                String[] sW;
                String[] sP;
                String[] sWW;
                String[] sWG;
                String[] sPP;
                String[] sPG;

                long sub = 10;
                boolean flag = true;
                if ((strW = bufferedReaderW.readLine()) != null && (strP = bufferedReaderP.readLine()) != null) {
                    while (flag && strW != null && strP != null) {
                        if (strW != null && strP != null) {
                            sW = strW.split(";");
                            sP = strP.split(";");

                            if (sub > Math.abs(Long.parseLong(sW[1]) - Long.parseLong(sP[1]))) {

                                //使用缓冲区中的方法将数据写入到缓冲区中。
                                sWW = sW[7].split(",");
                                sPP = sP[7].split(",");
                                sWG=sW[4].split(",");
                                sWG=sP[4].split(",");
                                for (int i = 0; i < 3; i++) {
                                    for (int j = 0; j < 3; j++) {
                                        inWatchRotateMatix[i][j] = Float.parseFloat(sWW[i * 3 + j]);
                                        inPhoneRotateMatrix[i][j] = Float.parseFloat(sPP[i * 3 + j]);
                                        isdo[i][j]=Float.parseFloat(sPP[i * 3 + j]);
                                    }
                                }
                                int result=(int)MatrixInverse.determinant(isdo);
                                if(result==0){
                                    strW = bufferedReaderW.readLine();
                                    strP = bufferedReaderP.readLine();
                                    continue;
                                }
                                //reverseMatrix为逆矩阵
                                MatrixInverse.Inverse(inPhoneRotateMatrix, 2, reverseMatrix);
                                //手表和手机加速度
                                watchAcc = MatrixInverse.changeToFloat(sW[5]);
                                phoneAcc = MatrixInverse.changeToFloat(sP[5]);
                                devicePhoneAcc=MatrixInverse.changeToFloat(sP[2]);
                                watchAcc = MatrixInverse.watchToPhone(watchAcc, phoneAcc, reverseMatrix);

                                //手表和手机线性加速度
                                watchLinearAcc = MatrixInverse.changeToFloat(sW[6]);
                                phoneLinearAcc = MatrixInverse.changeToFloat(sP[6]);
                                watchLinearAcc = MatrixInverse.watchToPhone(watchLinearAcc, phoneLinearAcc, reverseMatrix);

                                //手表和手机陀螺仪
                                watchG=MatrixInverse.changeToFloat(sW[8]);
                                phoneG=MatrixInverse.changeToFloat(sP[8]);
                                devicePhoneG=MatrixInverse.changeToFloat(sP[4]);
                                watchG=MatrixInverse.watchToPhone(watchG,phoneG,reverseMatrix);

                                transBW.write(
                                        sW[0] + ";" + sW[1] + ";" + sW[2] + ";" + sW[3] + ";" + sW[4] + ";" +
                                                watchAcc[0] + "," + watchAcc[1] + "," + watchAcc[2] + ";" +
                                                watchLinearAcc[0] + "," + watchLinearAcc[1] + "," + watchLinearAcc[2]+";"+
                                                watchG[0] + "," + watchG[1] + "," + watchG[2]
                                );
                                transBW.newLine();
                                //使用缓冲区中的方法，将数据刷新到目的地文件中去。
                                transBW.flush();

                                subBW.write(
                                        sW[0] + ";" + sW[1]+";"+
                                                (watchAcc[0]-devicePhoneAcc[0])+","+
                                                (watchAcc[1]-devicePhoneAcc[1])+","+
                                                (watchAcc[2]-devicePhoneAcc[2])+";"+
                                                (watchG[0]-devicePhoneG[0])+","+
                                                (watchG[1]-devicePhoneG[1])+","+
                                                (watchG[2]-devicePhoneG[2])
                                );
                                subBW.newLine();
                                //使用缓冲区中的方法，将数据刷新到目的地文件中去。
                                subBW.flush();
                                strW = bufferedReaderW.readLine();
                                strP = bufferedReaderP.readLine();
                            } else if (Long.parseLong(sW[1]) >= Long.parseLong(sP[1])) {
                                strP = bufferedReaderP.readLine();
                            } else {
                                strW = bufferedReaderW.readLine();
                            }
                        } else {
                            flag = false;
                        }
                    }
                }
                //close
                transBW.close();
                subBW.close();
                inputStreamW.close();
                bufferedReaderW.close();
                inputStreamP.close();
                bufferedReaderP.close();
                Message msg = new Message();
                msg.what = msgKey2;
                mHandler.sendMessage(msg);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgKey1:
                    long time = System.currentTimeMillis();
                    Date date = new Date(time);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    tv_time.setText(format.format(date));
                    break;
                case msgKey2:
                    processResult.setText("已完成");
                    break;
                default:
                    break;
            }
        }
    };
}
