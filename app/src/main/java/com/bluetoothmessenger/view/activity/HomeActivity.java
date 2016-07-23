package com.bluetoothmessenger.view.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluetoothmessenger.IDeviceListener;
import com.bluetoothmessenger.R;
import com.bluetoothmessenger.adapters.DeviceListAdapter;
import com.bluetoothmessenger.fragments.RenameDialogFragment;
import com.bluetoothmessenger.utils.Constants;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_BLUETOOTH = 1001;
    // Stops scanning after 5 seconds.
    private static final long SCAN_PERIOD = 5000;

    private BluetoothAdapter mBluetoothAdapter;

    private DeviceListAdapter mDeviceListAdapter;

    private Handler mHandler;

    private ListView listViewDevices;

    private ImageView imgSearch;
    private TextView txtDeviceName;
    private TextView txtNotification;

    private TextView txtEmptyState;

    private RenameDialogFragment fragment;

    private ProgressDialog mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_device);

        initView();
        initListeners();
        initializeState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemConnection = menu.findItem(R.id.item_connection);
        MenuItem itemRename = menu.findItem(R.id.item_rename_device);
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            itemConnection.setTitle(getString(R.string.txt_menu_connect));
            itemRename.setVisible(false);
        } else {
            itemConnection.setTitle(getString(R.string.txt_menu_disconnect));
            itemRename.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_rename_device) {
            showRenameDeviceDialog();
        } else if (id == R.id.item_connection) {
            onConnectionClicked();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onConnectionClicked() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            showPermissionRequest();
        } else {
            txtEmptyState.setVisibility(View.GONE);
            txtDeviceName.setVisibility(View.GONE);
            listViewDevices.setVisibility(View.GONE);
            listViewDevices.setAdapter(null);

            txtNotification.setVisibility(View.VISIBLE);
            txtNotification.setBackgroundColor(getResources().getColor(R.color.theme_orange_text));
            txtNotification.setText(getString(R.string.txt_not_connected));

            mBluetoothAdapter.disable();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        if (mLoading.isShowing()) {
            stopScanningDevice();
        }
    }

    @Override
    public void onDestroy() {
        if (mLoading.isShowing()) {
            stopScanningDevice();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.img_search) {
            onSearchClicked();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                txtNotification.setVisibility(View.GONE);
                txtDeviceName.setVisibility(View.VISIBLE);
                txtDeviceName.setText(getString(R.string.txt_device_name) + " " + mBluetoothAdapter.getName());
                startScanningDevice();
            } else {
                txtDeviceName.setVisibility(View.GONE);
                txtNotification.setVisibility(View.VISIBLE);
            }
        }
    }

    /* Initialize actionbar */
    private void initActionBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        txtDeviceName = (TextView) mToolbar.findViewById(R.id.txt_device_name);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initView() {
        initActionBar();

        listViewDevices = (ListView) findViewById(R.id.listView_devices);
        txtNotification = (TextView) findViewById(R.id.txt_notification);
        txtEmptyState = (TextView) findViewById(R.id.txt_empty_state);
        imgSearch = (ImageView) findViewById(R.id.img_search);

        mLoading = new ProgressDialog(this, R.style.LoadingDialog);
        mLoading.setCancelable(false);

        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = btManager.getAdapter();
    }

    private void initializeState() {
        if (mBluetoothAdapter.isEnabled()) {
            txtNotification.setVisibility(View.GONE);
            txtDeviceName.setVisibility(View.VISIBLE);
            txtDeviceName.setText(getString(R.string.txt_device_name) + " " + mBluetoothAdapter.getName());
        }

        mHandler = new Handler();

        mDeviceListAdapter = new DeviceListAdapter(getApplicationContext(), new IDeviceListener() {
            @Override
            public void onLinkClick(int position) {
                BluetoothDevice device = mDeviceListAdapter.getDevice(position);
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    updateDeviceStatus(device, Constants.BluetoothBondType.CREATE_BOND);
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    updateDeviceStatus(device, Constants.BluetoothBondType.REMOVE_BOND);
                }
            }

            @Override
            public void onMessageClick(int position) {
                BluetoothDevice device = mDeviceListAdapter.getDevice(position);
                showMessageView(device);
            }
        });

        listViewDevices.setAdapter(mDeviceListAdapter);
    }

    private void initListeners() {
        imgSearch.setOnClickListener(this);
    }

    private void onSearchClicked() {
        if (!mBluetoothAdapter.isEnabled()) {
            showPermissionRequest();
        } else {
            startScanningDevice();
        }
    }

    /*
    * Pair device to another device
    * @param device: needs a bluetooth device to pair with
    * */
    private void updateDeviceStatus(BluetoothDevice device, String status) {
        try {
            Method method = device.getClass().getMethod(status, (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Scan devices near to you */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanDevices() {
        mLoading.show();

        int apiVersion = Build.VERSION.SDK_INT;
        //  TODO for normal bluetooth
        mBluetoothAdapter.startDiscovery();

        if (apiVersion > Build.VERSION_CODES.KITKAT) {
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
            // scan for devices
            scanner.startScan(mScanCallback);
        } else {
            UUID[] uuidList = {Constants.MY_UUID_SECURE, Constants.MY_UUID_INSECURE};
            mBluetoothAdapter.startLeScan(uuidList, mLeScanCallback);
        }
    }

    /* Set timer for scanning available devices within 10 seconds */
    private void setupScanTimer() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScanningDevice();
                invalidateOptionsMenu();
            }
        }, SCAN_PERIOD);
    }

    /* Set to Scan View when searching a device */
    private void startScanningDevice() {
        mDeviceListAdapter.clear();
        setupScanTimer();
        scanDevices();
    }

    /*
     * Set Content View to show when scanning devices is finished
     * Shows an empty state if there was no device found.
     * Shows list of devices when there are devices available.
     * */
    private void stopScanningDevice() {
        mLoading.dismiss();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        int deviceCount = mDeviceListAdapter.getCount();

        if (mDeviceListAdapter == null || deviceCount == 0) {
            txtEmptyState.setVisibility(View.VISIBLE);
            listViewDevices.setVisibility(View.GONE);
            txtNotification.setVisibility(View.GONE);
        } else {
            listViewDevices.setVisibility(View.VISIBLE);
            txtNotification.setVisibility(View.VISIBLE);
            txtEmptyState.setVisibility(View.GONE);
        }
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
                            mDeviceListAdapter.addDevice(device);
                            mDeviceListAdapter.notifyDataSetChanged();
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
                            mDeviceListAdapter.addDevice(result.getDevice());
                            mDeviceListAdapter.notifyDataSetChanged();
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
                    mDeviceListAdapter.addDevice(device);
                    mDeviceListAdapter.notifyDataSetChanged();
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
                    mDeviceListAdapter.notifyDataSetChanged();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    // detect device finished scanning
                }
            } catch (Exception e) {

            }
        }
    };

    /* Updates count of devices near to you */
    private void updateDeviceCount() {
        txtNotification.setVisibility(View.VISIBLE);
        txtNotification.setBackgroundColor(getResources().getColor(R.color.lime_dark_primary));

        int deviceCount = mDeviceListAdapter.getCount();

        StringBuilder notificationMessage = new StringBuilder();
        notificationMessage.append(deviceCount);

        if (deviceCount == 1) {
            notificationMessage.append(" device found.");
        } else {
            notificationMessage.append(" devices found.");
        }

        txtNotification.setText(notificationMessage.toString());
    }

    /*
     * Opens intent for messaging a device
     * @param device: certain device to message
     * */
    private void showMessageView(BluetoothDevice device) {
        Intent intent = new Intent(HomeActivity.this, MessengerActivity.class);
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
    protected void showRenameDeviceDialog() {
        fragment = new RenameDialogFragment();
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

    /* Opens dialog to enable bluetooth device visibility */
    private void showPermissionRequest() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
    }
}