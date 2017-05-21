package org.openhab.binding.pentair.easytouch.internal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.pentair.easytouch.handler.EasyTouchHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Panel {

    private Logger logger = LoggerFactory.getLogger(EasyTouchHandler.class);
    EasyTouchHandler m_handler;

    public class Circuit {
        public final Channel channel;
        public final int circuitNum;
        public final int inx;
        public final int mask;
        public Boolean onOff;
        public boolean pendingResponse;

        public Circuit(Channel channel, int circuitNum) {
            this.channel = channel;
            this.circuitNum = circuitNum;
            int i = circuitNum - 1;
            this.inx = 2 + (i / 8); // The byte index where the status is found
            int mask = 1 << i; // The bit for the mask, always in the lower order byte
            while (mask >= 0x100) {
                mask = mask >> 8;
            }
            this.mask = mask;
            this.onOff = null;
            this.pendingResponse = false;
        }
    }

    public class Pump {
        public final Channel pumpChannel;
        public final Channel wattsChannel;
        public final Channel rpmsChannel;
        public final int pumpNum;
        public final int inx;
        public Boolean onOff;
        public Integer watts;
        public Integer rpms;

        public Pump(Channel pumpChannel, Channel wattsChannel, Channel rpmsChannel, int pumpNum) {
            this.pumpChannel = pumpChannel;
            this.wattsChannel = wattsChannel;
            this.rpmsChannel = rpmsChannel;
            this.pumpNum = pumpNum;
            this.inx = pumpNum + 0;
            this.onOff = null;
            this.watts = null;
            this.rpms = null;
        }
    }

    private Circuit[] circuits;
    private Pump[] pumps;
    private Channel airTempChannel;
    private int airTemp = -999;
    private Sequencer sequencer;

    public Panel(EasyTouchHandler handler) {
        this.m_handler = handler;
        airTempChannel = handler.getThing().getChannel("temp-airtemp");
        circuits = new Circuit[10];
        for (int i = 1; i <= 10; i++) {
            Channel channel = handler.getThing().getChannel("equipment-circuit" + i);
            circuits[i - 1] = new Circuit(channel, i);
        }
        pumps = new Pump[8];
        for (int i = 1; i <= 8; i++) {
            Channel pumpChannel = handler.getThing().getChannel("pumps-pump" + i);
            Channel wattsChannel = handler.getThing().getChannel("watts-pump" + i);
            Channel rpmsChannel = handler.getThing().getChannel("rpms-pump" + i);
            pumps[i - 1] = new Pump(pumpChannel, wattsChannel, rpmsChannel, i);
        }
        sequencer = new Sequencer(handler);
    }

    public void consumePumpStatusMessage(Message msg) {
        Pump pump = pumps[msg.source & 0x07];
        int[] payload = msg.payload;
        logger.debug("PumpStatus Payload: {}", Utils.formatCommandBytes(msg.payload));

        boolean onOff = payload[12] != 0x00; // Have seen 0x01 and 0x0B
        if (pump.onOff == null || pump.onOff != onOff) {
            logger.trace("{} = {}", pump.pumpChannel.getChannelTypeUID().getAsString(), onOff);
            pump.onOff = onOff;
            State state = onOff ? OnOffType.ON : OnOffType.OFF;
            m_handler.updateState(pump.pumpChannel, state);
        }

        int watts = payload[3] * 256 + payload[4];
        if (pump.watts == null || pump.watts != watts) {
            pump.watts = watts;
            State state = new DecimalType(watts);
            m_handler.updateState(pump.wattsChannel, state);
        }

        int rpms = payload[5] * 256 + payload[6];
        if (pump.rpms == null || pump.rpms != rpms) {
            pump.rpms = rpms;
            State state = new DecimalType(rpms);
            m_handler.updateState(pump.rpmsChannel, state);
        }
    }

    public void consumePumpSetRunMessage(Message msg) {
        Pump pump = pumps[msg.source & 0x07];
        int runByte = msg.payload[0];

        if (runByte == 0x0A || runByte == 0x04) {
            boolean onOff = runByte == 0x0A;
            if (pump.onOff == null || pump.onOff != onOff) {
                logger.trace("{} = {}", pump.pumpChannel.getChannelTypeUID().getAsString(), onOff);
                pump.onOff = onOff;
                State state = onOff ? OnOffType.ON : OnOffType.OFF;
                m_handler.updateState(pump.pumpChannel, state);

                if (!onOff) {
                    if (pump.watts == null || pump.watts != 0) {
                        pump.watts = 0;
                        state = new DecimalType(0);
                        m_handler.updateState(pump.wattsChannel, state);
                    }
                    if (pump.rpms == null || pump.rpms != 0) {
                        pump.rpms = 0;
                        state = new DecimalType(0);
                        m_handler.updateState(pump.rpmsChannel, state);
                    }
                }
            }
        }
    }

    /*
     * public void consumePumpCommandMessage(Message msg) {
     * Pump pump = pumps[msg.source & 0x07];
     * int[] payload = msg.payload;
     * int rpms = payload[0] * 256 + payload[1];
     * if (pump.rpms == null || pump.rpms != rpms) {
     * pump.rpms = rpms;
     * State state = new DecimalType(rpms);
     * m_handler.updateState(pump.rpmsChannel, state);
     * }
     * }
     */

    public void consumePanelStatusMessage(int[] payload) {
        for (Circuit c : circuits) {
            boolean onOff = (payload[c.inx] & c.mask) != 0;
            if (c.onOff == null || c.onOff != onOff) {
                logger.trace("{} = {}", c.channel.getChannelTypeUID().getAsString(), onOff);
                c.onOff = onOff;
                State state = onOff ? OnOffType.ON : OnOffType.OFF;
                m_handler.updateState(c.channel, state);
            }
        }

        logger.trace("{} = {}", airTempChannel.getChannelTypeUID().getAsString(), payload[18]);
        int value = payload[18];
        if (value != airTemp) {
            airTemp = payload[18];
            State state = new DecimalType(airTemp);
            m_handler.updateState(airTempChannel, state);
        }
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        if (OnOffType.class.isInstance(command)) {
            OnOffType onOff = (OnOffType) command;
            String cUID = channelUID.getId();
            if (cUID.startsWith("equipment-circuit")) {
                int circuitNum = Integer.parseInt(cUID.substring(17));
                byte[] onOffCommand = sequencer.makeOnOffCommand(circuitNum, onOff);
                if (logger.isTraceEnabled()) {
                    logger.trace("OnOffCommand: {}", Utils.formatCommandBytes(onOffCommand));
                }
                // Utils.printBytes("\nOnOffCommand", onOffCommand, "\n\n");
                m_handler.write(onOffCommand);
            }
        }
    }

}
