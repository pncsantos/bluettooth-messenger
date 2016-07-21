package com.bluetoothmessenger.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.bluetoothmessenger.R;

public class DefaultDialogFragment extends DialogFragment implements View.OnClickListener {

    private DialogInterface.OnDismissListener onDismissListener;

    private  BluetoothAdapter mBluetoothAdapter;
    private EditText editTxtDeviceName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(R.style.AlertDialog, R.style.AlertDialogTransparent);
        setShowsDialog(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_rename_device, null);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        view.findViewById(R.id.txt_apply).setOnClickListener(this);
        view.findViewById(R.id.txt_cancel).setOnClickListener(this);

        editTxtDeviceName = (EditText) view.findViewById(R.id.editTxt_device_name);
        editTxtDeviceName.setHint(mBluetoothAdapter.getName());

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.txt_apply) {
            mBluetoothAdapter.setName(editTxtDeviceName.getText().toString());
            dismiss();
        } else if (id == R.id.txt_cancel) {
            dismiss();
        }
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }
}
