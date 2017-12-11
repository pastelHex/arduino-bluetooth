package com.example.anna.arduinobluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Anna on 10.12.2017.
 */

public class ConnectTask extends AsyncTask<Object, Void, BluetoothSocket> {

    private BluetoothSocket mmSocket = null;
    private BluetoothDevice mmDevice = null;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    ConnectTask(BluetoothDevice bd) {
        this.mmDevice = bd;
/*        boolean bond;
        try {
            // bond = mmDevice.createBond();
            ParcelUuid list[] = bd.getUuids();
            Log.d(MainActivity.TAG, Arrays.asList(list).toString());
        } catch (Exception e) {
            Log.e(MainActivity.TAG, e.toString());
            e.printStackTrace();
        }*/
    }

    @Override
    protected BluetoothSocket doInBackground(Object[] objects) {
        OutputStream mmOutputStream;
        InputStream mmInputStream;
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            mmSocket.connect();

            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            mmOutputStream.flush();
            String msg = "Hello from android!";
            byte[] message = msg.getBytes();

            mmOutputStream.write(msg.getBytes());
            mmOutputStream.write(0x0d);
            mmOutputStream.write(0x0a);;
            mmOutputStream.flush();
        } catch (IOException connectException) {
            Log.e(MainActivity.TAG, connectException.toString());
            connectException.printStackTrace();
            try {
                mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocketToServiceRecord", new Class[]{UUID.class}).invoke(mmDevice, MY_UUID);
                mmSocket.connect();
                mmOutputStream = mmSocket.getOutputStream();
                mmInputStream = mmSocket.getInputStream();

                String msg = "Hello from android!";
                msg += "\r\n";
                mmOutputStream.write(msg.getBytes());
            }catch(Exception e){
                Log.e(MainActivity.TAG, e.toString());
                e.printStackTrace();
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return mmSocket;
    }

        @Override
        protected void onProgressUpdate (Void...values){
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled () {
            super.onCancelled();
        }
    }
