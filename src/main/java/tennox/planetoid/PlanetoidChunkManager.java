package tennox.planetoid;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;

public class PlanetoidChunkManager extends WorldChunkManager {

	public PlanetoidChunkManager(World world, WorldType type) {
		super(world.getSeed(), type);
	}

}