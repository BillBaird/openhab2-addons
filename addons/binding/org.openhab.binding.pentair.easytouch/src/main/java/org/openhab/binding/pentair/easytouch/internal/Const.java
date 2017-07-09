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

    // Command Sequence Templates
    final static byte CMD_SET_ACK = 0x01;
    final static byte CMD_SET_CIRCUIT = (byte) 0x86;

    // Command (CFI) values
    final static byte CFI_PUMP_COMMAND = (byte) 0x01;
    final static byte CFI_PUMP_SETCONTROL = (byte) 0x04;
    final static byte CFI_PUMP_SETRUN = (byte) 0x06;
    final static byte CFI_PUMP_STAT = (byte) 0x07;

    // Calendar constants
    final static long TIMEZONE_RAW_OFFSET_MILLIS = Calendar.getInstance().getTimeZone().getRawOffset();
    final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    final static long TEN_MINUTES = 10 * 60 * 1000;
}
