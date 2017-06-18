* Allow openHab configuration to accept/parse hex constants as integers
* Verify constant values in error messages ... make them based off Constants class?
* Get Pump Status values from Panel.consumePanelStatusMessage
* Get Water Temp and last Timestamp from Panel.consumePanelStatusMessage
* Calculate TimeStamp drift - make read-only item property ... once working, make auto-update, but only when nothing is running and not near midnight.
* Think about what happens if Pentair is behind the pi.  Does the drift go from positive to negative every 60 seconds?
