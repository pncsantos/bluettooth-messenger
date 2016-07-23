package com.bluetoothmessenger.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bluetoothmessenger.IDeviceListener;
import com.bluetoothmessenger.R;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater mInflator;
    private Context mContext;

    private IDeviceListener deviceListener = null;

    public DeviceListAdapter(Context context, IDeviceListener listener) {
        super();
        mLeDevices = new ArrayList<>();
        mContext = context;
        mInflator = LayoutInflater.from(mContext);
        deviceListener = listener;
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device);
            notifyDataSetChanged();
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.txtStatus = (TextView) view.findViewById(R.id.txt_status);
            viewHolder.txtLinkStatus = (TextView) view.findViewById(R.id.txt_link_status);
            viewHolder.txtMessage = (TextView) view.findViewById(R.id.txt_message);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(position);
        String deviceName = device.getName();

        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(deviceName);
        } else {
            viewHolder.deviceName.setText(mContext.getString(R.string.txt_unknown_device));
        }

        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            viewHolder.txtMessage.setAlpha(.5f);
            viewHolder.txtMessage.setEnabled(false);
            viewHolder.txtLinkStatus.setAlpha(1f);
            viewHolder.txtLinkStatus.setEnabled(true);
            viewHolder.txtStatus.setText(mContext.getString(R.string.txt_status_not_paired));
            viewHolder.txtLinkStatus.setText(mContext.getString(R.string.txt_status_link));
        } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            viewHolder.txtMessage.setAlpha(1f);
            viewHolder.txtMessage.setEnabled(true);
            viewHolder.txtLinkStatus.setAlpha(1f);
            viewHolder.txtLinkStatus.setEnabled(true);
            viewHolder.txtStatus.setText(mContext.getString(R.string.txt_status_paired));
            viewHolder.txtLinkStatus.setText(mContext.getString(R.string.txt_status_unlink));
        } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
            viewHolder.txtMessage.setAlpha(.5f);
            viewHolder.txtMessage.setEnabled(false);
            viewHolder.txtLinkStatus.setAlpha(.5f);
            viewHolder.txtLinkStatus.setEnabled(false);
            viewHolder.txtStatus.setText(mContext.getString(R.string.txt_status_pairing));
        }

        viewHolder.txtLinkStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceListener != null) {
                    deviceListener.onLinkClick(position);
                }
            }
        });

        viewHolder.txtMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceListener != null) {
                    deviceListener.onMessageClick(position);
                }
            }
        });

        return view;
    }

    private static class ViewHolder {
        TextView deviceName;
        TextView txtStatus;
        TextView txtLinkStatus;
        TextView txtMessage;
    }
}