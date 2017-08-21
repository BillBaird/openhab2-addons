* Allow openHab configuration to accept/parse hex constants as integers
* Verify constant values in error messages ... make them based off Constants class?
* Get Pump Status values from Panel.consumePanelStatusMessage
* Get Water Temp and last Timestamp from Panel.consumePanelStatusMessage
* Calculate TimeStamp drift - make read-only item property ... once working, make auto-update, but only when nothing is running and not near midnight.
* Think about what happens if Pentair is behind the pi.  Does the drift go from positive to negative every 60 seconds?
* Make clocksync interval configurable in minutes?  10 minute default?  (This would be at the thing level)

* MySqlLogging is off when starting, even if it was when previously running.  However, item shows "on" if previously on.  Either have both show off, or properly handle preserved value and turn it on.

* Sync clock right at the 0:00 second mark to get most accurate?  Add config to bias + or minus a few seconds when setting?
