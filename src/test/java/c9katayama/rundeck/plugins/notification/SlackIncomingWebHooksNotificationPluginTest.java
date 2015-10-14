package c9katayama.rundeck.plugins.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

import c9katayama.rundeck.plugins.notification.SlackIncomingWebHooksNotificationPlugin.Trigger;

public class SlackIncomingWebHooksNotificationPluginTest {

	@Test
	public void testValidJson() {

		SlackIncomingWebHooksNotificationPlugin plugin = new SlackIncomingWebHooksNotificationPlugin();
		Map executionData = new HashMap();
		executionData.put("id", "100");
		executionData.put("status", "succeeded");
		Map jobdata = new HashMap();
		jobdata.put("name", "MyJob");
		executionData.put("job", jobdata);
		String json = plugin.createText(Trigger.success, executionData);

		Gson gson = new Gson();
		Map<String, Object> result = gson.fromJson(json, new HashMap<String, Object>().getClass());
		assertEquals("#008000", result.get("color").toString());
		assertTrue(result.get("text").toString().contains("succeeded"));
	}

}
