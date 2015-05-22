package tennox.planetoid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TimeAnalyzer {
	private static TreeMap<String, Long> all = new TreeMap<String, Long>();
	private static HashMap<String, Long> count = new HashMap<String, Long>();
	private static HashMap<String, Long> start = new HashMap<String, Long>();
	private static HashMap<String, Long> max = new HashMap<String, Long>();

	private static Lock lock = new ReentrantLock();

	private static String current = "";

	public static void start(String s2) {
		lock.lock();

		if (current.contains(s2)) {
			throw new RuntimeException("'" + s2 + "' already started? (" + current + ")");
		}

		current = current + "." + s2;
		String s = current;
		// System.out.println("Starting: " + s);

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
		start.put(s, Long.valueOf(System.nanoTime()));
		lock.unlock();
	}

	public static void end(String sub) {
		lock.lock();

		String s = current;
		int last = current.lastIndexOf('.');
		if (!sub.equals(current.substring(last + 1))) {
			throw new RuntimeException("Trying to end '" + sub + "', but current section is '" + current.substring(last + 1) + "'");
		}
		current = last > 0 ? current.substring(0, last) : "";
		// System.out.println("Ending: " + s);

		if (!start.containsKey(s)) {
			lock.unlock();
			return;
		}
		long t = System.nanoTime() - ((Long) start.get(s)).longValue();
		if (!count.containsKey(s))
			count.put(s, Long.valueOf(1L));
		long a = ((Long) all.get(s)).longValue();
		all.remove(s);
		all.put(s, Long.valueOf(a + t));
		if (!max.containsKey(s) || max.get(s) < t)
			max.put(s, t);
		start.remove(s);
		if (t > 1000000000) {
			System.out.println("Section " + s + " took over " + t + "ns!");
		}
		lock.unlock();
	}

	public static void endStart(String end, String start) {
		end(end);
		start(start);
	}

	public static void print() {
		if (all.size() == 0) {
			System.out.println("----TimeAnalyzer EMPTY----");
			return;
		}
		System.out.println("++++TimeAnalyzer++++");

		String[][] table = new String[all.size() + 1][5]; // rows(+header), columns
		table[0] = new String[] { "Section", "Total[ms]", "Count", "Max[us]", "Average[us]" }; // Table header
		String[] sections = all.keySet().toArray(new String[all.size()]);
		String s;
		for (int i = 0; i < sections.length; i++) {
			s = sections[i];
			table[i + 1][0] = sections[i];
			table[i + 1][1] = intstr(all.get(s) / 1000000).toString();
			table[i + 1][2] = intstr(count.get(s));
			table[i + 1][3] = intstr(max.get(s) / 1000);
			table[i + 1][4] = String.format("%.2f", getAverage(s) / 1000);
		}

		printTable(table);
		// System.out.println("TA: Section=" + s + "\tTime=" + all.get(s) + "ms\tCount=" + count.get(s) + "\tMax=" + max.get(s) + "ms\tAverage=" +
		// getAverage(s) + "ms");

		System.out.println("----TimeAnalyzer----");
	}

	private static String intstr(Long l) {
		return l != null ? Long.toString(l) : "";
	}

	/** Prints table[rows][columns] **/
	private static void printTable(String[][] table) {
		int[] len = new int[table[0].length]; // stores the max content length per column

		String c;
		for (int col = 0; col < len.length; col++) {
			for (int row = 0; row < table.length; row++) {
				c = table[row][col];
				if (c.length() > 40) {
					throw new RuntimeException("'" + c + "' is too long");
				}
				if (c != null && c.length() > len[col])
					len[col] = c.length();
			}
		}

		for (int row = 0; row < table.length; row++) {
			for (int col = 0; col < len.length; col++) {
				System.out.printf("%-" + (len[col] + 1 + "s"), table[row][col]); // print max content length +1
			}
			System.out.println();
		}
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

	public static Double getAverage(String s) {
		double a = ((Long) all.get(s)).longValue();
		double c = ((Long) count.get(s)).longValue();
		return a / c;
	}
}