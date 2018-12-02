# FritzLED
A small program to turn the LEDs of a FRITZ!WLAN or FRITZ!Powerline product on or off, intended for automatic time control. For example, you could call this program via a cronjob to turn off your repeater LEDs every day at 22:00 and turn them on again at 07:00 (requires an always-online computer, like a raspberry pi). Useful if your repeater is placed in a bedroom or similar and you don't want the lights on at night. The repeater will still work normally, just without lights.

### Note: This program is not intended for use with a Fritz!Box. The program can only turn off the LEDs on products where this feature is supported (repeaters and powerline products from AVM). However, it has been reported that this program MAY work on some (older) Fritz!Box models.

# Usage
`java -jar <jarfile> <on/off> <domain> [username] <password>`

[username] is optional, the default configuration only uses a password.

If the web interface doesn't have a password set, any <password> given here will be valid.

Domain can be a hostname or an IP address. It's also possible to specify an entire URL like http://fritz.repeater/. HTTPS will only work if a valid certificate is used (non-default).

# Technical
This program uses functionality available in Fritz!OS web interface. The program logs in via the interface, just like any human, and turns the LEDs on or off with the feature available under `System -> LEDs`.

# Dependencies
FritzLED requires [Apache HttpClients](https://hc.apache.org/httpcomponents-client-ga/index.html) in a recent version and Java 7 or higher to compile.
There's currently no Gradle or Maven file, you will need to add the dependency manually.

# Download
A precompiled version of this program is currently not available, but I'm working on it.
