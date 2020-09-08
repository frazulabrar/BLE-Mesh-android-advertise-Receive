/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothadvertisements;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;
import static android.content.Context.BIND_AUTO_CREATE;
import static android.support.v4.app.ActivityCompat.invalidateOptionsMenu;
import static com.example.android.bluetoothadvertisements.BluetoothLEService.ACTION_GATT_CONNECTED;
import static com.example.android.bluetoothadvertisements.BluetoothLEService.ACTION_GATT_DISCONNECTED;


/**
 * Scans for Bluetooth Low Energy Advertisements matching a filter and displays them to the user.
 */
public class ScannerFragment extends ListFragment {

    private static final String TAG = ScannerFragment.class.getSimpleName();
    private int Battery_data;

    /**
     * Stops scanning after 5 seconds.
     */
    private static final long SCAN_PERIOD = 5000;//ms i.e 5secs

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothLeScanner mBluetoothLeScanner;

    private ScanCallback mScanCallback;

    private ScanResultAdapter mAdapter;

    private Handler mHandler;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean mConnected = false;
    private BluetoothLEService mBluetoothLEService = new BluetoothLEService();
    BluetoothDevice bluetoothDevice;
    private String Updated_data = "fraz";
    //public ListView lv;
    private boolean mScanning;
    protected String mDeviceAddress;


    /**
     * Must be called after object creation by MainActivity.
     *
     * @param btAdapter the local BluetoothAdapter
     */
    public void setBluetoothAdapter(BluetoothAdapter btAdapter) {
        this.mBluetoothAdapter = btAdapter;
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this.getActivity(), "ble not supported", Toast.LENGTH_SHORT).show();
            finishActivity();
        }


        final BluetoothManager mbluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mbluetoothManager.getAdapter();// TODO adapter!


        // Use  instead of just getActivity() because this
        // object lives in a fragment and needs to be kept separate from the Activity lifecycle.
        //
        // We could get a LayoutInflater from the ApplicationContext but it messes with the
        // default theme, so generate it from getActivity() and pass it in separately.
        mAdapter = new ScanResultAdapter(getActivity().getApplicationContext(),
                LayoutInflater.from(getActivity()));
        mHandler = new Handler();
        //TODO
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.REQUEST_LOCATION_ENABLE_CODE);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        getActivity().unregisterReceiver(mGattUpdateReceiver);
        getActivity().unbindService(mServiceConnection);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopScanning();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume of scanner fregment");
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            }
        }
        //  mBluetoothLEService.initialize();
//        Intent gattServiceIntent = new Intent(getActivity(), BluetoothLEService.class);
//        getActivity().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        getActivity().registerReceiver(mGattUpdateReceiver, GattUpdateIntentFilter());
        startScanning();
//            if (mBluetoothLEService != null)
//            {  final boolean result = mBluetoothLEService.connect(bluetoothDevice.getAddress());}



        super.onResume();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final View view = super.onCreateView(inflater, container, savedInstanceState);


        setListAdapter(mAdapter);
        Log.d(TAG, "setting adap");


        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (bluetoothDevice != null) {
            Intent gattServiceIntent = new Intent(getActivity(), BluetoothLEService.class);
            getActivity().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            mBluetoothLEService.connect(mAdapter.getDevice(position).getAddress());//bluetoothDevice.getAddress());
            mDeviceAddress = mAdapter.getDevice(position).getAddress();
            Log.d(TAG, "address chal ra?" + mDeviceAddress);


        }
        if (mNotifyCharacteristic != null) {
            Log.d(TAG, "bleeeeeee" + mNotifyCharacteristic);
            final int charaProp = mNotifyCharacteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                mBluetoothLEService.readCharacteristic(mNotifyCharacteristic);

            }

        }

        //     Toast.makeText(getActivity(), "You received "+Updated_data , Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Toast?????????????");

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

          getListView().setDivider(null);
      getListView().setDividerHeight(1);
    //  getListView().setOnItemClickListener()
//
       setEmptyText(getString(R.string.empty_list));


        // Trigger refresh on app's 1st load



    }

    @Override
    public void onPause() {
        super.onPause();

        mAdapter.clear(); //TODO minimize app bug handling !!!


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.scanner_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refresh:
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
                startScanning();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            bluetoothDevice = result.getDevice();
            mAdapter.addDevice(bluetoothDevice);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                mAdapter.addDevice(result.getDevice());
            }
            mAdapter.notifyDataSetChanged();
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "Scanning Failed " + errorCode);

        }
    };



    /**
     * Start scanning for BLE Advertisements (& set it up to stop after a set period of time).
     */
