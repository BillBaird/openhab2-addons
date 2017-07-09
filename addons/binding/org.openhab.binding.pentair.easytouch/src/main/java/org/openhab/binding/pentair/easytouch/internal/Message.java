package org.openhab.binding.pentair.easytouch.internal;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {

    private static final Logger logger = LoggerFactory.getLogger(Message.class);

    public byte source;
    public byte cfi;
    public byte dest;
    public byte other;
    public byte length;
    public int[] payload;
    public int checksum;
    public byte[] command;

    public Message() {
    }

    public void dispatchMessage(Panel panel) {
        panel.handleAcknowledgement(this);
        if (dest == 0x0F && length == 29) {
            panel.consumePanelStatusMessage(payload);
        } else if (cfi == Const.CFI_PUMP_STAT && dest == 0x10 && length == 15) {
            panel.consumePumpStatusMessage(this);
        } else if (cfi == Const.CFI_PUMP_SETRUN && dest == 0x10 && length == 01) {
            panel.consumePumpSetRunMessage(this);
            /*
             * } else if (cfi == Const.CFI_PUMP_COMMAND && dest == 0x10 && length == 02) {
             * panel.consumePumpCommandMessage(this);
             */
        }
        panel.logMsg(this);
    }

    public boolean matches(Message m) {
        boolean result = length == m.length && cfi == m.cfi && source == m.source && dest == m.dest && other == m.other;
        if (result) {
            for (int i = length - 1; i >= 0; i--) {
                result = result && (payload[i] == m.payload[i]);
            }
        }
        return result;
    }

    public byte[] asBytes() {
        if (command == null) {
            command = new byte[11 + length];
            command[0] = Const.PREAMBLE_1;
            command[1] = Const.PREAMBLE_2;
            command[2] = Const.PREAMBLE_3;
            command[3] = Const.PREAMBLE_4;
            command[4] = other;
            command[5] = dest;
            command[6] = source;
            command[7] = cfi;
            command[8] = length;
            for (int i = 0; i < payload.length; i++) {
                command[9 + i] = (byte) (payload[i] & 0xFF);
            }
            Utils.setCheckSum(command);
        }
        return command;
    }

    public String getHeaderByteStr() {
        return Utils.getByteStr(this.other) + " " + Utils.getByteStr(this.dest) + " " + Utils.getByteStr(this.source)
                + " " + Utils.getByteStr(this.cfi);
    }

    public String getSourceStr() {
        return Utils.getAddrName(source);
    }

    public String getDestStr() {
        return Utils.getAddrName(dest);
    }

    public String getAddressStr() {
        return getSourceStr() + " -> " + getDestStr();
    }

    public String getCommandStr() {
        return Utils.getCommand(cfi);
    }

    public int getPayloadLength() {
        return length;
    }

    public String getPayloadByteStr() {
        return Utils.formatCommandBytes(payload);
    }

    public String getInterpretationStr() {
        return getCfiStr();
    }

    public String getPumpCommandStr() {
        if (this.length >= 2) {
            switch (payload[0] << 8 | payload[1]) {
                case 0x02C4:
                    return "Pump RPMs " + (payload[2] << 8 | payload[3]);
                default:
                    return "<Unknown Pump Command>";
            }
        } else {
            return "<Unknown Pump Command - short length>";
        }
    }

    private long calcClockDiff(int hours, int minutes) {
        long msgTimeSecs = hours * 3600 + minutes * 60;
        long currentTimeSecs = (Calendar.getInstance().getTimeInMillis() + Const.TIMEZONE_RAW_OFFSET_MILLIS)
                % Const.MILLIS_PER_DAY / 1000;
        return msgTimeSecs - currentTimeSecs;
    }

    public String getCfiStr() {
        switch (this.cfi & 0xFF) {
            case 0x01:
                if ((Utils.isPanel(this.source) && Utils.isPump(this.dest))
                        || (Utils.isPanel(this.dest) && Utils.isPump(this.source))) {
                    return this.getPumpCommandStr();
                } else {
                    return "Acknowledge " + Utils.getCommand(payload[0]);
                }
            case 0x02:
                return "PanelStatus " + payload[0] + ":" + payload[1] + " Diff: "
                        + calcClockDiff(payload[0], payload[1]);
            case 0x04:
                return "SetControl "
                        + (payload[0] == 0x00 ? "Local" : payload[0] == 0x0FF ? "Remote" : "<UnknownControl>");
            case 0x06:
                return "SetRun " + (payload[0] == 0x0A ? "Start" : payload[0] == 0x04 ? "Stop" : "<UnknownRun>");
            case 0x07:
                return "PumpStatus " + payload[13] + ":" + payload[14] + " Diff: "
                        + calcClockDiff(payload[13], payload[14]);
            case 0x08:
                return "SetPoints? " + "Pool Set to " + payload[3] + ", Spa Set to " + payload[4] + ", Air Temp "
                        + payload[2] + " - More UNKNOWN";
            case 0x86:
                return "SetState " + Utils.getCircuitName(payload[0]) + " " + Utils.getOnOff(payload[1]);
            case 0xC8:
                return "Get SetPoints? ";
            default:
                return Utils.getCommand(this.cfi);
        }
    }

    @Override
    public String toString() {
        return this.getHeaderByteStr() + " - " + this.getAddressStr() + ": " + this.getCfiStr() +
        // ", " + Utils.getByteStr(this.other) +
                " (" + this.length + " bytes)";
    }

}
