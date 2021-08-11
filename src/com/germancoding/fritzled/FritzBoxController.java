package com.germancoding.fritzled;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Document;

public class FritzBoxController {

	private DocumentBuilder builder;
	private CloseableHttpClient client;
	private Charset charset;

	private String scheme;
	private String domain;
	private String username;
	private String password;

	private String sid; // TODO: Periodic checks if SID still valid

	public FritzBoxController(String domain, String username, String password) throws ParserConfigurationException, URISyntaxException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		builder = documentBuilderFactory.newDocumentBuilder();
		client = HttpClients.createDefault();
		charset = Charset.forName("UTF-16LE");
		scheme = "http://";

		if (domain.toLowerCase().startsWith("http")) {
			URI uri = new URI(domain);
			scheme = uri.getScheme() + "://";
			domain = uri.getHost() + uri.getPath();
		}

		if (domain.endsWith("/")) {
			domain = domain.substring(0, domain.length() - 1);
		}

		this.domain = domain;
		this.username = username;
		this.password = password;
		this.sid = getNewSessionID();
		if (this.sid.equalsIgnoreCase("0000000000000000")) {
			System.out.println("Login failed. Username/password may be incorrect.");
		}
	}

	public String getNewSessionID() {
		String url = getLoginAddress();
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse httpResponse = null;

		try {
			httpResponse = client.execute(get);
			Document document = builder.parse(httpResponse.getEntity().getContent());
			int timeout = Integer.parseInt(document.getElementsByTagName("BlockTime").item(0).getTextContent());
			if (timeout > 0) {
				System.out.println("Brute force protection active, waiting " + timeout + " seconds");
				Thread.sleep(timeout * 1000l);
				return getNewSessionID();
			}

			String sid = document.getElementsByTagName("SID").item(0).getTextContent();
			if (sid.equalsIgnoreCase("0000000000000000")) { // Invalid SID; New login required (non-null if no password required)
				String challenge = document.getElementsByTagName("Challenge").item(0).getTextContent();
				String response = getResponse(challenge, password);

				URIBuilder uriBuilder = new URIBuilder(url);

				if (username != null)
					uriBuilder.addParameter("username", username);
				uriBuilder.addParameter("response", response);
				HttpGet finalGet = new HttpGet(uriBuilder.build());
				httpResponse.close();
				httpResponse = client.execute(finalGet);
				document = builder.parse(httpResponse.getEntity().getContent());
				sid = document.getElementsByTagName("SID").item(0).getTextContent();
				// System.out.println("new SID is " + sid);
			}

			return sid;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;

		} finally {
			if (httpResponse != null)
				try {
					httpResponse.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public void setLEDStatus(boolean on) {
		// System.out.println("LEDs to " + on);
		String url = getDataAddress();

		HttpPost post = new HttpPost(url);

		// We first need to check if the device is dimmable and/or has dynamic light sensors and if so, get the current values
		int dimValue = 2;
		int envLight = 0;
		ArrayList<NameValuePair> dimParams = new ArrayList<NameValuePair>();
		dimParams.add(new BasicNameValuePair("sid", sid));
		dimParams.add(new BasicNameValuePair("page", "led"));
		CloseableHttpResponse response = null;
		try {
			post.setEntity(new UrlEncodedFormEntity(dimParams));
			response = sendPost(post);
			JSONTokener tokener = new JSONTokener(response.getEntity().getContent());
			JSONObject responseObject = new JSONObject(tokener);
			JSONObject parent = responseObject.getJSONObject("data").getJSONObject("ledSettings");
			try {
				dimValue = parent.getInt("dimValue");
			} catch (JSONException e) {
				// Might be unsupported
			}
			try {
				envLight = parent.getInt("envLight");
			} catch (JSONException e) {
				// Might be unsupported
			}
		} catch (Exception e) {
			// Ignore, this may be an older Fritz!OS device
			;
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					;
				}
			}
		}

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("sid", sid));
		params.add(new BasicNameValuePair("apply", ""));
		params.add(new BasicNameValuePair("led_display", on ? "0" : "2"));
		params.add(new BasicNameValuePair("oldpage", "/system/led_display.lua"));
		params.add(new BasicNameValuePair("ledDisplay", on ? "0" : "2"));
		params.add(new BasicNameValuePair("dimValue", Integer.toString(dimValue)));
		params.add(new BasicNameValuePair("envLight", Integer.toString(envLight)));
		params.add(new BasicNameValuePair("page", "led"));
		try {
			post.setEntity(new UrlEncodedFormEntity(params));
			sendPost(post).close();
			// System.out.println("POST send with sid " + sid);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public CloseableHttpResponse sendPost(HttpPost post) throws ClientProtocolException, IOException {
		return client.execute(post);
	}

	public String getSID() {
		return sid;
	}

	private String getResponse(String challenge, String password) {
		try {
			String message = challenge + "-" + password;
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(message.getBytes(charset));
			return challenge + "-" + bytesToHex(hash);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Copied from https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
	private final static char[] hexArray = "0123456789abcdef".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public String getLoginAddress() {
		return scheme + domain + "/login_sid.lua";
	}

	public String getDataAddress() {
		return scheme + domain + "/data.lua";
	}

	public String getFirmwareCfgAddress() {
		return scheme + domain + "/cgi-bin/firmwarecfg";
	}

}
