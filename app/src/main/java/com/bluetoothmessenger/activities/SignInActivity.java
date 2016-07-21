package com.bluetoothmessenger.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothmessenger.R;
import com.bluetoothmessenger.utils.Constants;

public class SignInActivity extends FragmentActivity implements View.OnClickListener {

    private BluetoothAdapter mBluetoothAdapter;

    private EditText txtUserName;

    private TextView btnSend;

    private CheckBox checkBoxStatus;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        init();
    }

    /* Initialize variables and set listeners */
    private void init() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        RelativeLayout layout_signin = (RelativeLayout) findViewById(R.id.layout_signin);
        layout_signin.requestFocus();

        btnSend = (TextView) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);

        checkBoxStatus = (CheckBox) findViewById(R.id.checkbox_status);

        txtUserName = (EditText) findViewById(R.id.txt_user_name);
        txtUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int count = s.length();

                if (count == 0) {
                    btnSend.setEnabled(false);
                    btnSend.setAlpha(.25f);
                } else {
                    btnSend.setEnabled(true);
                    btnSend.setAlpha(1f);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_send) {
            checkIsBluetoothEnabled();
        }
    }

    /* Checks if the devices support bluetooth and if it is enabled. */
    private void checkIsBluetoothEnabled() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getApplicationContext(), getString(R.string.txt_device_not_supported), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                openDialog();
            } else {
                mBluetoothAdapter.setName(txtUserName.getText().toString());
                commitDeviceName();
                openSearchDeviceIntent();
            }
        }
    }

    /* Opens dialog to ask permission to allow the app to turn on its bluetooth */
    private void openDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this).create();
        alertDialog.setTitle(getString(R.string.msg_title_turn_on_bluetooth));
        alertDialog.setMessage(getString(R.string.msg_turn_on_bluetooth));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_allow),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothAdapter.setName(txtUserName.getText().toString());
                        mBluetoothAdapter.enable();
                        commitDeviceName();
                        openSearchDeviceIntent();
                        dialog.dismiss();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.action_deny),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /* Opens intent to for searching devices */
    private void openSearchDeviceIntent() {
        Intent i = new Intent(SignInActivity.this, SearchDeviceActivity.class);
        startActivity(i);
        finish();
    }

    /* Store default preferences  */
    private void commitDeviceName() {
        prefs.edit().putBoolean(Constants.REGISTERED_USER, true).commit();
        prefs.edit().putBoolean(Constants.BLUETOOTH_ALWAYS_ON, checkBoxStatus.isChecked()).commit();
    }
}
