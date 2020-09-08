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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Holds and displays {@link ScanResult}s, used by {@link ScannerFragment}.
 */
public class ScanResultAdapter extends BaseAdapter {
    public  ArrayList<BluetoothDevice> mLeDevices;

  //  public ArrayList<ScanResult> mArrayList;

    private Context mContext;

    private LayoutInflater mInflater;

    public ScanResultAdapter(Context context, LayoutInflater inflater) {
        super();
        mContext = context;
        mInflater = inflater;
        mLeDevices = new ArrayList<BluetoothDevice>();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mLeDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // Reuse an old view if we can, otherwise create a new one.
        if (view == null) {
            view = mInflater.inflate(R.layout.listitem_scanresult,parent,false);//TODO listview
        }

        TextView deviceNameView = (TextView) view.findViewById(R.id.device_name);
        TextView deviceAddressView = (TextView) view.findViewById(R.id.device_address);
        TextView lastSeenView = (TextView) view.findViewById(R.id.last_seen);

        BluetoothDevice device = mLeDevices.get(position);

        String name = device.getName();
        if (name != null && name.length() > 0) {
            name = device.getName();
        }
        else
            name="unknown device";

        deviceNameView.setText(name);
        deviceAddressView.setText(device.getAddress());
        lastSeenView.setText("bla bla");

        return view;
    }


    /**
     * Search the adapter for an existing device address and return it, otherwise return -1.
     */
    public int getPosition(String address) {
        int position = -1;
        for (int i = 0; i < mLeDevices.size(); i++) {
            if (mLeDevices.get(i).getAddress().equals(address)) {    //TODO check!!!!
                position = i;
                break;
            }
        }
        return position;
    }



    /**
     * Add a ScanResult item to the adapter if a result from that device isn't already present.
     * Otherwise updates the existing position with the new ScanResult.
     */
//    public void add(ScanResult scanResult) {
//
//        int existingPosition = getPosition(scanResult.getDevice().getAddress());
//
//        if (existingPosition >= 0) {
//            // Device is already in list, update its record.
//            mLeDevices.set(existingPosition, scanResult);
//        } else {
//            // Add new Device's ScanResult to list.
//            mLeDevices.add(scanResult);
//
//
//        }
    public void addDevice(BluetoothDevice device) {
        int existingPosition = getPosition(device.getAddress());
        if (existingPosition >= 0)
            mLeDevices.set(existingPosition, device);
//        if(!mLeDevices.contains(device)) {
//            mLeDevices.add(device);
            else {
//            // Add new Device's ScanResult to list.
          mLeDevices.add(device);

        }
    }
    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }






    /**
     * Clear out the adapter.
     */
    public void clear()
    {
        mLeDevices.clear();
    }

    /**
     * Takes in a number of nanoseconds and returns a human-readable string giving a vague
     * description of how long ago that was.
     */
    public static String getTimeSinceString(Context context, long timeNanoseconds) {
        String lastSeenText = context.getResources().getString(R.string.last_seen) + " ";

        long timeSince = SystemClock.elapsedRealtimeNanos() - timeNanoseconds;
        long secondsSince = TimeUnit.SECONDS.convert(timeSince, TimeUnit.NANOSECONDS);

        if (secondsSince < 5) {
            lastSeenText += context.getResources().getString(R.string.just_now);
        } else if (secondsSince < 60) {
            lastSeenText += secondsSince + " " + context.getResources()
                    .getString(R.string.seconds_ago);
        } else {
            long minutesSince = TimeUnit.MINUTES.convert(secondsSince, TimeUnit.SECONDS);
            if (minutesSince < 60) {
                if (minutesSince == 1) {
                    lastSeenText += minutesSince + " " + context.getResources()
                            .getString(R.string.minute_ago);
                } else {
                    lastSeenText += minutesSince + " " + context.getResources()
                            .getString(R.string.minutes_ago);
                }
            } else {
                long hoursSince = TimeUnit.HOURS.convert(minutesSince, TimeUnit.MINUTES);
                if (hoursSince == 1) {
                    lastSeenText += hoursSince + " " + context.getResources()
                            .getString(R.string.hour_ago);
                } else {
                    lastSeenText += hoursSince + " " + context.getResources()
                            .getString(R.string.hours_ago);
                }
            }
        }

        return lastSeenText;
    }
}
