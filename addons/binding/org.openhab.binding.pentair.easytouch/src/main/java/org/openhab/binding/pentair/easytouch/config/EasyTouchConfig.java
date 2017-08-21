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

    /**
     * Max Seconds clock can drift before being synchronized
     */
    public Integer clockMaxDriftSeconds;

    /**
     * Seconds to bias when clock sets occur. This is capped at +/- clockMaxDriftSeconds - 2.
     */
    public Integer clockSetBiasSeconds;

    /**
     * Time of day after which it is safe to synchronize the clock, must be in range of 0 to 2359
     */
    public Integer safeToSyncStartTimeOfDay;

    /**
     * Time of day before which it is safe to synchronize the clock, must be in range of 0 to 2359
     */
    public Integer safeToSyncEndTimeOfDay;

    public String mySqlUrl;
    public String mySqlUser;
    public String mySqlPassword;

}
