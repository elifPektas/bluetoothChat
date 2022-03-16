package com.example.bluetoothchat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    private ListView listPairedDevices, listAvailableDevices;
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private ProgressBar progressScanDevices;

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_ADVERTISE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.ACCESS_COARSE_LOCATION"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        context = this;

        init();
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_ADVERTISE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.ACCESS_COARSE_LOCATION"})
    private void init() {
        listPairedDevices = findViewById(R.id.list_paired_devices);
        listAvailableDevices = findViewById(R.id.list_available_devices);
        progressScanDevices = findViewById(R.id.progress_scan_devices);

        adapterPairedDevices = new ArrayAdapter<>(context, R.layout.device_list_item);
        adapterAvailableDevices = new ArrayAdapter<>(context, R.layout.device_list_item);

        listPairedDevices.setAdapter(adapterPairedDevices);
        listAvailableDevices.setAdapter(adapterAvailableDevices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapterPairedDevices.add(device.getName() + "\n" + device.getAddress());
            }
        }

        IntentFilter intentFilterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDeviceListener, intentFilterFound);
        IntentFilter intentFilterDiscoveryFinished = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDeviceListener, intentFilterDiscoveryFinished);

        listPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @RequiresPermission(allOf = {
                    "android.permission.BLUETOOTH_CONNECT",
                    "android.permission.BLUETOOTH_ADVERTISE",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.BLUETOOTH_SCAN",
                    "android.permission.ACCESS_COARSE_LOCATION"})
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();

                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                Log.d("Address", address);

                Intent intent = new Intent();
                intent.putExtra("deviceAddress", address);

                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }

    BroadcastReceiver bluetoothDeviceListener = new BroadcastReceiver() {
        @RequiresPermission(allOf = {
                "android.permission.BLUETOOTH_CONNECT",
                "android.permission.BLUETOOTH_ADVERTISE",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.BLUETOOTH_SCAN",
                "android.permission.ACCESS_COARSE_LOCATION"})
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
                }
            } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                progressScanDevices.setVisibility(View.GONE);
                if(adapterAvailableDevices.getCount() == 0) {
                    Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Click on the device to start the chat", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_ADVERTISE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.ACCESS_COARSE_LOCATION"})
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan_devices:
                scanDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_ADVERTISE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.ACCESS_COARSE_LOCATION"})
    private void scanDevices() {
        progressScanDevices.setVisibility(View.VISIBLE);
        adapterAvailableDevices.clear();
        Toast.makeText(context, "Scan started", Toast.LENGTH_SHORT).show();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothDeviceListener != null) {
            unregisterReceiver(bluetoothDeviceListener);
        }
    }
}