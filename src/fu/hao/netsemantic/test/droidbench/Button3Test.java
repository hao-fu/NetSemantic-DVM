package fu.hao.netsemantic.test.droidbench;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import android.telephony.TelephonyManager;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class Button3Test {

	@Test
	public void test() {
		TelephonyManager tm = new TelephonyManager();
		String[] args = new String[4];
		Settings.logLevel = 0;
		args[3] = "ATaint";
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Button3/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}

}
