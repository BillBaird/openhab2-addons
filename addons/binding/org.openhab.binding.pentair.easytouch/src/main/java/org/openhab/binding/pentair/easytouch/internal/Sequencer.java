package org.openhab.binding.pentair.easytouch.internal;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.pentair.easytouch.handler.EasyTouchHandler;

class Sequencer {

    EasyTouchHandler m_handler;

    Sequencer(EasyTouchHandler handler) {
        m_handler = handler;
    }

    byte[] makeOnOffCommand(int circuitNum, OnOffType onOff) {
        byte[] cmd = Const.SEQ_SET_CIRCUIT.clone();
        cmd[Const.INX_SOURCE_ADDRESS] = m_handler.getBinderAddress();
        cmd[Const.INX_CIRCUIT_NUM] = (byte) circuitNum;
        cmd[Const.INX_CIRCUIT_ON_OFF] = (byte) (onOff == OnOffType.ON ? 0x01 : 0x00);
        Utils.setCheckSum(cmd);
        return cmd;
    }

    Message makeOnOffAck() {
        Message msg = new Message();
        msg.other = 0x01;
        msg.source = Const.PANEL_ADDRESS;
        msg.dest = m_handler.getBinderAddress();
        msg.cfi = Const.CMD_SET_ACK;
        msg.length = 1;
        msg.payload = new int[] { Const.CMD_SET_CIRCUIT & 0xFF };
        return msg;
    }
}
