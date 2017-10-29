package org.openhab.binding.pentair.easytouch.internal;

import org.openhab.binding.pentair.easytouch.handler.EasyTouchHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message {

    private static final Logger logger = LoggerFactory.getLogger(Message.class);

    private final EasyTouchHandler m_handler;

    public byte source;
    public byte cmd;
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
        } else if (cmd == Const.CMD_PUMP_STATUS && dest == 0x10 && length == 15) {
            panel.consumePumpStatusMessage(this);
        } else if (cmd == Const.CMD_SET_RUN && dest == 0x10 && length == 01) {
            panel.consumePumpSetRunMessage(this);
            /*
             * } else if (cfi == Const.CFI_PUMP_COMMAND && dest == 0x10 && length == 02) {
             * panel.consumePumpCommandMessage(this);
             */
        } else if (cmd == Const.CMD_CUSTOM_NAME) {
            panel.consumeCustomName(this);
        } else if (cmd == Const.CMD_CIRCUIT_DEF) {
            panel.consumeCircuitDef(this);
        }
        panel.logMsg(this);
        panel.writeNewTime();
    }

    public boolean matches(Message m) {
        boolean result = length == m.length && cmd == m.cmd && source == m.source && dest == m.dest && other == m.other;
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
            command[7] = cmd;
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
                + " " + Utils.getByteStr(this.cmd);
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
        return Utils.getCommand(cmd);
    }

    public int getPayloadLength() {
        return length;
    }

    public String getPayloadByteStr() {
        return Utils.formatCommandBytes(payload);
    }

    public String getInterpretationStr(Panel panel) {
        return getCmdStr(panel);
    }

    public String getMsgTime() {
        switch (this.cmd & 0xFF) {
            case Const.CMD_PANEL_STATUS: // 0x02:
                return String.format("%02d:%02d", payload[0], payload[1]);
            case Const.CMD_PUMP_STATUS: // 0x07:
                return String.format("%02d:%02d", payload[13], payload[14]);
            case Const.CMD_SET_DATETIME & 0xFF: // 0x85:
                return String.format("%02d:%02d", payload[0], payload[1]);
            default:
                return null;
        }
    }

    public Integer getClockDrift() {
        switch (this.cmd & 0xFF) {
            case Const.CMD_PANEL_STATUS: // 0x02:
                return (int) Panel.calcClockDiff(payload[0], payload[1]);
            case Const.CMD_PUMP_STATUS: // 0x07:
                return (int) Panel.calcClockDiff(payload[13], payload[14]);
            case Const.CMD_SET_DATETIME & 0xFF: // 0x85:
                return (int) Panel.calcClockDiff(payload[0], payload[1]);
            default:
                return null;
        }
    }

    public String getPanelPumpAckStr() {
        if (this.length == 4) {
            switch (payload[0] << 8 | payload[1]) {
                case 0x02C4:
                    return getDestStr() + " RPMs " + (payload[2] << 8 | payload[3]);
                default:
                    return "<Unknown Panel Pump Ack - " + getPayloadByteStr() + ">";
            }
        } else {
            return "<Unknown Panel Pump Ack - unknown length>";
        }
    }

    public String getPumpPanelAckStr() {
        if (this.length == 2) {
            int rpms = payload[0] << 8 | payload[1];
            if (rpms <= 3450) {
                return getSourceStr() + " RPMs " + rpms;
            } else {
                return getSourceStr() + " state " + getPayloadByteStr();
            }
        } else {
            return "<Unknown Pump Ack - unknown length>";
        }
    }

    public String getPanelPumpStatusStr() {
        String result = "Pump " + payload[0] + " - ";
        if (payload[13] == 0x00) {
            result += "off";
        } else {
            if (payload[13] == 0x01) {
                result += "on, ";
            } else if (payload[13] == 0x0B) {
                result += "priming, ";
            }
            result += (payload[6] << 8 | payload[7]) + " RPMs, " + (payload[4] << 8 | payload[5]) + " watts";
        }
        result += ", [1]=" + Utils.getByteStr(payload[1]) + ", [15]=" + Utils.getByteStr(payload[15]);
        return result;
    }

    public String parseCustomName() {
        int len = 11;
        for (int i = 1; i <= 11; i++) {
            if (payload[i] == 0x00) {
                len = i - 1;
                break;
            }
        }
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = (char) payload[i + 1];
        }
        return new String(chars);
    }

    private String parsePumpCircuitSpeedMsg() {
        String result = "Pump " + payload[0] + "(";
        switch (payload[1]) {
            case 0x06:
                result += "VF";
                break;
            case 0x80:
                result += "VS";
                break;
            case 0x40:
                result += "VSF";
                break;
            default:
                result += "unknown";
                break;
        }
        if (payload[2] == 0F) {
            result += "-no priming) ";
        } else {
            result += "-prime " + payload[2] + " minute@" + (payload[21] * 256 + payload[30]) + ") ";
        }
        for (int c = 0; c <= 7; c++) {
            result += c + ":";
            int circuit = payload[5 + c * 2];
            if (circuit == 0) {
                result += "unused";
            } else {
                result += circuit + "-" + (payload[6 + c * 2] * 256 + payload[22 + c]);
            }
            if (c < 7) {
                result += ", ";
            }
        }
        return result;
    }

    private String getCircuitDefStr(Panel panel) {
        String result = payload[0] + ": ";
        if (panel != null) {
            result += panel.getCircuitInxName(payload[2]) + " (" + Const.CIRCUIT_FUNCTIONS[payload[1] & 0x0F] + ")";
        } else {
            result += Utils.getCircuitName(payload[2]) + " (" + Const.CIRCUIT_FUNCTIONS[payload[1] & 0x0F] + ")";
        }
        if ((payload[1] & 0x40) == 0x40) {
            result += ", On w/Freeze";
        }
        return result;
    }

    private String getScheduleStr(Panel panel) {
        String result = payload[0] + ": ";
        result += "Circuit " + payload[1];
        if (panel != null) {
            result += " (" + panel.getCircuitFeatureName(payload[1]) + ")";
        }
        if ((payload[6] & 0xFF) == 0xFF) {
            result += String.format(", on %02d:%02d, off %02d:%02d", payload[2], payload[3], payload[4], payload[5]);
        } else if (payload[6] == 0x00) {
            if (payload[2] == 25) {
                result += ", not configured";
            } else {
                result += ", unexpected value " + Utils.getByteStr(payload[2]) + " at byte 2";
            }
        } else {
            result += ", unexpected value " + Utils.getByteStr(payload[6]) + " at byte 6";
        }
        return result;
    }

    public String getCmdStr(Panel panel) {
        switch (this.cmd & 0xFF) {
            case Const.CMD_SET_ACK: // 0x01:
                if (Utils.isPanel(this.source) && Utils.isPump(this.dest)) {
                    return this.getPanelPumpAckStr();
                } else if (Utils.isPump(this.source) && Utils.isPanel(this.dest)) {
                    return this.getPumpPanelAckStr();
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
                return String.format("Current DateTime %02d:%02d %s %02d/%02d/%02d", payload[0], payload[1],
                        Const.WEEKDAYS[payload[2] - 1], payload[4], payload[3], payload[5]);
            case Const.CMD_SET_RUN: // 0x06:
                return "SetRun " + (payload[0] == 0x0A ? "Start" : payload[0] == 0x04 ? "Stop" : "<UnknownRun>");
            case Const.CMD_PUMP_STATUS: // 0x07:
                return "PumpStatus " + payload[13] + ":" + payload[14] + " Diff: "
                        + Panel.calcClockDiff(payload[13], payload[14]);
            case Const.CMD_TEMPERATURE_SET_POINTS: // 0x08:
                return "SetPoints? " + "Pool Set to " + payload[3] + ", Spa Set to " + payload[4] + ", Air Temp "
                        + payload[2] + " - More UNKNOWN";
            case Const.CMD_CUSTOM_NAME: // 0x0A:
                return "Custom Name " + payload[0] + " = " + parseCustomName();
            case Const.CMD_CIRCUIT_DEF: // 0x0B:
                return "Circuit Def " + getCircuitDefStr(panel);
            case Const.CMD_SCHEDULE: // 0x11:
                return "Schedule " + getScheduleStr(panel);
            case Const.CMD_PANEL_PUMP_STATUS: // 0x17:
                return "PanelPumpStatus " + this.getPanelPumpStatusStr();
            case Const.CMD_PUMP_CIRCUIT_SPEEDS: // 0x18:
                return "PumpCircuitSpeeds " + this.parsePumpCircuitSpeedMsg();

            case Const.CMD_SET_DATETIME & 0xFF: // 0x85:
                return String.format("Current DateTime %02d:%02d %s %02d/%02d/%02d", payload[0], payload[1],
                        Const.WEEKDAYS[payload[2] - 1], payload[4], payload[3], payload[5]);
            case Const.CMD_SET_CIRCUIT_STATE & 0xFF: // 0x86:
                return "Set Circuit State " + m_handler.getItemNames(payload[0]) + " " + Utils.getOnOff(payload[1]);
            case Const.CMD_SET_CUSTOM_NAME & 0xFF: // 0x8A:
                return "Set Custom Name " + payload[0] + " = " + parseCustomName();
            case Const.CMD_SET_CIRCUIT_DEF & 0xFF: // 0x8B
                return "Set Circuit Def " + getCircuitDefStr(panel);
            case Const.CMD_SET_SCHEDULE & 0xFF: // 0x91
                return "Set Schedule " + getScheduleStr(panel);
            case Const.CMD_SET_PUMP_CIRCUIT_SPEEDS & 0xFF: // 0x98:
                return "Set PumpCircuitSpeeds " + this.parsePumpCircuitSpeedMsg();

            case Const.CMD_GET_DATETIME & 0xFF: // 0xC5:
                return "Get DateTime";
            case Const.CMD_GET_TEMPERATURE_SET_POINTS & 0xFF: // 0xC8:
                return "Get SetPoints";
            case Const.CMD_GET_CUSTOM_NAME & 0xFF: // 0xCA:
                return "Get Custom Name " + Utils.getByteStr(payload[0]);
            case Const.CMD_GET_CIRCUIT_DEF & 0xFF: // 0xCB
                return "Get Circuit Def " + Utils.getByteStr(payload[0]);
            case Const.CMD_GET_SCHEDULE & 0xFF: // 0xD1
                return "Get Schedule " + Utils.getByteStr(payload[0]);
            case Const.CMD_GET_PANEL_PUMP_STATUS & 0xFF: // 0xD7:
                return "Get Panel Pump Status - Pump " + payload[0];
            case Const.CMD_GET_PUMP_CIRCUIT_SPEEDS & 0xFF: // 0xD8:
                return "Get Pump Circuit Speeds - Pump " + payload[0];
            default:
                return Utils.getCommand(this.cmd);
        }
    }

    @Override
    public String toString() {
        return this.getHeaderByteStr() + " - " + this.getAddressStr() + ": " + this.getCmdStr(null) +
        // ", " + Utils.getByteStr(this.other) +
                " (" + this.length + " bytes)";
    }

}
