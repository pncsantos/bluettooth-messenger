package com.bluetoothmessenger.activities;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothmessenger.R;
import com.bluetoothmessenger.adapters.MessagesAdapter;
import com.bluetoothmessenger.model.MessageDetails;
import com.bluetoothmessenger.utils.BluetoothChatService;
import com.bluetoothmessenger.utils.Constants;
import com.bluetoothmessenger.utils.Utils;

import java.util.Calendar;
import java.util.List;

public class MessageContentActivity extends BaseActionBarActivity implements View.OnClickListener {

    private String mDeviceName;
    private String mDeviceAddress;

    private ListView listViewMessages;
    private MessagesAdapter messagesAdapter;

    private TextView txtStatus;
    private TextView txtUserName;
    private EditText editTxtMessage;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messges);

        initActionBar();
        init();
    }

    /* Initialize variables and set listeners */
    private void init() {
        findViewById(R.id.img_send).setOnClickListener(this);

        listViewMessages = (ListView) findViewById(R.id.listView_messages);
        editTxtMessage = (EditText) findViewById(R.id.txt_message);

        messagesAdapter = new MessagesAdapter(getApplicationContext());
        listViewMessages.setAdapter(messagesAdapter);

        mChatService = new BluetoothChatService(this, mHandler);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent = getIntent();
        connectDevice(intent, false);

        txtUserName.setText(mDeviceName);
    }

    /* Initialize actionbar to custom and set listeners */
    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(R.layout.actionbar_message);
        actionBar.setDisplayShowCustomEnabled(true);

        View ab = actionBar.getCustomView();
        ImageView imgBack = (ImageView) ab.findViewById(R.id.img_back);
        Utils.changeImageColor(imgBack, getResources(), R.color.white);
        imgBack.setOnClickListener(this);

        txtUserName = (TextView) ab.findViewById(R.id.txt_receiver_user_name);
        txtStatus = (TextView) ab.findViewById(R.id.txt_status);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.img_back) {
            finish();
        } else if (id == R.id.img_send) {
            sendMessage(editTxtMessage.getText().toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            txtStatus.setText(getString(R.string.txt_status_online));
                            messagesAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            txtStatus.setText(getString(R.string.txt_status_connecting));
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            txtStatus.setText(getString(R.string.txt_status_connecting));
                        case BluetoothChatService.STATE_NONE:
                            txtStatus.setText(getString(R.string.txt_status_offline));
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    messagesAdapter.addMessage(createSenderDetails(writeMessage , Constants.MessageType.MESSAGE_SENDER));
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    messagesAdapter.addMessage(createSenderDetails(readMessage , Constants.MessageType.MESSAGE_RECEIVER));
                    break;
            }
        }
    };

    /* Clears typing message */
    private void clearMessage() {
        hideKeyboard();

        editTxtMessage.setText("");
        editTxtMessage.clearFocus();
    }

    /*
    * Create MessageDetails object that will added to the list of messages
    *
    * @param msg : the message to seen at the list
    * @param msgType : will detect if the message is created by sender or receiver
    *
    */
    private MessageDetails createSenderDetails(String msg , int msgType) {
        MessageDetails messageDetails = new MessageDetails();
        messageDetails.setMessage(msg);
        messageDetails.setType(msgType);

        Calendar cal = Calendar.getInstance();
        messageDetails.setDateCreated(cal.getTimeInMillis());
        return messageDetails;
    }

    /*
    * hides active keyboard
    * */
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /*
     * Send message to paired device
     *
     * @param message : text message that will be send to paired device
     *
     * */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), "Device is not yet connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            clearMessage();
        }
    }

    /*
     * Pair device by using mac address
     *
     * @param data : intent of object
     * @param secure : type of connection
     *
     * */
    private void connectDevice(Intent data, boolean secure) {
        mDeviceName = data.getStringExtra(Constants.EXTRAS_DEVICE_NAME);
        mDeviceAddress = data.getStringExtra(Constants.EXTRAS_DEVICE_ADDRESS);

        // Get the device MAC address
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

}
