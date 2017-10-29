package org.openhab.binding.pentair.easytouch.internal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import org.openhab.binding.pentair.easytouch.config.EasyTouchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySqlLogger {

    private static final Logger logger = LoggerFactory.getLogger(MySqlLogger.class);

    private boolean initialized = false;
    private boolean enabled = false;

    private String driverClass = "com.mysql.jdbc.Driver";
    private String url; // = "jdbc:mysql://127.0.0.1:3306/OpenHAB";
    private String user; // = "openhab";
    private String password; // = "MySQLOpenHab";

    // Error counter - used to reconnect to database on error
    private int errCnt;
    private int errReconnectThreshold = 1;

    private int waitTimeout = -1;

    private Connection connection = null;

    public MySqlLogger(EasyTouchConfig config) {
        url = config.mySqlUrl;
        user = config.mySqlUser;
        password = config.mySqlPassword;
        connectToDatabase();
        initialized = true;
    }

    /**
     * Checks if we have a database connection
     *
     * @return true if connection has been established, false otherwise
     */
    private boolean isConnected() {
        // Check if connection is valid
        try {
            if (connection != null && !connection.isValid(5000)) {
                errCnt++;
                logger.error("MySqlLogger: Connection is not valid!");
            }
        } catch (SQLException e) {
            errCnt++;

            logger.error("MySqlLogger: Error while checking connection: {}", e);
        }

        // Error check. If we have 'errReconnectThreshold' errors in a row, then
        // reconnect to the database
        if (errReconnectThreshold != 0 && errCnt >= errReconnectThreshold) {
            logger.error("MySqlLogger: Error count exceeded {}. Disconnecting database.", errReconnectThreshold);
            disconnectFromDatabase();
        }
        return connection != null;
    }

    /**
     * Connects to the database
     */
    private void connectToDatabase() {
        if (url == null || user == null) {
            return;
        }
        try {
            // Reset the error counter
            errCnt = 0;

            logger.debug("MySqlLogger: Attempting to connect to database {}", url);
            Class.forName(driverClass).newInstance();
            connection = DriverManager.getConnection(url, user, password);
            logger.debug("MySqlLogger: Connected to database {}", url);

            String cmd = "CREATE TABLE IF NOT EXISTS PentairRawLog (Id int NOT NULL AUTO_INCREMENT, "
                    + "Time DATETIME NOT NULL, RawHeader varchar(100), SRC char(2), Source varchar(20), DST char(2), Dest varchar(20), "
                    + "CMD char(2), Command varchar(30), Len int, Payload varchar(200), Interpretation varchar(10000), "
                    + "PanelTime char(5), DriftSecs int, PRIMARY KEY(Id));";
            Statement st = connection.createStatement();
            st.executeUpdate(cmd);
            st.close();

            if (waitTimeout != -1) {
                logger.debug("MySqlLogger: Setting wait_timeout to {} seconds.", waitTimeout);
                st = connection.createStatement();
                st.executeUpdate("SET SESSION wait_timeout=" + waitTimeout);
                st.close();
            }
        } catch (Exception e) {
            logger.error(
                    "MySqlLogger: Failed connecting to the SQL database using: driverClass={}, url={}, user={}, password={}",
                    driverClass, url, user, password, e);
        }
    }

    /**
     * Disconnects from the database
     */
    private void disconnectFromDatabase() {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("MySqlLogger: Disconnected from database {}", url);
            } catch (Exception e) {
                logger.error("MySqlLogger: Failed disconnecting from the SQL database {}", e);
            }
            connection = null;
        }
    }

    public void dispose() {
        logger.debug("Handler dispose on thread {}", java.lang.Thread.currentThread().getId());
        disconnectFromDatabase();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        logEnabled(enabled);
        if (enabled) {
            if (!isConnected()) {
                connectToDatabase();
            }
        } else {
            disconnectFromDatabase();
        }
    }

    public void logEnabled(boolean enabled) {
        if (!initialized) {
            return;
        }

        // Connect to mySQL server if we're not already connected
        if (!isConnected()) {
            connectToDatabase();
        }

        // If we still didn't manage to connect, then return!
        if (!isConnected()) {
            logger.warn(
                    "mySQL: No connection to database. Can not persist message!  "
                            + "Will retry connecting to database when error count:{} equals errReconnectThreshold:{}",
                    errCnt, errReconnectThreshold);
            return;
        }

        String cmd = "INSERT INTO PentairRawLog(Time, Source, Command) VALUES(?,?,?);";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(cmd);
            statement.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            statement.setString(2, "MsgLog Channel");
            statement.setString(3, enabled ? "Enable Logging" : "Disable Logging");

            statement.executeUpdate();

            // Success
            errCnt = 0;
        } catch (Exception e) {
            errCnt++;

            logger.error("MySqlLogger: Could not store message in database with " + "statement '{}': {}", cmd,
                    e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception hidden) {
                }
            }
        }
    }

    public void logMsg(Message msg, Panel panel) {
        if (!initialized || !enabled) {
            return;
        }

        // Connect to mySQL server if we're not already connected
        if (!isConnected()) {
            connectToDatabase();
        }

        // If we still didn't manage to connect, then return!
        if (!isConnected()) {
            logger.warn(
                    "mySQL: No connection to database. Can not persist message!  "
                            + "Will retry connecting to database when error count:{} equals errReconnectThreshold:{}",
                    errCnt, errReconnectThreshold);
            return;
        }

        String cmd = "INSERT INTO PentairRawLog(Time, RawHeader, SRC, Source, DST, Dest, CMD, Command, Len, Payload, Interpretation, PanelTime, DriftSecs) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?);";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(cmd);
            statement.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            statement.setString(2, msg.getHeaderByteStr());
            statement.setString(3, Utils.getByteStr(msg.source));
            statement.setString(4, msg.getSourceStr());
            statement.setString(5, Utils.getByteStr(msg.dest));
            statement.setString(6, msg.getDestStr());
            statement.setString(7, Utils.getByteStr(msg.cmd));
            statement.setString(8, msg.getCommandStr());
            statement.setInt(9, msg.getPayloadLength());
            statement.setString(10, msg.getPayloadByteStr());
            statement.setString(11, msg.getInterpretationStr(panel));
            statement.setString(12, msg.getMsgTime());
            statement.setObject(13, msg.getClockDrift(), Types.INTEGER);

            statement.executeUpdate();

            // Success
            errCnt = 0;
        } catch (Exception e) {
            errCnt++;

            logger.error("MySqlLogger: Could not store message in database with " + "statement '{}': {}", cmd,
                    e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception hidden) {
                }
            }
        }
    }

}
