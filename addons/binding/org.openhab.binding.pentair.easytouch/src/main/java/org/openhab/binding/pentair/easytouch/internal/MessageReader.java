package org.openhab.binding.pentair.easytouch.internal;

import java.io.IOException;
import java.io.InputStream;

import org.openhab.binding.pentair.easytouch.handler.EasyTouchHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class MessageReader implements SerialPortEventListener {

    private Logger logger = LoggerFactory.getLogger(EasyTouchHandler.class);

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
    private byte[] dest = new byte[2];
    private byte[] source = new byte[2];
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
                    msg = new Message();
                    msg.other = (byte) b;
                    dest[0] = (byte) b;
                    checksumCalc += b;
                    state = State.H1;
                    break;
                case H1:
                    msg.dest = (byte) b;
                    dest[1] = (byte) b;
                    checksumCalc += b;
                    state = State.H2;
                    break;
                case H2:
                    msg.source = (byte) b;
                    source[0] = (byte) b;
                    checksumCalc += b;
                    state = State.H3;
                    break;
                case H3:
                    msg.cfi = (byte) b;
                    source[1] = (byte) b;
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
                        // logger.debug("Calculated Checksum = " + checksumCalc);
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

                        // logger.debug(this.msgCnt + ": " + msg.toString() + " 0x" + Utils.getByteStr(checkSumBytes[0])
                        // + Utils.getByteStr(checkSumBytes[1]));

                        // logger.debug(this.msgCnt + ": " + msg.toString() + " 0x" + Utils.getByteStr(checkSumBytes[0])
                        // + Utils.getByteStr(checkSumBytes[1]));

                        logger.debug("{}: {} {}", this.msgCnt, msg.toString(), Utils.formatCommandBytes(payload));

                        // Utils.printBytes("Source ", source, " " + Utils.getAddrName(source[0]) + "\n");
                        // Utils.printBytes("Dest ", dest, " " + Utils.getAddrName(dest[1]) + "\n");
                        // if (length > 0) {
                        // logger.debug("Len : " + length);
                        // Utils.printBytes("Payload ", payload, "\n");
                        // }
                        // else
                        // logger.debug("Payload : <empty>");
                        if (length > 0 && logger.isTraceEnabled()) {
                            logger.trace("Payload: {}", Utils.formatCommandBytes(payload));
                        }
                        // if (length > 0) {
                        // Utils.printBytes("Payload ", payload, "\n");
                        // }
                        // if (dest[1] == 0x10 && length == 2)
                        // if (payload[1] == 01)
                        // logger.debug("Turn " + Utils.getCircuitName(payload[0]) + " - " + payload[0] + " on");
                        // else if (payload[1] == 00)
                        // logger.debug("Turn " + Utils.getCircuitName(payload[0]) + " - " + payload[0] + " off");
                        msg.dispatchMessage(m_panel);
                        // if (dest[1] == 0x0F && length == 29) {
                        // logger.debug("Time : " + payload[0] + ":" + payload[1]);
                        // logger.debug("Mode : " + payload[2] + " - "
                        // + Utils.getModeName(payload[2], payload[3], payload[4]));
                        // logger.debug("Temp : Pool = " + payload[14] + ", Spa = " + payload[15] + ", Air = "
                        // + payload[18]);
                        // m_panel.consumePanelStatusMessage(payload);
                        // }
                        // if (dest[1] == 0x10 && length == 15) {
                        // System.out
                        // .println("Pump : "
                        // + (source[0] == 0x60 ? "Circulation"
                        // : (source[0] == 0x61 ? "Edge" : "<Unknown>"))
                        // + " is " + (payload[12] == 0x01 ? "on" : "off"));
                        // logger.debug("Time : " + payload[13] + ":" + payload[14]);
                        // if (payload[12] == 0x01) {
                        // logger.debug("Watts : " + (payload[3] * 256 + payload[4]));
                        // logger.debug("Rpms : " + (payload[5] * 256 + payload[6]));
                        // }
                        // }
                        // Utils.printBytes("Checksum", checkSumBytes, "\n");
                    } else {
                        logger.debug("*********************** Checksums do not match ********************");
                    }
                    state = State.Start;
                    break;
            }
        }
    }
}
