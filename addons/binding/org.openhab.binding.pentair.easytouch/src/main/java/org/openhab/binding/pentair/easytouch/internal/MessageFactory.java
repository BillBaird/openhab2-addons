package org.openhab.binding.pentair.easytouch.internal;

import java.util.Calendar;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.pentair.easytouch.handler.EasyTouchHandler;

class MessageFactory {

    EasyTouchHandler m_handler;

    MessageFactory(EasyTouchHandler handler) {
        m_handler = handler;
    }

    Message makeOnOffCommand(int circuitNum, OnOffType onOff) {
        Message msg = new Message(m_handler);
        msg.other = 0x01;
        msg.source = m_handler.getBinderAddress();
        msg.dest = Const.PANEL_ADDRESS;
        msg.cmd = Const.CMD_SET_CIRCUIT_STATE;
        msg.length = 2;
        msg.payload = new int[] { circuitNum, (onOff == OnOffType.ON ? 0x01 : 0x00) };
        return msg;
    }

    Message makeOnOffAck() {
        return makeSimpleAck(Const.CMD_SET_CIRCUIT_STATE);
    }

    Message makeSetDateTime(Calendar dt) {
        Message msg = new Message(m_handler);
        msg.other = 0x01;
        msg.source = m_handler.getBinderAddress();
        msg.dest = Const.PANEL_ADDRESS;
        msg.cmd = Const.CMD_SET_DATETIME;
        msg.length = 8;
        msg.payload = new int[] { dt.get(Calendar.HOUR_OF_DAY), dt.get(Calendar.MINUTE), dt.get(Calendar.DAY_OF_WEEK),
                dt.get(Calendar.DAY_OF_MONTH), dt.get(Calendar.MONTH) + 1, dt.get(Calendar.YEAR) % 100, 0, 0 };
        return msg;
    }

    Message makeSetDateTimeAck() {
        return makeSimpleAck(Const.CMD_SET_DATETIME);
    }

    Message makeSimpleAck(byte msgType) {
        Message msg = new Message(m_handler);
        msg.other = 0x01;
        msg.source = Const.PANEL_ADDRESS;
        msg.dest = m_handler.getBinderAddress();
        msg.cmd = Const.CMD_SET_ACK;
        msg.length = 1;
        msg.payload = new int[] { msgType & 0xFF };
        return msg;
    }
}