//TODO scanning !!!! Thumar
    public void startScanning() {
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        //if (scanCallback == null) {
        Log.d(TAG, "Starting Scanning");

        // Will stop the scanning after a set time.
        mHandler.postDelayed(   new Runnable() {
            @Override
            public void run() {
                mScanning=false;
                getActivity().invalidateOptionsMenu();
                stopScanning();

            }
        }, SCAN_PERIOD);

        // Kick off a new scan.
//            m
//
//
//      = new ScanCallback();

        mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), scanCallback);
        Log.d(TAG, "scan callbk ");

        String toastText = getString(R.string.scan_start_toast) + " "
                + TimeUnit.SECONDS.convert(SCAN_PERIOD, TimeUnit.MILLISECONDS) + " "
                + getString(R.string.seconds);
        Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
     getActivity().invalidateOptionsMenu();
    }

    /**
     * Stop scanning for BLE Advertisements.
     */
    public void stopScanning() {
        Log.d(TAG, "Stopping Scanning");

        // Stop the scan, wipe the callback.
        mBluetoothLeScanner.stopScan(scanCallback);
        //scanCallback = null;
        mScanning=false;

        // Even if no new results, update 'last seen' times.
        mAdapter.notifyDataSetChanged();

    }

    /**
     * Return a List of {@link ScanFilter} objects to filter by Service UUID.
     */
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(Constants.Battery_Service_UUID);
        scanFilters.add(builder.build());

        return scanFilters;
    }

    /**
     * Return a {@link ScanSettings} object set to use low power (to preserve battery life).
     */
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }





    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLEService = ((BluetoothLEService.LocalBinder) service).getService();
            if (!mBluetoothLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finishActivity();
            }
            //startScanning();
            mBluetoothLEService.connect(mDeviceAddress);
            displayGattServices(mBluetoothLEService.getSupportedGattServices());

            //First time only time u have to press , if readvertise data then 2 times pressed , tho handler can be removed as well
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                public void run() {
                    Log.d(TAG, "onServiceConnected:gatt1 "+mNotifyCharacteristic);
                    mBluetoothLEService.readCharacteristic(mNotifyCharacteristic);

                }

            }, 2000);

        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLEService = null;
        }
    };
    private void finishActivity() {
        if(getActivity() != null) {
            getActivity().finish();
        }
    }
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive:Receiver working?? ");
            final String action = intent.getAction();
            if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                // updateConnectionState("connected");
                Log.d(TAG, "onReceive: connected");
                getActivity().invalidateOptionsMenu();

            } else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
//                updateConnectionState("disconnected");
                Log.d(TAG, "onReceive: discon");

            } else if (BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLEService.getSupportedGattServices());
            } else if (BluetoothLEService.ACTION_DATA_AVAILABLE.equals(action)) {

                Updated_data=intent.getStringExtra(BluetoothLEService.EXTRA_DATA);
             //   Toast.makeText(context, "you received "+Updated_data, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onReceive: newdata"+Updated_data);
                showdialog();

//
            }

        }
    };
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        String serviceString = "unknown service";
        String charaString = "unknown characteristic";

        for (BluetoothGattService gattService : gattServices) {

            uuid = gattService.getUuid().toString();
            if(uuid.equalsIgnoreCase("0000180F-0000-1000-8000-00805f9b34fb"))
                serviceString=Constants.BatteryService;
            else
                serviceString = null;

            if (serviceString != null) {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if(uuid.equalsIgnoreCase("00002A19-0000-1000-8000-00805f9b34fb"))
                        charaString = Constants.Batterylvl;
                    else
                        charaString=null;
                    if (charaString != null) {
                        Log.d(TAG, "displayGattServices: "+charaString);
                    }
                    mNotifyCharacteristic = gattCharacteristic;
                    Log.d(TAG, "displayGattServices: ");
                    return;
                }
            }
        }
    }




//        if (mNotifyCharacteristic != null) {
//            final int charaProp = mNotifyCharacteristic.getProperties();
//            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                mBluetoothLEService.readCharacteristic(mNotifyCharacteristic);
//
//            }
//        }
//            Toast.makeText(mBluetoothLEService, "You received" + Updated_data, Toast.LENGTH_SHORT).show();
//        Log.d(TAG, "Toast?????????????");
//    }



    private static IntentFilter GattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
     public void showdialog(){
        final AlertDialog.Builder builder=new AlertDialog.Builder(this.getActivity(),R.style.AlertDialogCustom);
               builder.setTitle("                      BLE Receiver    ")
                .setMessage("      You received : "+Updated_data+".Do You want to Re-advertise this data? ")
                .setCancelable(false)
                       .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               Intent readvertiser=new Intent(getActivity(),AdvertiserService.class);
                               readvertiser.putExtra("From Scanner to Readvertise",Updated_data);
                               startActivity(readvertiser);

                           }
                       })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        builder.create().dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
                dialog.show();

    }




}