package tennox.planetoid;

public class PlanetoidGeneratorInfo {

	boolean defaultGeneration;
	int min_radius, max_radius, spawntries;

	// FlatGeneratorInfo
	public static PlanetoidGeneratorInfo getDefaultGenerator() {
		PlanetoidGeneratorInfo info = new PlanetoidGeneratorInfo();
		info.defaultGeneration = true;
		info.min_radius = 5;
		info.max_radius = 15;
		info.spawntries = 15;

		return info;
	}

	public static PlanetoidGeneratorInfo createGeneratorFromString(String string) {
		PlanetoidGeneratorInfo info = getDefaultGenerator();
		if (string.length() == 0)
			return info;
		String[] arr = string.split(";");

		for (String str : arr) {
			String key = str.split("=")[0];
			String val = str.split("=")[1];

			if (key.equals("defgen")) {
				try {
					info.defaultGeneration = Integer.parseInt(val) == 1;
				} catch (NumberFormatException e) {
				}
			} else if (key.equals("minrad")) {
				try {
					Option.MIN_RADIUS.setValue(info, Integer.parseInt(val));
				} catch (NumberFormatException e) {
				}
			} else if (key.equals("maxrad")) {
				try {
					Option.MAX_RADIUS.setValue(info, Integer.parseInt(val));
				} catch (NumberFormatException e) {
				}
			} else if (key.equals("tries")) {
				try {
					Option.SPAWNTRIES.setValue(info, Integer.parseInt(val));
				} catch (NumberFormatException e) {
				}
			}
		}

		Option.checkValues(info);
		return info;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("v=").append(1);

		builder.append(";defgen=").append(defaultGeneration ? 1 : 0);
		builder.append(";minrad=").append(min_radius);
		builder.append(";maxrad=").append(max_radius);
		builder.append(";tries=").append(spawntries);

		return builder.toString();
	}

}