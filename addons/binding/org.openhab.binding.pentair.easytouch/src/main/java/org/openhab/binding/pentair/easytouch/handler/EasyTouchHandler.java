/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.easytouch.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.pentair.easytouch.BindingConstants;
import org.openhab.binding.pentair.easytouch.config.EasyTouchConfig;
import org.openhab.binding.pentair.easytouch.internal.MessageReader;
import org.openhab.binding.pentair.easytouch.internal.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXPort;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link Pentair.EasyTouchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bill Baird - Initial contribution
 */
public class EasyTouchHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(EasyTouchHandler.class);

    /**
     * Our poll rate
     */
    int pollMilliSeconds;

    /**
     * is our config correct
     */
    private boolean properlyConfigured;

    private byte binderAddress;
    private Byte protocolAdapterAddress;

    private Panel panel;
    private String m_portName = "<no port configured>";
    private int m_baud = 9600;
    private CommPortIdentifier m_portId = null;
    private RXTXPort m_port;
    private MessageReader m_messageReader;
    private InputStream m_inStream;
    private OutputStream m_outStream;

    public EasyTouchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand: {}, Command: {}", channelUID, command);
        panel.handleCommand(channelUID, command);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.INITIALIZING);
        logger.debug("Handler initialize on thread {}", java.lang.Thread.currentThread().getId());
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        // updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
        configure();
        panel = new Panel(this);
        if (properlyConfigured) {
            openPort();
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler dispose on thread {}", java.lang.Thread.currentThread().getId());
        try {
            if (m_inStream != null) {
                m_inStream.close();
            }
            if (m_outStream != null) {
                m_outStream.close();
            }
        } catch (IOException e) {
            logger.error("Can't close the input and output streams properly", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot properly close the input and output streams.");
        } finally {
            if (m_port != null) {
                m_port.close();
            }
            m_messageReader = null;
            m_inStream = null;
            m_outStream = null;
            m_port = null;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // clear our cached values so the new channel gets updated
        logger.debug("Channel Linked on thread {}", java.lang.Thread.currentThread().getId());
        logger.debug("Channel Linked:", channelUID.toString());
        // clearState(true);
    }

    /**
     * Configures this thing
     */
    private void configure() {
        properlyConfigured = false;

        EasyTouchConfig configuration = getConfig().as(EasyTouchConfig.class);
        try {
            m_portName = configuration.usbPort;
        } catch (Exception e) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            logger.error("Could not configure PentairEasytouch instance", e);
        }

        binderAddress = BindingConstants.DEFAULT_BINDER_ADDRESS;
        try {
            if (configuration.binderAddress != null && configuration.binderAddress.startsWith("0x")) {
                binderAddress = (byte) Integer.parseInt(configuration.binderAddress.substring(2), 16);
            } else {
                binderAddress = (byte) Integer.parseInt(configuration.binderAddress);
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            logger.error("Could not configure PentairEasytouch instance.  Unable to parse binderAddress.", e);
        }
        if (binderAddress < BindingConstants.MIN_REMOTE_ADDRESS
                || binderAddress > BindingConstants.MAX_REMOTE_ADDRESS) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR,
                    "binderAddress must be between 0x20 and 0x22 inclusive.");
            logger.error("Could not configure PentairEasytouch instance.  Unable to set binderAddress.",
                    "binderAddress must be between 0x20 and 0x22 inclusive.");
        }

        protocolAdapterAddress = BindingConstants.DEFAULT_PROTOCOL_ADAPTER_ADDRESS;
        try {
            if (configuration.protocolAdapterAddress == null || configuration.protocolAdapterAddress == "") {
                protocolAdapterAddress = BindingConstants.DEFAULT_PROTOCOL_ADAPTER_ADDRESS;
            } else if (configuration.protocolAdapterAddress.toLowerCase() == "null") {
                protocolAdapterAddress = null;
            } else if (configuration.protocolAdapterAddress.startsWith("0x")) {
                protocolAdapterAddress = (byte) Integer.parseInt(configuration.protocolAdapterAddress.substring(2), 16);
            } else {
                protocolAdapterAddress = (byte) Integer.parseInt(configuration.protocolAdapterAddress);
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            logger.error("Could not configure PentairEasytouch instance.  Unable to parse protocolAdapterAddress.", e);
        }
        if (protocolAdapterAddress != null && (protocolAdapterAddress < BindingConstants.MIN_REMOTE_ADDRESS
                || protocolAdapterAddress > BindingConstants.MAX_REMOTE_ADDRESS)) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR,
                    "protocolAdapterAddress must be between 0x20 and 0x22 inclusive.");
            logger.error("Could not configure PentairEasytouch instance.  Unable to set protocolAdapterAddress.",
                    "protocolAdapterAddress must be between 0x20 and 0x22 inclusive.");
        }

        try {
            Integer _pollMilliSeconds = configuration.pollMilliSeconds;
            pollMilliSeconds = 5000;

            if (_pollMilliSeconds != null) {
                pollMilliSeconds = _pollMilliSeconds.intValue();
            }

            logger.debug("Pentair binding configured on USB {}, polling every {} milliseconds.", m_portName,
                    pollMilliSeconds);
            properlyConfigured = true;
        } catch (Exception e) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            logger.error("Could not configure PentairEasytouch instance", e);
        }
    }

    /**
     * Initialize this device and open the serial port
     *
     * @throws InitializationException if port can not be opened
     */
    @SuppressWarnings("rawtypes")
    public void openPort() {
        try {
            // Handle SYMLINK port specification if it exists (for Linux systems), since the port may change on a
            // reboot.
            // Rather than setting "gnu.io.rxtx.SerialPorts", which is what other bindings do and is not threadsafe,
            // resolve to the port's canonical (physical) path (which could change on a reboot) and use it instead.
            File f = new File(m_portName);
            String cannonicalPortName = f.getCanonicalPath();
            logger.debug("{} resolved to {}", m_portName, cannonicalPortName);
            m_portId = CommPortIdentifier.getPortIdentifier(cannonicalPortName);
            logger.info("Configured serial port '{}' has been found as {}.", m_portName, cannonicalPortName);

            m_port = m_portId.open("PentairEasytouch Binding", 2000);
            m_port.setSerialPortParams(m_baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            m_port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            m_inStream = m_port.getInputStream();
            m_outStream = m_port.getOutputStream();
            m_messageReader = new MessageReader(panel, m_inStream);
            m_port.addEventListener(m_messageReader);
            m_port.clearCommInput();
            m_port.notifyOnDataAvailable(true);

            updateStatus(ThingStatus.ONLINE);
        } catch (PortInUseException e) {
            logger.error("cannot open serial port: {}, it is in use!", m_portName);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cannot open " + m_portName + ": Port already in use.");
        } catch (UnsupportedCommOperationException e) {
            logger.error("Unsupported operation {} on port {}", e.getMessage(), m_portName);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unsupported operation " + e.getMessage() + " on port " + m_portName);
        } catch (TooManyListenersException e) {
            logger.error("setSerialEventHandler(): Too Many Listeners Exception: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Too Many Listeners Exception: " + e.getMessage() + " on port " + m_portName);
        } catch (NoSuchPortException e) {
            logger.error("got no such port for {}", m_portName);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Port " + m_portName + " not found");
        } catch (IOException e) {
            logger.error("error got IOException for {}: {}", m_portName, e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "IOException on port " + m_portName + ": " + e.getMessage());
        }
    }

    public void updateState(Channel channel, State state) {
        super.updateState(channel.getUID(), state);
    }

    /**
     * Write data to iostream
     *
     * @param b byte array to write
     */
    public void write(byte[] b) {
        try {
            m_outStream.write(b);
        } catch (IOException e) {
            logger.trace("got exception while writing: {}", e.getMessage());
            // while (!reconnect()) {
            // try {
            // logger.trace("sleeping before reconnecting");
            // Thread.sleep(10000);
            // } catch (InterruptedException ie) {
            // logger.warn("interrupted while sleeping on write reconnect");
            // }
            // }
        }
    }

    public byte getBinderAddress() {
        return binderAddress;
    }

    public Byte getProtocolAdapterAddress() {
        return protocolAdapterAddress;
    }

}
