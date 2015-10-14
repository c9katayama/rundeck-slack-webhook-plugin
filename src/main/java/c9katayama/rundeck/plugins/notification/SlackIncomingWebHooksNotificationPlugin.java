package c9katayama.rundeck.plugins.notification;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;

@Plugin(service = "Notification", name = "SlackIncomingWebHooksNotification")
@PluginDescription(title = "SlackIncomingWebHooksNotification", description = "Send notification messages to Slack by incoming web hook.")
public class SlackIncomingWebHooksNotificationPlugin implements NotificationPlugin {

	@PluginProperty(name = "webhookUrl", title = "Webhook URL", description = "webhook url for slack incoming webhooks", required = true)
	protected String webhookUrl;
	@PluginProperty(name = "username", title = "Username", description = "Username of notification message.(default:Rundeck Job Notification)")
	protected String username;
	@PluginProperty(name = "icon_emoji", title = "Icon emoji", description = "Message icon.")
	protected String icon_emoji;
	@PluginProperty(name = "channel", title = "Channel", description = "Channal to post notification.")
	protected String channel;
	@PluginProperty(name = "color", title = "Color", description = "Message color.")
	protected String color;
	@PluginProperty(name = "addTriggerSuffixToUserName", defaultValue = "true", title = "Add trigger suffix to user name", description = "add trigger(start,success,failure) to user name.")
	protected Boolean addTriggerSuffixToUserName;

	public static enum Trigger {
		start, success, failure
	}

	public static enum Status {
		succeeded, running, failed
	}

	protected static final String LF = "\n";

	@SuppressWarnings("rawtypes")
	@Override
	public boolean postNotification(String trigger, Map executionData, Map config) {
		if (hasValue(username) == false) {
			username = "Rundeck Job Notification";
		}
		if (addTriggerSuffixToUserName != null && addTriggerSuffixToUserName == true) {
			username = username + " [" + trigger + "]";
		}
		if (hasValue(icon_emoji) == false) {
			switch (Trigger.valueOf(trigger)) {
			case start:
				icon_emoji = ":smirk_cat:";
				break;
			case success:
				icon_emoji = ":smile_cat:";
				break;
			case failure:
				icon_emoji = ":scream_cat:";
				break;
			}
		}

		String text = createText(Trigger.valueOf(trigger), executionData);
		String json = toMessageJson(text);
		send(json);
		return true;
	}

	protected String toMessageJson(String text) {
		StringBuffer buf = new StringBuffer();
		buf.append("{");
		buf.append("\"username\":\"" + username + "\",");
		buf.append("\"icon_emoji\":\"" + icon_emoji + "\",");
		if (hasValue(channel)) {
			buf.append("\"channel\":\"" + channel + "\",");
		}
		buf.append("\"attachments\":[" + text + "]}");
		return buf.toString();
	}

	@SuppressWarnings("rawtypes")
	protected String createText(Trigger trigger, Map executionData) {
		final Map jobdata = (Map) executionData.get("job");
		final String jobname = jobdata.get("name").toString();
		final String id = "#" + executionData.get("id");
		final Status status = Status.valueOf(executionData.get("status").toString());
		if (hasValue(color) == false) {
			switch (status) {
			case succeeded:
				color = "#008000";
				break;
			case running:
				color = "#4682b4";
				break;
			case failed:
				color = "#ff0000";
				break;
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{\"pretext\":\"Rundeck [" + jobname + "] " + id + "\",");
		sb.append("\"color\":\"" + color + "\",");
		sb.append("\"text\":\"Job status : " + status + "\"}");
		return sb.toString();
	}

	protected URL toURL(String url) {
		try {
			return new URL(webhookUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	protected void send(String json) {
		URL url = toURL(webhookUrl);
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("charset", "utf-8");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(json);
			wr.flush();
			wr.close();
			InputStream is = connection.getInputStream();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				connection.disconnect();
			} catch (Exception e) {
			}
		}
	}

	protected boolean hasValue(String value) {
		if (value == null || value.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
}
