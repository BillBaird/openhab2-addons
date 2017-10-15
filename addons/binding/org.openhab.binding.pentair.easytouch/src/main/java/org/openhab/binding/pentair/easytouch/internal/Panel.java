package org.openhab.binding.pentair.easytouch.internal;

import java.util.Calendar;

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

    private Logger logger = LoggerFactory.getLogger(Panel.class);
    EasyTouchHandler m_handler;

    Calendar nextTimeToSetClock = null;

    public class Circuit {
        private final Panel panel;
        public final Channel channel;
        public final int circuitNum;
        public final int inx;
        public final int mask;
        public Boolean onOff;
        public boolean pendingResponse;
        public int nameInx;
        public int funcInx;

        public Circuit(Panel panel, Channel channel, int circuitNum) {
            this.panel = panel;
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
            this.nameInx = 0;
            this.funcInx = 0;
        }

        public String getName() {
            return panel.getCircuitInxName(nameInx);
        }

        public String getFunction() {
            return Const.CIRCUIT_FUNCTIONS[funcInx];
        }
    }

    public class Feature {
        private final Panel panel;
        public final Channel channel;
        public final int featureNum;
        public final int inx;
        public final int mask;
        public Boolean onOff;
        public boolean pendingResponse;
        public int nameInx;
        public int funcInx;

        public Feature(Panel panel, Channel channel, int featureNum) {
            this.panel = panel;
            this.channel = channel;
            this.featureNum = featureNum;
            int i = 10 + featureNum - 1;
            this.inx = 2 + (i / 8); // The byte index where the status is found
            int mask = 1 << i; // The bit for the mask, always in the lower order byte
            while (mask >= 0x100) {
                mask = mask >> 8;
            }
            this.mask = mask;
            this.onOff = null;
            this.pendingResponse = false;
            this.nameInx = 0;
            this.funcInx = 0;
        }

        public String getName() {
            return panel.getCircuitInxName(nameInx);
        }

        public String getFunction() {
            return Const.CIRCUIT_FUNCTIONS[funcInx];
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

    public static long calcClockDiff(int hours, int minutes) {
        return calcClockDiff(hours, minutes, Calendar.getInstance());
    }

    public static long calcClockDiff(int hours, int minutes, Calendar now) {
        long msgTimeSecs = hours * 3600 + minutes * 60;
        long currentTimeSecs = (now.getTimeInMillis() + Const.TIMEZONE_RAW_OFFSET_MILLIS) % Const.MILLIS_PER_DAY / 1000;
        return currentTimeSecs - msgTimeSecs;
    }

    public class ClockDrift {

        public EasyTouchHandler handler;
        public MessageFactory msgFactory;
        public Channel clockDriftChannel;
        public Calendar driftLastSet;
        public long lastDriftSecs;

        public ClockDrift(EasyTouchHandler handler, MessageFactory msgFactory) {
            this.handler = handler;
            this.msgFactory = msgFactory;
            clockDriftChannel = handler.getThing().getChannel("clock-drift");
        }

        public void captureDrift(int hours, int minutes) {
            Calendar now = Calendar.getInstance();
            long drift = calcClockDiff(hours, minutes, now);
            if (driftLastSet == null) {
                // We are starting again. Initialize lastDriftSecs to whatever this one is.
                lastDriftSecs = drift;
                driftLastSet = now;
            }
            // Find the smallest drift ... even if negative (smallest will be the point when the panel minute changes)
            if (drift < lastDriftSecs) {
                lastDriftSecs = drift;
                driftLastSet = now;
            }
            long nowInMillis = now.getTimeInMillis();
            // If it has been 10 minutes since we last captured a minimum value, publish it and start over
            if ((nowInMillis - driftLastSet.getTimeInMillis()) > Const.TEN_MINUTES) {
                State state = new DecimalType(lastDriftSecs);
                handler.updateState(clockDriftChannel, state);
                driftLastSet = null;

                // ReSync if needed
                if (handler.getMaxClockDriftSecs() > 0 && (lastDriftSecs > handler.getMaxClockDriftSecs()
                        || lastDriftSecs + handler.getMaxClockDriftSecs() < 0)) {
                    // Adjust to local time
                    nowInMillis = (nowInMillis + Const.TIMEZONE_RAW_OFFSET_MILLIS) % Const.MILLIS_PER_DAY;
                    logger.info("nowInMillis {}, {}, {}", nowInMillis, handler.getClockResyncStartTimeMillis(),
                            handler.getClockResyncEndTimeMillis());
                    // ReSync if safe
                    if (nowInMillis >= handler.getClockResyncStartTimeMillis()
                            && nowInMillis < handler.getClockResyncEndTimeMillis()) {

                        // Set the clock up to 2 minutes from now. 2 rather than 1 so that bias can be added later.
                        Calendar newTime = (Calendar) now.clone();
                        newTime.add(Calendar.MINUTE, 2);
                        newTime.set(Calendar.SECOND, 0);
                        newTime.set(Calendar.MILLISECOND, 0);
                        newTime.add(Calendar.SECOND, handler.getClockSetBiasSeconds());
                        synchronized (this) {
                            nextTimeToSetClock = (Calendar) newTime.clone();
                        }
                        logger.info("Clock drift of {} exceeds +/-{}, will reset clock", lastDriftSecs,
                                handler.getMaxClockDriftSecs());
                    }
                }
            }
        }
    }

    private String[] custNames = { "USERNAME-01", "USERNAME-02", "USERNAME-03", "USERNAME-04", "USERNAME-05",
            "USERNAME-06", "USERNAME-07", "USERNAME-08", "USERNAME-09", "USERNAME-10" };
    private Circuit[] circuits;
    private Feature[] features;
    private Pump[] pumps;
    private Channel airTempChannel;
    private Channel poolTempChannel;
    private Channel spaTempChannel;
    private ClockDrift clockDrift;
    private int airTemp = -999;
    private int poolTemp = -999;
    private int spaTemp = -999;
    private MessageFactory msgFactory;

    public Panel(EasyTouchHandler handler) {
        this.m_handler = handler;
        airTempChannel = handler.getThing().getChannel("temp-airtemp");
        poolTempChannel = handler.getThing().getChannel("temp-pooltemp");
        spaTempChannel = handler.getThing().getChannel("temp-spatemp");
        msgFactory = new MessageFactory(handler);
        clockDrift = new ClockDrift(handler, msgFactory);
        circuits = new Circuit[10];
        for (int i = 1; i <= 10; i++) {
            Channel channel = handler.getThing().getChannel("equipment-circuit" + i);
            circuits[i - 1] = new Circuit(this, channel, i);
        }
        features = new Feature[10];
        for (int i = 1; i <= 10; i++) {
            Channel channel = handler.getThing().getChannel("equipment-feature" + i);
            features[i - 1] = new Feature(this, channel, i);
        }
        pumps = new Pump[8];
        for (int i = 1; i <= 8; i++) {
            Channel pumpChannel = handler.getThing().getChannel("pumps-pump" + i);
            Channel wattsChannel = handler.getThing().getChannel("watts-pump" + i);
            Channel rpmsChannel = handler.getThing().getChannel("rpms-pump" + i);
            pumps[i - 1] = new Pump(pumpChannel, wattsChannel, rpmsChannel, i);
        }
    }

    public Channel getCircuitFeatureChannel(int circuitNum) {
        if (circuitNum <= 10) {
            return circuits[circuitNum - 1].channel;
        } else {
            return features[circuitNum - 11].channel;
        }
    }

    public String getCircuitFeatureName(int circuitNum) {
        if (circuitNum <= 10) {
            return circuits[circuitNum - 1].getName();
        } else {
            return features[circuitNum - 11].getName();
        }
    }

    public String getCircuitFeatureFunction(int circuitNum) {
        if (circuitNum <= 10) {
            return circuits[circuitNum - 1].getFunction();
        } else {
            return features[circuitNum - 11].getFunction();
        }
    }

    public void consumeCircuitDef(Message msg) {
        int circuitNum = msg.payload[0];
        if (circuitNum <= 10) {
            Circuit circuit = circuits[circuitNum];
            circuit.nameInx = msg.payload[2];
            circuit.funcInx = msg.payload[1] & 0x0F;
        } else {
            Feature feature = features[circuitNum - 11];
            feature.nameInx = msg.payload[2];
            feature.funcInx = msg.payload[1] & 0x0F;
        }
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

    public void consumeCustomName(Message msg) {
        int inx = msg.payload[0];
        if (inx <= 9) {
            this.custNames[inx] = msg.parseCustomName();
        }
    }

    public String getCircuitInxName(int nameInx) {
        if (nameInx < 200) {
            return Utils.getCircuitName(nameInx);
        } else if (nameInx < 210) {
            return this.custNames[nameInx - 200];
        } else {
            return "Unknown Name 0x" + Utils.getByteStr(nameInx);
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
            if (logger.isTraceEnabled()) {
                logger.trace("PanelStatus {} {}-{} ({}) = {}", c.channel.getChannelTypeUID().getAsString(), c.inx,
                        Utils.getByteStr(c.mask), this.m_handler.getItemNames(c.channel), onOff);
            }
            if (c.onOff == null || c.onOff != onOff) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} ({}) = {}", c.channel.getChannelTypeUID().getAsString(),
                            m_handler.getItemNames(c.channel), onOff);
                }
                c.onOff = onOff;
                State state = onOff ? OnOffType.ON : OnOffType.OFF;
                m_handler.updateState(c.channel, state);
            }
        }

        for (Feature f : features) {
            boolean onOff = (payload[f.inx] & f.mask) != 0;
            if (logger.isTraceEnabled()) {
                logger.trace("PanelStatus {} {}-{} ({}) = {}", f.channel.getChannelTypeUID().getAsString(), f.inx,
                        Utils.getByteStr(f.mask), m_handler.getItemNames(f.channel), onOff);
            }
            if (f.onOff == null || f.onOff != onOff) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} ({}) = {}", f.channel.getChannelTypeUID().getAsString(),
                            m_handler.getItemNames(f.channel), onOff);
                }
                f.onOff = onOff;
                State state = onOff ? OnOffType.ON : OnOffType.OFF;
                m_handler.updateState(f.channel, state);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("PanelStatus: {} ({}) = {}", airTempChannel.getChannelTypeUID().getAsString(),
                    m_handler.getItemNames(airTempChannel), payload[18]);
        }
        int value = payload[14];
        if (value != poolTemp) {
            Circuit poolCircuit = circuits[5]; // Get Pool Circuit ... TODO: needs to be dynamic
            if (poolCircuit.onOff) { // Get Pool Circuit state ... TODO: needs to be dynamic
                poolTemp = payload[14];
                State state = new DecimalType(poolTemp);
                m_handler.updateState(poolTempChannel, state);
            }
        }
        value = payload[15];
        if (value != spaTemp) {
            Circuit spaCircuit = circuits[0]; // Get Spa Circuit ... TODO: needs to be dynamic
            if (spaCircuit.onOff) { // Get Spa Circuit state ... TODO: needs to be dynamic
                spaTemp = payload[15];
                State state = new DecimalType(spaTemp);
                m_handler.updateState(spaTempChannel, state);
            }
        }
        value = payload[18];
        if (value != airTemp) {
            airTemp = payload[18];
            State state = new DecimalType(airTemp);
            m_handler.updateState(airTempChannel, state);
        }
        clockDrift.captureDrift(payload[0], payload[1]);
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        if (OnOffType.class.isInstance(command)) {
            OnOffType onOff = (OnOffType) command;
            String cUID = channelUID.getId();
            if (cUID.startsWith("equipment-circuit")) {
                int circuitNum = Integer.parseInt(cUID.substring(17));
                Message onOffCommandMsg = msgFactory.makeOnOffCommand(circuitNum, onOff);
                if (logger.isTraceEnabled()) {
                    logger.trace("Circuit OnOffCommand: {}", Utils.formatCommandBytes(onOffCommandMsg));
                }
                Message onOffAck = msgFactory.makeOnOffAck();
                m_handler.write(onOffCommandMsg, onOffAck);
            } else if (cUID.startsWith("equipment-feature")) {
                int featureNum = Integer.parseInt(cUID.substring(17)) + 10;
                Message onOffCommandMsg = msgFactory.makeOnOffCommand(featureNum, onOff);
                if (logger.isTraceEnabled()) {
                    logger.trace("Feature OnOffCommand: {}", Utils.formatCommandBytes(onOffCommandMsg));
                }
                Message onOffAck = msgFactory.makeOnOffAck();
                m_handler.write(onOffCommandMsg, onOffAck);
            } else if (cUID.contentEquals("log-messages")) {
                m_handler.getMsgLog().setEnabled(onOff == OnOffType.ON);
            }
        }
    }

    public void handleAcknowledgement(Message msg) {
        m_handler.handleAcknowledgement(msg);
    }

    public void writeNewTime() {
        Calendar nextTimeToSet = nextTimeToSetClock;
        if (nextTimeToSet != null) {
            if (Calendar.getInstance().compareTo(nextTimeToSet) >= 0) {
                synchronized (this) {
                    // Undo the bias that was added in earlier. Subtract it out to get back to the whole minute.
                    nextTimeToSet.add(Calendar.SECOND, -m_handler.getClockSetBiasSeconds());
                    Message msgSetClockAck = msgFactory.makeSetDateTimeAck();
                    Message msgSetClock = msgFactory.makeSetDateTime(nextTimeToSet);
                    if (logger.isInfoEnabled()) {
                        logger.info("Set Clock: {}", Utils.formatCommandBytes(msgSetClock));
                    }
                    m_handler.write(msgSetClock, msgSetClockAck);
                    nextTimeToSetClock = null;
                }
            }
        }
    }

    public void logMsg(Message msg) {
        m_handler.getMsgLog().logMsg(msg, this);
    }

}
