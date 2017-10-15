package org.openhab.binding.pentair.easytouch.internal;

import java.util.Calendar;

class Const {

    // List of all RS485 addresses
    final static byte PANEL_ADDRESS = 0x10;
    final static byte PUMP_0_ADDRESS = 0x60;
    final static byte BROADCAST_ADDRESS = 0x0F;

    // PreAmble
    final static byte PREAMBLE_1 = (byte) 0xFF;
    final static byte PREAMBLE_2 = (byte) 0x00;
    final static byte PREAMBLE_3 = (byte) 0xFF;
    final static byte PREAMBLE_4 = (byte) 0xA5;

    // Command values
    final static byte PLACEHOLDER = (byte) 0x00;
    final static byte SOURCE_ADDRESS_PLACEHOLDER = PLACEHOLDER;
    final static byte CIRCUIT_NUM_PLACEHOLDER = PLACEHOLDER;
    final static byte CIRCUIT_ON_OFF_PLACEHOLDER = PLACEHOLDER;
    final static int CHK_SUM_1_PLACEHOLDER = PLACEHOLDER;
    final static int CHK_SUM_2_PLACEHOLDER = PLACEHOLDER;

    // Commands
    final static byte CMD_SET_ACK = 0x01;
    final static byte CMD_PANEL_STATUS = 0x02;
    final static byte CMD_SET_CONTROL = 0x04;
    final static byte CMD_CURRENT_DATETIME = 0x05;
    final static byte CMD_SET_RUN = 0x06;
    final static byte CMD_PUMP_STATUS = 0x07;
    final static byte CMD_TEMPERATURE_SET_POINTS = 0x08;
    final static byte CMD_CUSTOM_NAME = 0x0A;
    final static byte CMD_PANEL_PUMP_STATUS = 0x17;
    final static byte CMD_PUMP_CIRCUIT_SPEEDS = 0x18;

    final static byte CMD_SET_DATETIME = (byte) 0x85;
    final static byte CMD_SET_CIRCUIT_STATE = (byte) 0x86;
    final static byte CMD_SET_CUSTOM_NAME = (byte) 0x8A;
    final static byte CMD_SET_PUMP_CIRCUIT_SPEEDS = (byte) 0x98;

    final static byte CMD_GET_DATETIME = (byte) 0xC5;
    final static byte CMD_GET_TEMPERATURE_SET_POINTS = (byte) 0xC8; // Note sure of this one
    final static byte CMD_GET_CUSTOM_NAME = (byte) 0xCA;
    final static byte CMD_GET_PANEL_PUMP_STATUS = (byte) 0xD7;
    final static byte CMD_GET_PUMP_CIRCUIT_SPEEDS = (byte) 0xD8;

    // Calendar constants
    final static long TIMEZONE_RAW_OFFSET_MILLIS = Calendar.getInstance().getTimeZone().getRawOffset();
    final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    final static long TEN_MINUTES = 10 * 60 * 1000;
    final static String[] WEEKDAYS = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
            "Saturday" };
}
