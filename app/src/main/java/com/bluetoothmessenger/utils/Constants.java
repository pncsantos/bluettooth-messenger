package com.bluetoothmessenger.utils;

import java.util.UUID;

public class Constants {

    // Unique UUID for this application
    public static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

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

    public static final String REGISTERED_DEVICE_NAME = "registered_device_name";
    public static final String BLUETOOTH_ALWAYS_ON = "bluetooth_always_on";

    /* Messaging group type */
    public class MessageType {
        public static final int MESSAGE_SENDER = 1;
        public static final int MESSAGE_RECEIVER = 2;
    }

    public class BluetoothBondType {
        public static final String CREATE_BOND = "createBond";
        public static final String REMOVE_BOND = "removeBond";
    }
}
