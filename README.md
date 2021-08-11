# FritzLED
A small program to turn the LEDs of a Fritz!Box, FRITZ!WLAN or FRITZ!Powerline product on or off, intended for automatic time control. For example, you could call this program via a cronjob to turn off your repeater LEDs every day at 22:00 and turn them on again at 07:00 (requires an always-online computer, like a raspberry pi). Useful if your repeater is placed in a bedroom or similar and you don't want the lights on at night. The repeater will still work normally, just without lights.

### Note: This program can only turn off the LEDs on products where this feature is supported (newer Fritz!OS versions of boxes, repeaters and powerline products from AVM). However, it has been reported that this program MAY work on some models running older versions of Fritz!OS. Confirmed working on most Fritz!OS 7+ models.

# Usage
`java -jar <jarfile> <on/off> <domain> [username] <password>`

[username] is optional, older Fritz!OS default is to only use a password.

**WARNING:**
On newer Fritz!OS versions, usernames are mandatory. Even the "password only" option now uses a (hidden) user. If you're running Fritz!OS 7.25 or higher, login into your Fritz!Box and check System -> Fritz!Box users. This will show the username ("fritzXXXX", where X are digits) you need for login.

If the web interface doesn't have a password set, any <password> given here will be valid.

Domain can be a hostname or an IP address. It's also possible to specify an entire URL like http://fritz.repeater/. HTTPS will only work if a valid certificate is used (non-default).

# Technical
This program uses functionality available in Fritz!OS web interface. The program logs in via the interface, just like any human, and turns the LEDs on or off with the feature available under `System -> LEDs`.

# Dependencies
There's a Maven file (pom.xml) included, which should automatically manage dependencies. 

If you do not use Maven:
FritzLED requires [Apache HttpClients](https://hc.apache.org/httpcomponents-client-ga/index.html), JSON (json.org) and Java 7 or higher to compile.

# Download
You can download an up to date pre-compiled version of this program here (Compiled with Java 8):
https://build.germancoding.com/job/FritzLED/lastSuccessfulBuild/artifact/target/FritzLED-0.0.1-SNAPSHOT-jar-with-dependencies.jar
