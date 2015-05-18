package tennox.planetoid;

import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.WeightedRandom;

class PlanetType extends WeightedRandom.Item {
	IBlockState in;
	IBlockState out;
	int total;
	IBlockState bottom = null, top = null;
	String name;

	public PlanetType(IBlockState state, int i, String n) {
		this(state, state, i, n);
	}

	PlanetType(IBlockState out, IBlockState in, int weight, String name) {
		super(weight);
		this.out = out;
		this.in = in;
		this.name = name;
	}

	public IBlockState getTop() {
		return top != null ? top : out;
	}

	public IBlockState getBottom() {
		return bottom != null ? bottom : out;
	}

	public PlanetType setTopBlockState(IBlockState state) {
		top = state;
		return this;
	}

	public PlanetType setBottomBlockState(IBlockState state) {
		bottom = state;
		return this;
	}
}