package com.bluetoothmessenger.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bluetoothmessenger.R;
import com.bluetoothmessenger.adapters.LeDeviceListAdapter;
import com.bluetoothmessenger.fragments.DefaultDialogFragment;
import com.bluetoothmessenger.utils.Constants;
import com.bluetoothmessenger.utils.Utils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SearchDeviceActivity extends BaseActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean mScanning;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private ListView listViewDevices;

    private RelativeLayout layoutProgressBar;
    private RelativeLayout layoutActionButton;
    private RelativeLayout layoutEmptyDevice;
    private RelativeLayout layoutHeader;
    private RelativeLayout layoutDevicesCount;

    private LinearLayout layoutSettings;
    private boolean isSettingsVisible;

    private ImageView icSettings;

    private TextView txtAction;
    private TextView txtDeviceName;
    private TextView txtDeviceCount, txtHeaderMessage;

    private DefaultDialogFragment fragment;

    private int REQUEST_BLUETOOTH = 1;

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private BluetoothGatt mGatt;

    BluetoothProfile.ServiceListener mProfileServiceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

        initActionBar();
        init();
    }

    /* Initialize actionbar */
    private void initActionBar() {
        ActionBar ab = getActionBar();
        View customView = ab.getCustomView();

        layoutActionButton = (RelativeLayout) customView.findViewById(R.id.btn_scan);
        layoutActionButton.setOnClickListener(this);

        icSettings = (ImageView) customView.findViewById(R.id.ic_settings);
        icSettings.setOnClickListener(this);

        txtDeviceName = (TextView) customView.findViewById(R.id.txt_device_name);
        txtAction = (TextView) customView.findViewById(R.id.txt_action);
    }

    /* Initialize variables and set listeners */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void init() {
        mHandler = new Handler();

        layoutProgressBar = (RelativeLayout) findViewById(R.id.layout_progressBar);
        layoutEmptyDevice = (RelativeLayout) findViewById(R.id.layout_empty_device);
        layoutHeader = (RelativeLayout) findViewById(R.id.layout_header);
        layoutDevicesCount = (RelativeLayout) findViewById(R.id.layout_devices_count);
        layoutSettings = (LinearLayout) findViewById(R.id.layout_settings);

        findViewById(R.id.txt_visible_device).setOnClickListener(this);
        findViewById(R.id.txt_rename_device).setOnClickListener(this);

        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = btManager.getAdapter();

        txtDeviceName.setText(getString(R.string.txt_device_name) + " " + mBluetoothAdapter.getName());

        txtDeviceCount = (TextView) findViewById(R.id.txt_device_count);
        txtHeaderMessage = (TextView) findViewById(R.id.txt_header_message);

        listViewDevices = (ListView) findViewById(R.id.listView_devices);
        listViewDevices.setOnItemClickListener(this);

        mLeDeviceListAdapter = new LeDeviceListAdapter(getApplicationContext());
        listViewDevices.setAdapter(mLeDeviceListAdapter);

        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
            return;
        } else {
            scanDeviceTimer();
            runScanningDevice();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (!mBluetoothAdapter.isEnabled()) {
//            if (!mBluetoothAdapter.isEnabled()) {
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
//            }
//        } else {
//            // Initializes list view adapter.
//            mLeDeviceListAdapter = new LeDeviceListAdapter(getApplicationContext());
//            scanDeviceTimer();
//            runScanningDevice();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
//        if (mLeDeviceListAdapter != null) {
//            mLeDeviceListAdapter.clear();
//        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_scan) {
            if (!mBluetoothAdapter.isEnabled()) {
                openBlueToothEnableRequest();
            } else {
                if (!mScanning) {
                    scanLeDevice(true);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                } else {
                    scanLeDevice(false);
                }
            }
        } else if (id == R.id.ic_settings) {
            if (!isSettingsVisible) {
                layoutSettings.setVisibility(View.VISIBLE);
            } else {
                layoutSettings.setVisibility(View.GONE);
            }

            isSettingsVisible = !isSettingsVisible;
        } else if (id == R.id.txt_visible_device) {
            isSettingsVisible = false;
            layoutSettings.setVisibility(View.GONE);
            openDiscoverableIntent();
        } else if (id == R.id.txt_rename_device) {
            isSettingsVisible = false;
            layoutSettings.setVisibility(View.GONE);
            openDialogRenameDevice();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);

        if (device == null) {
            return;
        }

        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }

        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            pairDevice(device);
        } else {
            openMessageIntent(device);
        }
    }

    /*
   * Pair device to another device
   *
   * @param device: needs a bluetooth device to pair with
   *
   * */
    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Scan devices near to you */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanDevices() {
        int apiVersion = Build.VERSION.SDK_INT;
//  TODO for normal bluetooth
        mBluetoothAdapter.startDiscovery();

        if (apiVersion > Build.VERSION_CODES.KITKAT) {
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

            // scan for devices
            scanner.startScan(mScanCallback);
        } else {
            UUID[] uuidList = {MY_UUID_SECURE, MY_UUID_INSECURE};
            mBluetoothAdapter.startLeScan(uuidList ,mLeScanCallback);
        }
    }

    /* Run and stop device */
    private void scanLeDevice(boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            scanDeviceTimer();
            runScanningDevice();
        } else {
            stopScanningDevice();
        }
        invalidateOptionsMenu();
    }

    /* Set timer for scanning available devices within 10 seconds */
    private void scanDeviceTimer() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanningDevice();
                invalidateOptionsMenu();
            }
        }, SCAN_PERIOD);
    }

    /* Set to Scan View when searching a device */
    private void runScanningDevice() {
        mScanning = true;

        layoutHeader.setVisibility(View.VISIBLE);

        txtAction.setText(getString(R.string.action_stop));
        layoutProgressBar.setVisibility(View.VISIBLE);
        layoutDevicesCount.setVisibility(View.GONE);

        scanDevices();
    }

    /*
     * Set Content View to show when scanning devices is finished
     *
     * Shows an empty state if there was no device found.
     *
     * Shows list of devices when there are devices available.
     *
     * */
    private void stopScanningDevice() {
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        layoutProgressBar.setVisibility(View.GONE);
        txtAction.setText(getString(R.string.action_scan));

        int deviceCount = mLeDeviceListAdapter.getCount();

        if (mLeDeviceListAdapter == null || deviceCount == 0) {
            listViewDevices.setVisibility(View.GONE);
            layoutHeader.setVisibility(View.GONE);
            layoutEmptyDevice.setVisibility(View.VISIBLE);
            ImageView img = (ImageView) findViewById(R.id.img_no_device);
            Utils.changeImageColor(img, getResources(), R.color.theme_color);
        } else {
            layoutEmptyDevice.setVisibility(View.GONE);
            layoutProgressBar.setVisibility(View.GONE);
            listViewDevices.setVisibility(View.VISIBLE);
            layoutDevicesCount.setVisibility(View.VISIBLE);
            if (deviceCount == 1) {
                txtHeaderMessage.setText(getString(R.string.txt_device_detected));
            } else {
                txtHeaderMessage.setText(getString(R.string.txt_devices_detected));
            }
            txtDeviceCount.setText(deviceCount + "");
        }
    }

    /* Updates count of devices near to you */
    private void updateDeviceCount() {
        int deviceCount = mLeDeviceListAdapter.getCount();

        layoutEmptyDevice.setVisibility(View.GONE);
        listViewDevices.setVisibility(View.VISIBLE);

        if (deviceCount == 1) {
            txtHeaderMessage.setText(getString(R.string.txt_device_detected));
        } else {
            txtHeaderMessage.setText(getString(R.string.txt_devices_detected));
        }
        txtDeviceCount.setText(deviceCount + "");
    }

    /* Callback method for adding LE devices at the list */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                            updateDeviceCount();
                        }
                    });
                }
            };

    /* Callback method for adding devices at the list */
    private ScanCallback mScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    super.onScanResult(callbackType, result);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(result.getDevice());
                            mLeDeviceListAdapter.notifyDataSetChanged();
                            updateDeviceCount();
                        }
                    });
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult sr : results) {
                        Log.i("ScanResult - Results", sr.toString());
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.e("Scan Failed", "Error Code: " + errorCode);
                }
            };

    /* Receives the incoming bluetooth devices that was found */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String dName = device.getName();
                String dAddress = device.getAddress();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    mLeDeviceListAdapter.addDevice(device);
                    updateDeviceCount();
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    // detect device connected
                } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                        // detect device paired
                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                        // detect device disconnected
                    }
                    mLeDeviceListAdapter.notifyDataSetChanged();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    // detect device finished scanning
                }
            } catch (Exception e) {

            }
        }
    };

    /*
     * Opens intent for messaging a device
     *
     * @param device: certain device to message
     *
     * */
    private void openMessageIntent(BluetoothDevice device) {
        Intent intent = new Intent(SearchDeviceActivity.this, MessageContentActivity.class);
        intent.putExtra(Constants.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(Constants.EXTRAS_DEVICE_ADDRESS, device.getAddress());

        startActivity(intent);
    }

    /* Opens intent to ask user if the device can be able be visible to other devices for certain time */
    private void openDiscoverableIntent() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    /* Opens dialog to rename device */
    protected void openDialogRenameDevice() {
        fragment = new DefaultDialogFragment();
        fragment.show(getSupportFragmentManager(), getString(R.string.txt_rename_device));

        fragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                String deviceName = mBluetoothAdapter.getName();

                if (!deviceName.equals(txtDeviceName.getText().toString())) {
                    txtDeviceName.setText(getString(R.string.txt_device_name) + " " + deviceName);
                }
            }
        });
    }

    /* Opens dialog to enable bluetooth device visibilty */
    private void openBlueToothEnableRequest() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
    }

}
