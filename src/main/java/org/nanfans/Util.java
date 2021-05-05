package org.nanfans;

import java.util.Calendar;

public class Util {
	public static long getSec() {
		long current = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long tomorrowzero = calendar.getTimeInMillis();
		long tomorrowzeroSeconds = (tomorrowzero- current) / 1000;
		return tomorrowzeroSeconds;
	}
}
