package tennox.planetoid;

import net.minecraft.block.Block;
import net.minecraft.util.WeightedRandom;

class PlanetType extends WeightedRandom.Item {
	Block in;
	Block out;
	int total;
	Block bottom = null, top = null;
	String name;

	public PlanetType(Block b, int i, String n) {
		this(b, b, i, n);
	}

	PlanetType(Block o, Block i, int w, String n) {
		super(w);
		this.out = o;
		this.in = i;
		this.name = n;
	}

	public Block getTopBlock() {
		return top != null ? top : out;
	}

	public Block getBottomBlock() {
		return bottom != null ? bottom : out;
	}

	public PlanetType setTopBlock(Block i) {
		top = i;
		return this;
	}

	public PlanetType setBottomBlock(Block i) {
		bottom = i;
		return this;
	}
}