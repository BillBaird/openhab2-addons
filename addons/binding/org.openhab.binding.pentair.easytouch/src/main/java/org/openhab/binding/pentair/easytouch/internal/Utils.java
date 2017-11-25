package org.openhab.binding.pentair.easytouch.internal;

public class Utils {

    public static String getByteStr(int b) {
        String result = Integer.toHexString(b & 0xFF).toUpperCase();
        if (result.length() == 1) {
            result = "0" + result;
        }
        return result;
    }

    public static void printByte(String name, byte b, String suffix) {
        System.out.print(name + ": ");
        String value = Utils.getByteStr(b);
        System.out.print(value + " ");
        System.out.print(suffix);
    }

    public static void printBytes(String name, int[] bytes, String suffix) {
        System.out.print(name + ": ");
        for (int i = 0; i < bytes.length; i++) {
            String value = Utils.getByteStr(bytes[i]);
            System.out.print(value + " ");
        }
        System.out.print(suffix);
    }

    /*
     * public static void printBytes(String name, byte[] bytes, String suffix) {
     * System.out.print(name + ": ");
     * for (int i = 0; i < bytes.length; i++) {
     * String value = Utils.getByteStr(bytes[i]);
     * System.out.print(value + " ");
     * }
     * System.out.print(suffix);
     * }
     */
    public static String formatCommandBytes(Message msg) {
        return formatCommandBytes(msg.asBytes());
    }

