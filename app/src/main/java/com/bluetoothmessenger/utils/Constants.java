package com.bluetoothmessenger.utils;

public class Constants {

    // EXTRAS
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    public static final String REGISTERED_USER = "registered_user";
    public static final String BLUETOOTH_ALWAYS_ON = "bluetooth_always_on";

    /* Messaging group type */
    public class MessageType {
        public static final int MESSAGE_SENDER = 1;
        public static final int MESSAGE_RECEIVER = 2;
    }

}
