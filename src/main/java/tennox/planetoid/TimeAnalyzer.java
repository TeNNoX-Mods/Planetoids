package tennox.planetoid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TimeAnalyzer {
	private static HashMap<String, Long> all = new HashMap();
	private static HashMap<String, Long> count = new HashMap();
	private static HashMap<String, Long> start = new HashMap();
	private static HashMap<String, Integer> max = new HashMap();

	static Lock lock = new ReentrantLock();

	public static void start(String s) {
		lock.lock();
		if (!all.containsKey(s)) {
			all.put(s, Long.valueOf(0L));
			count.put(s, Long.valueOf(1L));
		} else {
			if (!count.containsKey(s))
				count.put(s, Long.valueOf(1L));
			long co = ((Long) count.get(s)).longValue();
			count.remove(s);
			count.put(s, Long.valueOf(co + 1L));
		}
		start.put(s, Long.valueOf(System.currentTimeMillis()));
		lock.unlock();
	}

	public static void end(String s) {
		lock.lock();
		if (!start.containsKey(s)) {
			lock.unlock();
			return;
		}
		long t = System.currentTimeMillis() - ((Long) start.get(s)).longValue();
		if (!count.containsKey(s))
			count.put(s, Long.valueOf(1L));
		long a = ((Long) all.get(s)).longValue();
		all.remove(s);
		all.put(s, Long.valueOf(a + t));
		if (!max.containsKey(s) || max.get(s) < t)
			max.put(s, (int) t);
		start.remove(s);
		if (t > 1000) {
			System.out.println("Section " + s + " took over " + t + "ms!");
		}
		lock.unlock();
	}

	public static void print() {
		if (all.size() == 0) {
			System.out.println("----TimeAnalyzer EMPTY----");
			return;
		}
		System.out.println("----TimeAnalyzer----");
		Iterator iter = all.entrySet().iterator();
		while (iter.hasNext()) {
			String s = (String) ((Entry) iter.next()).getKey();
			System.out.println("TA: Section=" + s + "\tTime=" + all.get(s) + "ms\tCount=" + count.get(s) + "\tMax=" + max.get(s) + "ms\tAverage=" + getAverage(s) + "ms");
		}
		System.out.println("----TimeAnalyzer----");
	}

	public static void reset() {
		try {
			all.clear();
			count.clear();
			start.clear();
			max.clear();
		} catch (Exception e) {
			System.err.println("TA: wuaaat! " + e);
		}
	}

	public static double getAverage(String s) {
		double a = ((Long) all.get(s)).longValue();
		double c = ((Long) count.get(s)).longValue();
		return a / c;
	}
}