package com.example.anna.arduinobluetooth;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ListNoticeFragment.ListNoticeListener {

    public static final String TAG = "herbata";
    public static final String deviceTag = "com.example.anna.arduinobluetooth.device";
    public static final String adapterTag = "com.example.anna.arduinobluetooth.adapter";
    TextView tv;
    Activity activity;
    int REQUEST_ENABLE_BT =1;
    String[] deviceNamesList;
    Set<BluetoothDevice> pairedDevices;
    ArrayList<BluetoothDevice> devices = new ArrayList<>();
    public static BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ACCESS_COARSE_LOCATION=1;
    RelativeLayout loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activity = this;
        loading = (RelativeLayout) findViewById(R.id.loadingPanel);
        loading.setVisibility(View.GONE);
       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button listDevices = (Button) findViewById(R.id.scan);
        listDevices.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                listEmDevices();
            }
        });

        // Example of a call to a native method
        tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        setupBluetoothAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                     new AlertDialog.Builder(this)
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
                                    "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                REQUEST_ACCESS_COARSE_LOCATION);
                                    }
                                }
                            })
                            .show();
                            //.findViewById(android.R.id.message))
                            //.setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }
    }

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);
                Log.d(TAG, "Found and add device to list: " + device.getName());
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Object[] tmp = devices.stream().map((x)-> {
                    String name = x.getName();
                    if(name == null)
                        name = "DEVICE_WITHOUT_NAME";
                    return name;
                }).toArray();
                deviceNamesList = Arrays.copyOf(tmp,tmp.length, String[].class);

                loading.setVisibility(View.GONE);
                showList(deviceNamesList);
                //TODO: sprawdziś czy lista jest pusta, jak tak to jakiś napis "nie ma deviców"
            }
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                loading.setVisibility(View.VISIBLE);

            }
        }
    };


    private void listEmDevices() {
/*        pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size()>0) {
            devices.addAll(pairedDevices);
            pairedDevices.stream().forEach((x) -> Log.d(TAG, x.getName() + " " + x.getAddress()));
            Object[] tmp = devices.stream().map((x)-> {return x.getName();}).toArray();
            deviceNamesList = Arrays.copyOf(tmp,tmp.length, String[].class);
            for (String s:deviceNamesList) {
                Log.d(TAG,s);
            }
        }
        else
            Log.d(TAG, "nie ma urządzeń");*/
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        if(devices.size()>0)
            devices.clear();
        registerReceiver(bReciever, filter);
        mBluetoothAdapter.startDiscovery();

        Handler handler = new Handler();
        handler.postDelayed(()->{
            mBluetoothAdapter.cancelDiscovery();
        },3000);

    }

    private void setupBluetoothAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            tv.setText("You don't have bluetooth adapter! App will not work!");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void showList(String[] list){
        DialogFragment listDialog = new ListNoticeFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray("list",list);
        listDialog.setArguments(bundle);
        listDialog.show(getFragmentManager(),"ListNoticeFragment");//w(getSupportFragmentManager(), "ListNoticeFragment");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListClick(ListNoticeFragment dialogFragment, int i) {
        if(deviceNamesList.length>=i) {
            Log.d(TAG, deviceNamesList[i]);
            Intent intent = new Intent(this, DisplayDeviceInfo.class);
            intent.putExtra(deviceTag, devices.get(i));
            //intent.putExtra(adapterTag, mBluetoothAdapter.);
            startActivity(intent);
        }
        else
            Log.d(TAG, "nie ma takiej pozycji na liście");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bReciever);
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }



}
