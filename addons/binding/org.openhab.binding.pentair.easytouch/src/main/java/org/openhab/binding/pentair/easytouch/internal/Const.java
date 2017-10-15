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

    /*
     * Command list discovery from SQL log:
     *
     * select s.Cmd, l.*
     * from PentairRawLog l
     * inner join (
     * select
     * Max(Id) as Id,
     * Right(p.RawHeader, 2) as Cmd,
     * Source,
     * Dest,
     * p.Command
     * from PentairRawLog p
     * inner join (
     * select distinct Right(RawHeader, 2) as Cmd, Command
     * from PentairRawLog
     * #where time > '2017-09-01'
     * ) x on Right(p.RawHeader, 2) = x.Cmd and p.Command = x.Command
     * group by Right(p.RawHeader, 2), p.Source, p.Dest, p.Command
     * ) s on l.Id = s.Id
     * order by s.Cmd;
     *
     */

    // Commands
    final static byte CMD_SET_ACK = 0x01; // Pump -> Panel, Panel -> Pump, Panel -> Wireless
    final static byte CMD_PANEL_STATUS = 0x02; // Panel -> All
    final static byte CMD_SET_CONTROL = 0x04; // Pump -> Panel, Panel -> Pump
    final static byte CMD_CURRENT_DATETIME = 0x05; // Panel -> All
    final static byte CMD_SET_RUN = 0x06; // Pump -> Panel, Panel -> Pump
    final static byte CMD_PUMP_STATUS = 0x07; // Pump -> Panel, Panel -> Pump
    final static byte CMD_TEMPERATURE_SET_POINTS = 0x08; // Panel -> All
    final static byte CMD_CUSTOM_NAME = 0x0A; // Panel -> All
    final static byte CMD_UNKNOWN_0B = 0x0B; // 14 00 5D 00 00 Panel -> All
    final static byte CMD_UNKNOWN_11 = 0x11; // 0C 0B 19 00 05 00 00 Panel -> All
    final static byte CMD_UNKNOWN_16 = 0x16; // 00 02 00 00 00 01 32 01 01 90 0D 7A 0F 82 00 00 Panel -> All
    final static byte CMD_PANEL_PUMP_STATUS = 0x17; // Panel -> All
    final static byte CMD_PUMP_CIRCUIT_SPEEDS = 0x18; // Panel -> All
    final static byte CMD_UNKNOWN_19 = 0x19; // 00 4B 01 00 00 00 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 Panel
                                             // -> All
    final static byte CMD_UNKNOWN_1D = 0x1D; // 03 00 00 00 00 08 FF FF FF 01 01 02 03 04 05 06 07 08 09 0A 01 02 03 04
                                             // Panel -> All
    final static byte CMD_UNKNOWN_1E = 0x1E; // 00 00 00 00 41 48 00 00 07 4A 00 00 07 3F 00 00 Panel -> All
    final static byte CMD_UNKNOWN_20 = 0x20; // 01 01 02 03 04 05 06 07 08 09 0A Panel -> All
    final static byte CMD_UNKNOWN_21 = 0x21; // 01 02 03 04 Panel -> All
    final static byte CMD_UNKNOWN_22 = 0x22; // 05 00 46 Panel -> All
    final static byte CMD_UNKNOWN_23 = 0x23; // 10 00 Panel -> All
    final static byte CMD_UNKNOWN_27 = 0x27; // 02 00 00 00 03 00 00 00 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
                                             // 00 00 00 00 00 00 00 00 Panel -> All
    final static byte CMD_UNKNOWN_28 = 0x28; // 00 00 00 00 00 00 00 00 00 00 Panel -> All

    final static byte CMD_SET_DATETIME = (byte) 0x85; // Wireless -> Panel
    final static byte CMD_SET_CIRCUIT_STATE = (byte) 0x86; // Wireless -> Panel
    final static byte CMD_SET_CUSTOM_NAME = (byte) 0x8A; // Wireless -> Panel
    final static byte CMD_SET_UNKNOWN_96 = (byte) 0x96; // 00 02 00 00 00 01 32 01 01 90 0D 7A 0F 82 00 00 Wireless ->
                                                        // Panel
    final static byte CMD_SET_PUMP_CIRCUIT_SPEEDS = (byte) 0x98; // Wireless -> Panel

    final static byte CMD_GET_DATETIME = (byte) 0xC5; // Wireless -> Panel
    final static byte CMD_GET_TEMPERATURE_SET_POINTS = (byte) 0xC8; // Note sure of this one
    final static byte CMD_GET_CUSTOM_NAME = (byte) 0xCA; // Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_CB = (byte) 0xCB; // 14 Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_D1 = (byte) 0xD1; // 0C Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_D6 = (byte) 0xD6; // 00 Wireless -> Panel
    final static byte CMD_GET_PANEL_PUMP_STATUS = (byte) 0xD7; // Wireless -> Panel
    final static byte CMD_GET_PUMP_CIRCUIT_SPEEDS = (byte) 0xD8; // Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_D9 = (byte) 0xD9; // 00 Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_DD = (byte) 0xDD; // 00 Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_DE = (byte) 0xDE; // 00 Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_E0 = (byte) 0xE0; // 01 Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_E1 = (byte) 0xE1; // 00 Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_E2 = (byte) 0xE2; // 00 Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_E3 = (byte) 0xE3; // 00 Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_E7 = (byte) 0xE7; // 00 Wireless -> Panel
    final static byte CMD_GET_UNKNOWN_E8 = (byte) 0xE8; // 00 Wireless -> Panel
    final static byte CMD_UNKNOWN_FC = (byte) 0xFC; // 00 02 82 00 00 01 0A 00 00 00 00 00 00 00 00 00 00 Panel -> All
    final static byte CMD_GET_UNKNOWN_FD = (byte) 0xFD; // 00 Wireless -> Panel
    final static byte CMD_UNKNOWN_FF = (byte) 0xFF; // 08 Pump -> Panel

    // Calendar constants
    final static long TIMEZONE_RAW_OFFSET_MILLIS = Calendar.getInstance().getTimeZone().getRawOffset();
    final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    final static long TEN_MINUTES = 10 * 60 * 1000;
    final static String[] WEEKDAYS = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
            "Saturday" };
}
