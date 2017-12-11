package com.example.anna.arduinobluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;


/**
 * Created by Anna on 09.12.2017.
 */

public class DisplayDeviceInfo extends Activity {

    BluetoothDevice device;
    Intent intent;
    BluetoothSocket socket;
    InputStream inputStream;
    OutputStream outputStream;
    EditText text;
    Button send;
    Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_info);
        intent = getIntent();

        TextView deviceInfo = (TextView) findViewById(R.id.deviceInfo);
        writeInfo(deviceInfo);

        Button back = (Button) findViewById(R.id.back);
        back.setOnClickListener((v)->{
            finish();
        });

        Button chooseActivity = (Button) findViewById(R.id.pair);
        chooseActivity.setOnClickListener((v)->{
            pairWithDevice();
        });

        send = (Button) findViewById(R.id.send);
        send.setEnabled(false);
        send.setOnClickListener((v)->{
            sendSome();
        });

        text = (EditText) findViewById(R.id.text);

        Button clear = (Button) findViewById(R.id.clear);
        clear.setOnClickListener((v)->{
            text.setText("");
        });

        TextView messages = (TextView) findViewById(R.id.messages);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                messages.append(msg.obj.toString());
            }
        };
    }

    private void pairWithDevice(){
        //MainActivity.mBluetoothAdapter
        ConnectTask connect = new ConnectTask(device);
        try {
            socket = connect.execute().get();
            //inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        send.setEnabled(true);

        new ConnectionThread(socket).start();
    }

    private void sendSome(){
        String msg = text.getText().toString();
        msg += "\r\n";
        try {

            outputStream.write(msg.getBytes());
            outputStream.write(0x0d);
            outputStream.write(0x0a);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ConnectionThread extends Thread{

        private BluetoothSocket socket;
        private InputStream inputStream;
        private byte[] buffer;

        private ConnectionThread(BluetoothSocket socket){
            this.socket = socket;
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run(){
            buffer = new byte[1024];
            int num;
            while(true){
                try{
                    num = inputStream.read(buffer);
                    Message msg = new Message();
                    msg.obj = new String(buffer, 0, num, StandardCharsets.UTF_8);
                    handler.sendMessage(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void writeInfo(TextView tv){
        device = intent.getParcelableExtra(MainActivity.deviceTag);
        tv.setText("Name: " + device.getName() + "\n");
        tv.append("Adress: " + device.getAddress() + "\n");
        tv.append("Type: " + getType(device) + "\n");
        tv.append("Class: " + getDeviceClass(device) + "\n");

    }
    private String getType(BluetoothDevice bd){
        int type = bd.getType();
        String t = "";
        switch(type) {
            case 1:
                t = "Classic - BR/EDR devices";
                break;
            case 3:
                t = "Dual Mode - BR/EDR/LE";
                break;
            case 2:
                t = "Low Energy - LE-only";
                break;
            case 0:
                t = "Unknown";
                break;
        }
        return t;
    }
    private String getDeviceClass(BluetoothDevice bd){
        int i = bd.getBluetoothClass().getMajorDeviceClass();
        String t = "";
        Log.d(MainActivity.TAG, "Major device class: " + i);
        switch(i){
            case 1024:
                t = "AUDIO_VIDEO";
                break;
            case 2304:
                t = "HEALTH";
                break;
            case 256:
                t = "COMPUTER";
                break;
            case 512:
                t = "PHONE";
                break;
            case 1536:
                t = "IMAGING";
                break;
            case 0:
                t = "MISC";
                break;
            case 768:
                t = "NETWORKING";
                break;
            case 1280:
                t = "PERIPHERAL";
                break;
            case 2048:
                t = "TOY";
                break;
            case 7936:
                t = "UNCATEGORIZED";
                break;
        }
        return t;
    }
}
