package com.bluetoothmessenger.view.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothmessenger.R;
import com.bluetoothmessenger.utils.Constants;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private BluetoothAdapter mBluetoothAdapter;

    private EditText txtDeviceName;

    private TextView btnRegister;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    /* Initialize variables and set listeners */
    private void initView() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        btnRegister = (TextView) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(this);

        txtDeviceName = (EditText) findViewById(R.id.txt_device_name);
        txtDeviceName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateRegisterButton(s.toString());
            }
        });
    }

    private void updateRegisterButton(String deviceName) {
        int charLength = deviceName.length();
        if (charLength > 0) {
            btnRegister.setAlpha(1f);
            btnRegister.setEnabled(true);
        } else {
            btnRegister.setAlpha(.5f);
            btnRegister.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_register) {
            onRegisterClicked();
        }
    }

    /* Checks if the devices support bluetooth and if it is enabled. */
    private void onRegisterClicked() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getApplicationContext(), getString(R.string.txt_device_not_supported), Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                showPermissionDialog();
            } else {
                mBluetoothAdapter.setName(txtDeviceName.getText().toString());
                savePreference();
                showHomeView();
            }
        }
    }

    /* Opens dialog to ask permission to allow the app to turn on its bluetooth */
    private void showPermissionDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
        alertDialog.setTitle(getString(R.string.msg_title_turn_on_bluetooth));
        alertDialog.setMessage(getString(R.string.msg_turn_on_bluetooth));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.action_allow),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothAdapter.setName(txtDeviceName.getText().toString());
                        mBluetoothAdapter.enable();
                        savePreference();
                        showHomeView();
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
    private void showHomeView() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
        finish();
    }

    /* Store default preferences  */
    private void savePreference() {
        preferences.edit().putBoolean(Constants.REGISTERED_DEVICE_NAME, true).commit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}