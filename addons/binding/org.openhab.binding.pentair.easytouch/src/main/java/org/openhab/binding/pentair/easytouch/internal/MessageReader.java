package org.openhab.binding.pentair.easytouch.internal;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class MessageReader implements SerialPortEventListener {

    private Logger logger = LoggerFactory.getLogger(MessageReader.class);

    Panel m_panel;
    InputStream m_inStream;

    public enum State {
        Start,
        P1,
        P2,
        P3,
        Preamble,
        H1,
        H2,
        H3,
        Header,
        Length,
        Payload,
        C1,
        C2,
        Done
    }

    private State state = State.Start;
    private int length;
    private int position;
    private int[] payload;
    private byte[] checkSumBytes = new byte[2];
    private int checksumCalc;
    private int checksumMsg;
    private Message msg = null;
    private int msgCnt = 0;

    public MessageReader(Panel panel, InputStream inStream) {
        this.m_panel = panel;
        this.m_inStream = inStream;
    }

    // @Override
    @Override
    public synchronized void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                byte[] bytes = new byte[256];
                int bytesRead = m_inStream.read(bytes, 0, 256);
                logger.trace("Bytes read: {}", bytesRead);
                consumeBytes(bytes, bytesRead);
            } catch (IOException ioException) {
                logger.error("serialEvent(): IO Exception: {}", ioException.getMessage());
            }
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void consumeBytes(byte[] bytes, int bytesRead) {
        for (int i = 0; i < bytesRead; i++) {
            int b = bytes[i] & 0xFF;
            // logger.trace(Integer.toHexString(b) + " - " + state.toString());
            switch (state) {
                case Start:
                    if (b == 0xFF) {
                        state = State.P1;
                    } else {
                        state = State.Start;
                    }
                    break;
                case P1:
                    if (b == 0x00) {
                        state = State.P2;
                    } else if (b == 0xFF) {
                        state = State.P1;
                    } else {
                        state = State.Start;
                    }
                    break;
                case P2:
                    if (b == 0xFF) {
                        state = State.P3;
                    } else {
                        state = State.Start;
                    }
                    break;
                case P3:
                    if (b == 0xA5) {
                        state = State.Preamble;
                        checksumCalc = b;
                    } else {
                        state = State.Start;
                    }
                    break;
                case Preamble:
                    msg = new Message(m_panel.m_handler);
                    msg.other = (byte) b;
                    checksumCalc += b;
                    state = State.H1;
                    break;
                case H1:
                    msg.dest = (byte) b;
                    checksumCalc += b;
                    state = State.H2;
                    break;
                case H2:
                    msg.source = (byte) b;
                    checksumCalc += b;
                    state = State.H3;
                    break;
                case H3:
                    msg.cfi = (byte) b;
                    checksumCalc += b;
                    state = State.Header;
                    break;
                case Header:
                    msg.length = (byte) b;
                    length = b;
                    checksumCalc += b;
                    position = 0;
                    if (length > 0) {
                        payload = new int[length];
                        // logger.debug("Length = " + b);
                        state = State.Payload;
                    } else {
                        payload = new int[0];
                        state = State.C1;
                    }
                    break;
                case Payload:
                    try {
                        payload[position] = b;
                    } catch (Exception e) {
                        logger.error("Error parsing Payload", e);
                    }
                    checksumCalc += b;
                    position++;
                    if (position == length) {
                        state = State.C1;
                    }
                    break;
                case C1:
                    msg.payload = payload;
                    checkSumBytes[0] = (byte) b;
                    state = State.C2;
                    checksumMsg = b * 256;
                    break;
                case C2:
                    checkSumBytes[1] = (byte) b;
                    checksumMsg += b;
                    msg.checksum = checksumMsg;
                    // logger.debug("Message Checksum = " + checksumMsg);
                    logger.debug("");
                    if (checksumCalc == checksumMsg) {
                        // logger.debug("Finished C2 ... Do something with our message");
                        this.msgCnt++;
                        logger.debug("{}: {} {}", this.msgCnt, msg.toString(), Utils.formatCommandBytes(payload));
                        if (length > 0 && logger.isTraceEnabled()) {
                            logger.trace("Payload: {}", Utils.formatCommandBytes(payload));
                        }
                        msg.dispatchMessage(m_panel);
                    } else {
                        logger.debug("*********************** Checksums do not match ********************");
                    }
                    state = State.Start;
                    break;
            }
        }
    }
}
