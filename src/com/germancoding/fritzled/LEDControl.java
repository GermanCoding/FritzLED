package com.germancoding.fritzled;

import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

public class LEDControl {

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Usage: java -jar LEDControl.jar <on/off> <domain> [username] <password>");
			System.out.println("[username] is optional, default configuration only uses password.");
			System.out.println("If the web interface doesn't have a password set, any <password> given here will be valid.");
			System.out.println("Domain can be a hostname or an IP address. It's also possible to specify an entire URL like http://fritz.repeater/. " + "HTTPS will only work if a valid certificate is used (non-default).");
			return;
		}

		boolean on;
		if ("on".equalsIgnoreCase(args[0])) {
			on = true;
		} else {
			on = false;
		}

		String domain = args[1];
		String password;
		String username;

		if (args.length > 3) {
			username = args[2];
			password = args[3];
		} else {
			password = args[2];
			username = null;
		}

		try {
			FritzBoxController control = new FritzBoxController(domain, username, password);
			control.setLEDStatus(on);
		} catch (ParserConfigurationException | URISyntaxException e) {
			System.err.println("An error occured: " + e);
			e.printStackTrace();
		}
	}

}
