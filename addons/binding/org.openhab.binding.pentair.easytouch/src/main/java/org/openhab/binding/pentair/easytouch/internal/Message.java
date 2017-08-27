package org.openhab.binding.pentair.easytouch.internal;

import java.util.Calendar;

import org.openhab.binding.pentair.easytouch.handler.EasyTouchHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {

    private static final Logger logger = LoggerFactory.getLogger(Message.class);

    private final EasyTouchHandler m_handler;

    public byte source;
    public byte cfi;
    public byte dest;
    public byte other;
    public byte length;
    public int[] payload;
    public int checksum;
    public byte[] command;

    public Message(EasyTouchHandler handler) {
        m_handler = handler;
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
        panel.writeNewTime();
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

    public String getCfiStr() {
        switch (this.cfi & 0xFF) {
            case Const.CMD_SET_ACK: // 0x01:
                if ((Utils.isPanel(this.source) && Utils.isPump(this.dest))
                        || (Utils.isPanel(this.dest) && Utils.isPump(this.source))) {
                    return this.getPumpCommandStr();
                } else {
                    return "Acknowledge " + Utils.getCommand(payload[0]);
                }
            case Const.CMD_PANEL_STATUS: // 0x02:
                return "PanelStatus " + payload[0] + ":" + payload[1] + " Diff: "
                        + Panel.calcClockDiff(payload[0], payload[1]);
            case Const.CMD_SET_CONTROL: // 0x04:
                return "SetControl "
                        + (payload[0] == 0x00 ? "Local" : payload[0] == 0x0FF ? "Remote" : "<UnknownControl>");
            case Const.CMD_CURRENT_DATETIME: // 0x05:
                return String.format("Current DateTime %2d:%2d %s %2d/%2d/%2d", payload[0], payload[1],
                        Const.WEEKDAYS[payload[2] - 1], payload[4], payload[3], payload[5]);
            case Const.CMD_SET_RUN: // 0x06:
                return "SetRun " + (payload[0] == 0x0A ? "Start" : payload[0] == 0x04 ? "Stop" : "<UnknownRun>");
            case Const.CMD_PUMP_STATUS: // 0x07:
                return "PumpStatus " + payload[13] + ":" + payload[14] + " Diff: "
                        + Panel.calcClockDiff(payload[13], payload[14]);
            case Const.CMD_TEMPERATURE_SET_POINTS: // 0x08:
                return "SetPoints? " + "Pool Set to " + payload[3] + ", Spa Set to " + payload[4] + ", Air Temp "
                        + payload[2] + " - More UNKNOWN";
            case Const.CMD_SET_DATETIME & 0xFF: // 0x85:
                return String.format("Current DateTime %2d:%2d %s %2d/%2d/%2d", payload[0], payload[1],
                        Const.WEEKDAYS[payload[2] - 1], payload[4], payload[3], payload[5]);
            case Const.CMD_SET_CIRCUIT_STATE & 0xFF: // 0x86:
                return "Set Circuit State " + m_handler.getItemNames(payload[0]) + " " + Utils.getOnOff(payload[1]);
            case Const.CMD_GET_DATETIME & 0xFF: // 0xC5:
                return "Get DateTime";
            case Const.CMD_GET_TEMPERATURE_SET_POINTS & 0xFF: // 0xC8:
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
