package org.openhab.binding.pentair.easytouch.config;

public class EasyTouchConfig {

    /**
     * USB Port (RS-485) of the Pentair controller
     */
    public String usbPort;

    /**
     * RS485 device address of this binder
     */
    public String binderAddress;

    /**
     * RS485 device address of this binder
     */
    public String protocolAdapterAddress;

    /**
     * Rate we poll the RS-485 usb port
     */
    public Integer pollMilliSeconds;

}
