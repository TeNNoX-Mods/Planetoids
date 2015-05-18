package tennox.planetoid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlanetoidWorld extends WorldType {
	public PlanetoidWorld(String name) {
		super(name);
	}

	public WorldChunkManager getChunkManager(World world) {
		return new PlanetoidChunkManager(world, this);
	}

	public IChunkProvider getChunkGenerator(World world, String generatorOptions) {
		return new PlanetoidChunkProvider(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
	}

	public int getSpawnFuzz() {
		return 3;
	}

	public boolean isCustomizable() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onCustomizeButton(Minecraft mc, GuiCreateWorld guiCreateWorld) {
		mc.displayGuiScreen(new GuiCreatePlanetoidWorld(guiCreateWorld, guiCreateWorld.chunkProviderSettingsJson)); // GuiCreateFlatWorld
	}
}