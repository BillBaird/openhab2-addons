package org.openhab.binding.pentair.easytouch.internal;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.pentair.easytouch.handler.EasyTouchHandler;

class MessageFactory {

    EasyTouchHandler m_handler;

    MessageFactory(EasyTouchHandler handler) {
        m_handler = handler;
    }

    Message makeOnOffCommand(int circuitNum, OnOffType onOff) {
        Message msg = new Message();
        msg.other = 0x01;
        msg.source = m_handler.getBinderAddress();
        msg.dest = Const.PANEL_ADDRESS;
        msg.cfi = Const.CMD_SET_CIRCUIT;
        msg.length = 2;
        msg.payload = new int[] { circuitNum, (onOff == OnOffType.ON ? 0x01 : 0x00) };
        return msg;
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
