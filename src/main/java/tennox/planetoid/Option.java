package tennox.planetoid;

public enum Option {
	MIN_RADIUS("planetoid.gui.minradius", 1, 128),
	MAX_RADIUS("planetoid.gui.maxradius", 1, 128),
	SPAWNTRIES("planetoid.gui.spawntries", 1, 100);

	public static void defaults(PlanetoidGeneratorInfo info) {
		Option.MIN_RADIUS.setValue(info, 5);
		Option.MAX_RADIUS.setValue(info, 15);
		Option.SPAWNTRIES.setValue(info, 16);
	}

	public void updateValue(PlanetoidGeneratorInfo info, float sliderValue) {
		setValue(info, getRealValue(sliderValue));
	}

	public void setValue(PlanetoidGeneratorInfo info, int v) {
		int value = v;
		if (v < min)
			value = min;
		else if (v > max)
			value = max;

		switch (this) {
		case MIN_RADIUS:
			info.min_radius = value;
			break;
		case MAX_RADIUS:
			info.max_radius = value;
			break;
		case SPAWNTRIES:
			info.spawntries = value;
			break;
		}
	}

	public int getValue(PlanetoidGeneratorInfo info) {
		switch (this) {
		case MIN_RADIUS:
			return info.min_radius;
		case MAX_RADIUS:
			return info.max_radius;
		case SPAWNTRIES:
			return info.spawntries;
		default:
			return 0;
		}
	}

	public String getDisplayString(float sliderValue) {
		return Planetoid.translate(name) + ": " + getRealValue(sliderValue);
	}

	public int getRealValue(float sliderValue) {
		return min + (int) (Math.round(max - min) * sliderValue);
	}

	public float getSliderValue(PlanetoidGeneratorInfo info) {
		return (float) (getValue(info) - min) / ((float) max - min);
	}

	public static void checkValues(PlanetoidGeneratorInfo info) {
		if (MIN_RADIUS.getValue(info) > MAX_RADIUS.getValue(info))
			MIN_RADIUS.setValue(info, MAX_RADIUS.getValue(info));
		if (MAX_RADIUS.getValue(info) < MIN_RADIUS.getValue(info))
			MAX_RADIUS.setValue(info, MIN_RADIUS.getValue(info));
	}

	private String name;
	private int min, max;

	Option(String name, int min, int max) {
		this.name = name;
		this.min = min;
		this.max = max;
	}
}