    public static String formatCommandBytes(int[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String digits = Integer.toHexString(bytes[i] & 0xFF).toUpperCase();
            if (digits.length() == 1) {
                sb.append("0");
            }
            sb.append(digits).append(" ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String formatCommandBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String digits = Integer.toHexString(bytes[i] & 0xFF).toUpperCase();
            if (digits.length() == 1) {
                sb.append("0");
            }
            sb.append(digits).append(" ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String getOnOff(int onOff) {
        return onOff == 0 ? "off" : onOff == 1 ? "on" : "<unknown OnOff state>";
    }

    /*
     * public static String getModeName(int mode1, int mode2, int mode3) {
     * if (mode1 == 0x00 && mode2 == 0x00 && mode3 == 0x00) {
     * return "all off";
     * }
     * String result = "";
     * if ((mode1 & 0x01) == 0x01) {
     * result += "Circulation, ";
     * }
     * if ((mode1 & 0x02) == 0x02) {
     * result += "SPA light, ";
     * }
     * if ((mode1 & 0x04) == 0x04) {
     * result += "Pool light, ";
     * }
     * if ((mode1 & 0x08) == 0x08) {
     * result += "Spa Jets, ";
     * }
     * if ((mode1 & 0x10) == 0x10) {
     * result += "Air Blower, ";
     * }
     * if ((mode1 & 0x20) == 0x20) {
     * result += "Pool Vac m1x40, ";
     * }
     * if ((mode1 & 0x40) == 0x40) {
     * result += "Edge Pump, ";
     * }
     * if ((mode1 & 0x80) == 0x80) {
     * result += "Spillway, ";
     * }
     * if ((mode2 & 0x01) == 0x01) {
     * result += "Patio Lights, ";
     * }
     * if ((mode2 & 0x02) == 0x02) {
     * result += "<Mode2 0x02>, ";
     * }
     * if ((mode2 & 0x04) == 0x04) {
     * result += "Pool Vac M2x04, ";
     * }
     * if ((mode2 & 0x08) == 0x08) {
     * result += "Edge Pump+, ";
     * }
     * if ((mode2 & 0x10) == 0x10) {
     * result += "Filter 3200, ";
     * }
     * if ((mode2 & 0x20) == 0x20) {
     * result += "Edge 3200, ";
     * }
     * if ((mode2 & 0x40) == 0x40) {
     * result += "Feature 5, ";
     * }
     * if ((mode2 & 0x80) == 0x80) {
     * result += "Feature 6, ";
     * }
     * if ((mode3 & 0x01) == 0x01) {
     * result += "Feature 7, ";
     * }
     * if ((mode3 & 0x02) == 0x02) {
     * result += "Feature 8, ";
     * }
     * if ((mode3 & 0x04) == 0x04) {
     * result += "<Mode3 0x04>, ";
     * }
     * if ((mode3 & 0x08) == 0x08) {
     * result += "AuxEx, ";
     * }
     * if ((mode3 & 0x10) == 0x10) {
     * result += "<Mode3 0x10>, ";
     * }
     * if ((mode3 & 0x20) == 0x20) {
     * result += "<Mode3 0x20>, ";
     * }
     * if ((mode3 & 0x40) == 0x40) {
     * result += "<Mode3 0x40>, ";
     * }
     * if ((mode3 & 0x80) == 0x80) {
     * result += "<Mode3 0x80>, ";
     * }
     * if (result.length() > 2) {
     * return result.substring(0, result.length() - 2);
     * }
     * return "???";
     * }
     */
    /*
     * public static String getCircuitName(int circuit) {
     * switch (circuit) {
     * case 0x01:
     * return "Circulation Pump";
     * case 0x02:
     * return "Spa Light";
     * case 0x03:
     * return "Pool Light";
     * case 0x04:
     * return "Spa Jets";
     * case 0x05:
     * return "Air Blower";
     * case 0x06:
     * return "Pool Vac 0x06";
     * case 0x07:
     * return "Edge Pump";
     * case 0x08:
     * return "Spillway";
     * case 0x09:
     * return "Patio Lights";
     * case 0x0B:
     * return "Pool Vac";
     * case 0x0C:
     * return "Edge Pump+";
     * case 0x0D:
     * return "Filter 3200";
     * case 0x0E:
     * return "Edge 3200";
     * case 0x0F:
     * return "Feature 5";
     * case 0x10:
     * return "Feature 6";
     * case 0x11:
     * return "Feature 7";
     * case 0x12:
     * return "Feature 8";
     * case 0x14:
     * return "AuxEx";
     * default:
     * return "<unknown>";
     * }
     * }
     */
    public static String getAddrName(byte addr) {
        switch (addr & 0xF0) {
            case 0x00:
                return addr == 0x0F ? "<all>" : "<unknown>";
            case 0x10:
                return "Panel";
            case 0x20:
                return (addr & 0x0F) == 2 ? "Wireless" : "Remote " + (addr & 0x0F);
            case 0x60:
                return "Pump " + ((addr & 0x0F) + 1);
            default:
                return "<unknown " + Utils.getByteStr(addr) + ">";
        }
    }

    public static String getCommand(int cmd) {
        switch (cmd & 0xFF) {
            case Const.CMD_SET_ACK: // 0x01:
                return "Acknowledge";
            case Const.CMD_PANEL_STATUS: // 0x02:
                return "PanelStatus";
            case Const.CMD_SET_CONTROL: // 0x04:
                return "SetControl";
            case Const.CMD_CURRENT_DATETIME: // 0x05:
                return "CurrentDateTime";
            case Const.CMD_SET_RUN: // 0x06:
                return "Run";
            case Const.CMD_PUMP_STATUS: // 0x07:
                return "PumpStatus";
            case Const.CMD_TEMPERATURE_SET_POINTS: // 0x08:
                return "TemperatureSettings";
            case Const.CMD_CUSTOM_NAME: // 0x0A:
                return "CustomName";
            case Const.CMD_CIRCUIT_DEF: // 0x0B:
                return "CircuitDef";
            case Const.CMD_SCHEDULE: // 0x11:
                return "Schedule";
            case Const.CMD_PANEL_PUMP_STATUS: // 0x17:
                return "PanelPumpStatus";
            case Const.CMD_PUMP_CIRCUIT_SPEEDS: // 0x18:
                return "PumpCircuitSpeeds";

            case Const.CMD_SET_DATETIME & 0xFF: // 0x85:
                return "SetDateTime";
            case Const.CMD_SET_CIRCUIT_STATE & 0xFF: // 0x86:
                return "SetState";
            case Const.CMD_SET_CUSTOM_NAME & 0xFF: // 0x8A:
                return "SetCustomName";
            case Const.CMD_SET_CIRCUIT_DEF & 0xFF: // 0x8B:
                return "SetCircuitDef";
            case Const.CMD_SET_SCHEDULE & 0xFF: // 0x91
                return "SetSchedule";
            case Const.CMD_SET_PUMP_CIRCUIT_SPEEDS & 0xFF: // 0x98:
                return "SetPumpCircuitSpeeds";

            case Const.CMD_GET_DATETIME & 0xFF: // 0xC5:
                return "GetDateTime";
            case Const.CMD_GET_TEMPERATURE_SET_POINTS & 0xFF: // 0xC8:
                return "GetTemperatureSettings";
            case Const.CMD_GET_CUSTOM_NAME & 0xFF: // 0xCA:
                return "GetCustomName";
            case Const.CMD_GET_CIRCUIT_DEF & 0xFF: // 0xCB
                return "GetCircuitDef";
            case Const.CMD_GET_SCHEDULE & 0xFF: // 0xD1
                return "GetSchedule";
            case Const.CMD_GET_PANEL_PUMP_STATUS & 0xFF: // 0xD7:
                return "GetPanelPumpStatus";
            case Const.CMD_GET_PUMP_CIRCUIT_SPEEDS & 0xFF: // 0xD8:
                return "GetPumpCircuitSpeeds";

            default:
                return "<command " + Utils.getByteStr(cmd) + " (" + (cmd & 0xFF) + ")>";
        }
    }

    public static boolean isPanel(byte addr) {
        return addr == 0x10;
    }

    public static boolean isPump(byte addr) {
        return addr >= 0x60 && addr <= 0x6F;
    }

    public static void setCheckSum(byte[] bytes) {
        int msgLast = bytes.length - 2;
        int checkSum = 0;
        for (int i = 3; i < msgLast; i++) {
            checkSum += bytes[i] & 0xFF;
        }
        bytes[msgLast] = (byte) (checkSum >> 8);
        bytes[msgLast + 1] = (byte) (checkSum & 0xFF);
    }

    public static String getCircuitName(int circName) {
        switch (circName) {
            case 0x00:
                return "[NOT USED]"; // unsure
            // case 0x01:
            // return "Aerator"; // unsure
            case 0x02:
                return "Air Blower";
            // case 0x03:
            // return "Aux 1"; // unsure
            // case 0x04:
            // return "Aux 10"; // unsure
            // case 0x05:
            // return "Aux 3"; // unsure
            // case 0x06:
            // return "Aux 4"; // unsure
            // case 0x07:
            // return "Aux 5"; // unsure
            // case 0x08:
            // return "Aux 6"; // unsure
            // case 0x09:
            // return "Aux 7"; // unsure
            // case 0x0A:
            // return "Aux 8"; // unsure
            // case 0x0B:
            // return "Aux 9"; // unsure
            // case 0x0D:
            // return "Back Light"; // unsure
            case 0x1B:
                return "Edge Pump";
            case 0x2E:
                return "Jets";
            case 0x38:
                return "Patio Lights";
            case 0x3D:
                return "Pool";
            case 0x3F:
                return "Pool Light";
            case 0x48:
                return "Spa";
            case 0x4A:
                return "Spa Light";
            case 0x4F:
                return "Spillway";
            case 0x5D:
                return "Aux Ex"; // unsure
            case 0x5E:
                return "Feature 1";
            case 0x5F:
                return "Feature 2";
            case 0x60:
                return "Feature 3";
            case 0x61:
                return "Feature 4";
            case 0x62:
                return "Feature 5";
            case 0x63:
                return "Feature 6";
            case 0x64:
                return "Feature 7";
            case 0x65:
                return "Feature 8";
            default:
                return "CircuitName 0x" + getByteStr(circName);
        }
    }
    /*
     * // Circuit Names
     * final static String[] CIRCUIT_NAMES = {
     * "[NOT USED]", "Aerator", "Air Blower",
     * "Aux 1", "Aux 10", "Aux 2", "Aux 3", "Aux 4", "Aux 5", "Aux 6", "Aux 7", "Aux 8", "Aux 9", "Aux Ex",
     * "Back Light", "Backwash", "BBQ Light", "Beach Light", "Booster Pump", "Bug Light",
     * "Cabana Lights", "Chemical Feeder", "Chlorinator", "Cleaner", "Color Wheel",
     * "Deck Light", "Drain Line", "Drive Light",
     * "Edge Pump", "Entry Light"
     * };
     */

}
