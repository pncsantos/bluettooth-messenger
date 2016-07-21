package com.bluetoothmessenger.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageDetails implements Parcelable {

    public MessageDetails() {
        //Required since we we have a private constructor
    }

    public long dateCreated;
    public String message;
    public int type;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("dateCreated: ").append(dateCreated);
        builder.append(", message: ").append(message);
        builder.append(", type: ").append(type);
        return builder.toString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(dateCreated);
        dest.writeInt(type);
        dest.writeString(message);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<MessageDetails> CREATOR = new Parcelable.Creator<MessageDetails>() {
        public MessageDetails createFromParcel(Parcel in) {
            return new MessageDetails(in);
        }

        public MessageDetails[] newArray(int size) {
            return new MessageDetails[size];
        }
    };

    public MessageDetails(Parcel in) {
        dateCreated = in.readLong();
        type = in.readInt();
        message = in.readString();
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
