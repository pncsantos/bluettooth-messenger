package com.bluetoothmessenger.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluetoothmessenger.R;
import com.bluetoothmessenger.utils.Utils;

import java.util.ArrayList;

public class LeDeviceListAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater mInflator;
    private Context mContext;

    public LeDeviceListAdapter(Context context) {
        super();
        mLeDevices = new ArrayList<>();
        mContext = context;
        mInflator = LayoutInflater.from(mContext);
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
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.txtStatus = (TextView) view.findViewById(R.id.txt_status);

            viewHolder.imgDevice = (ImageView) view.findViewById(R.id.img_device);
            viewHolder.imgLink = (ImageView) view.findViewById(R.id.img_link);
            viewHolder.layoutMsg = (LinearLayout) view.findViewById(R.id.layout_msg);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(deviceName);
            viewHolder.deviceAddress.setText(device.getAddress());
        } else {
            viewHolder.deviceName.setText(mContext.getString(R.string.txt_unknown_device));
            viewHolder.deviceAddress.setText(device.getAddress());
        }

        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            viewHolder.txtStatus.setText(mContext.getString(R.string.txt_status_not_paired));
            viewHolder.layoutMsg.setVisibility(View.GONE);
            Utils.changeImageColor(viewHolder.imgLink, mContext.getResources(), R.color.gray_shade2);
        } else if  (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            viewHolder.txtStatus.setText(mContext.getString(R.string.txt_status_paired));
            viewHolder.layoutMsg.setVisibility(View.VISIBLE);
            Utils.changeImageColor(viewHolder.imgLink,  mContext.getResources(), R.color.teal);
        } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
            viewHolder.txtStatus.setText(mContext.getString(R.string.txt_status_pairing));
            viewHolder.layoutMsg.setVisibility(View.GONE);
            Utils.changeImageColor(viewHolder.imgLink, mContext.getResources(), R.color.gray_shade2);
        }

        return view;
    }

    private static class ViewHolder {
        TextView deviceName, deviceAddress, txtStatus;
        ImageView imgDevice, imgLink;
        LinearLayout layoutMsg;
    }
}
