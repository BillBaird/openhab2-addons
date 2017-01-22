package org.openhab.binding.pentair.easytouch.internal;

public class Message {

    public byte source;
    public byte cfi;
    public byte dest;
    public byte other;
    public byte length;
    public int[] payload;
    public int checksum;

    public Message() {
    }

    public void dispatchMessage(Panel panel) {
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
    }

    public String getHeaderByteStr() {
        return Utils.getByteStr(this.other) + " " + Utils.getByteStr(this.dest) + " " + Utils.getByteStr(this.source)
                + " " + Utils.getByteStr(this.cfi);
    }

    public String getAddressStr() {
        return Utils.getAddrName(this.source) + " -> " + Utils.getAddrName(this.dest);
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
            case 0x01:
                if ((Utils.isPanel(this.source) && Utils.isPump(this.dest))
                        || (Utils.isPanel(this.dest) && Utils.isPump(this.source))) {
                    return this.getPumpCommandStr();
                } else {
                    return "Acknowledge " + Utils.getCommand(payload[0]);
                }
            case 0x04:
                return "SetControl "
                        + (payload[0] == 0x00 ? "Local" : payload[0] == 0x0FF ? "Remote" : "<UnknownControl>");
            case 0x06:
                return "SetRun " + (payload[0] == 0x0A ? "Start" : payload[0] == 0x04 ? "Stop" : "<UnknownRun>");
            case 0x07:
                return "PumpStatus";
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
