package com.bluetoothmessenger.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluetoothmessenger.R;
import com.bluetoothmessenger.model.MessageDetails;
import com.bluetoothmessenger.utils.Constants;
import com.bluetoothmessenger.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends BaseAdapter {

    private List<MessageDetails> messageDetailsList;
    private LayoutInflater mInflater;
    private Context mContext;

    public MessagesAdapter(Context context) {
        this.mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.messageDetailsList = new ArrayList<>();
    }

    public void addMessage(MessageDetails messageDetails) {
        if (!messageDetailsList.contains(messageDetails)) {
            messageDetailsList.add(messageDetails);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return messageDetailsList.size();
    }

    @Override
    public MessageDetails getItem(int position) {
        return messageDetailsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void clear() {
        messageDetailsList.clear();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;

        MessageDetails m = messageDetailsList.get(position);
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.fragment_text_message, null);

            setHolderInflater(holder, view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        setHolderDetails(holder, m);

        return view;
    }

    private static class ViewHolder {
        TextView txtMessage, txtDate;
        ImageView imgReceiver, imgSender;
    }

    protected void setHolderInflater(ViewHolder holder, View view) {
        holder.txtMessage = (TextView) view.findViewById(R.id.txt_message);
        holder.txtDate = (TextView) view.findViewById(R.id.txt_date);
        holder.imgReceiver = (ImageView) view.findViewById(R.id.img_user_receiver);
        holder.imgSender = (ImageView) view.findViewById(R.id.img_user_sender);
    }

    protected void setHolderDetails(ViewHolder holder, MessageDetails messageDetails) {
        int msgType = messageDetails.getType();

        if (msgType == Constants.MessageType.MESSAGE_RECEIVER) {
            holder.imgReceiver.setVisibility(View.VISIBLE);
            holder.imgSender.setVisibility(View.INVISIBLE);
            holder.txtMessage.setBackgroundResource(R.drawable.rounded_theme_background);
            holder.txtMessage.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.txtDate.setGravity(Gravity.LEFT);
        } else if (msgType == Constants.MessageType.MESSAGE_SENDER) {
            holder.imgReceiver.setVisibility(View.INVISIBLE);
            holder.imgSender.setVisibility(View.VISIBLE);
            holder.txtMessage.setBackgroundResource(R.drawable.rounded_grey_background);
            holder.txtMessage.setTextColor(mContext.getResources().getColor(R.color.gray_dark_shade2));
            holder.txtDate.setGravity(Gravity.RIGHT);
        }

        int dp = (int) Utils.pxFromDp(mContext, 4);
        holder.txtMessage.setPadding(dp ,dp , dp, dp);
        holder.txtMessage.setText(messageDetails.getMessage());
        holder.txtDate.setText(Utils.formatDate(messageDetails.getDateCreated()));
    }

}
