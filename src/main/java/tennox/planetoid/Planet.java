package tennox.planetoid;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;

public class Planet {
	static PlanetType DIRT = new PlanetType(Blocks.dirt, 20, "Dirt").setTopBlock(Blocks.grass);
	static PlanetType WOOD = new PlanetType(Blocks.leaves, Blocks.log, 10, "Wood");
	static PlanetType WATER = new PlanetType(Blocks.glass, Blocks.water, 5, "Water");
	static PlanetType SAND = new PlanetType(Blocks.sand, 10, "Sand").setBottomBlock(Blocks.sandstone);
	static PlanetType GLOWSTONE = new PlanetType(Blocks.glowstone, 3, "Glowstone");
	static PlanetType STONE = new PlanetType(Blocks.stone, 20, "Stone");

	static PlanetType GRAVEL = new PlanetType(Blocks.stone, Blocks.gravel, 40, "Gravel");
	static PlanetType COBBLESTONE = new PlanetType(Blocks.stone, Blocks.cobblestone, 60, "Cobblestone");
	static PlanetType LAVA = new PlanetType(Blocks.stone, Blocks.lava, 60, "Lava");
	static PlanetType COAL = new PlanetType(Blocks.stone, Blocks.coal_ore, 60, "Coal");
	static PlanetType IRON = new PlanetType(Blocks.stone, Blocks.iron_ore, 60, "Iron");
	static PlanetType GOLD = new PlanetType(Blocks.stone, Blocks.gold_ore, 30, "Gold");
	static PlanetType REDSTONE = new PlanetType(Blocks.stone, Blocks.redstone_ore, 30, "Redstone");
	static PlanetType LAPISLAZULI = new PlanetType(Blocks.stone, Blocks.lapis_ore, 15, "Lapislazuli");
	static PlanetType TNT = new PlanetType(Blocks.stone, Blocks.tnt, 2, "TNT");
	static PlanetType DIAMOND = new PlanetType(Blocks.stone, Blocks.diamond_ore, 2, "Diamond");
	static PlanetType EMERALD = new PlanetType(Blocks.stone, Blocks.emerald_ore, 1, "Emerald");

	static ArrayList<PlanetType> stonetypes = new ArrayList<PlanetType>();
	static ArrayList<PlanetType> types = initTypes();

	Random rand = new Random();
	PlanetoidChunkProvider chunkprovider;
	World world2;
	int x;
	int y;
	int z;
	int radius;
	PlanetType type;
	ArrayList<Point> unfinished = new ArrayList<Point>();
	ArrayList<Point> finished = new ArrayList<Point>();

	public Planet(PlanetoidChunkProvider provider, World w, int x2, int y2, int z2, int r) {
		this.chunkprovider = provider;
		this.world2 = w;
		this.x = x2;
		this.y = y2;
		this.z = z2;
		this.radius = r;

		this.type = getRandomPlanet();
	}

	private static ArrayList<PlanetType> initTypes() {
		ArrayList<PlanetType> list = new ArrayList<PlanetType>();
		list.add(DIRT);
		// list.add(WOOD);
		// list.add(WATER);
		// list.add(SAND);
		// list.add(GLOWSTONE);
		// list.add(STONE);
		// stonetypes.add(GRAVEL);
		// stonetypes.add(COBBLESTONE);
		// stonetypes.add(LAVA);
		// stonetypes.add(COAL);
		// stonetypes.add(IRON);
		// stonetypes.add(GOLD);
		// stonetypes.add(REDSTONE);
		// stonetypes.add(LAPISLAZULI);
		// stonetypes.add(TNT);
		// stonetypes.add(DIAMOND);
		// stonetypes.add(EMERALD);
		return list;
	}

	public Planet(PlanetoidChunkProvider provider, World w, double x, double y, double z, double r) {
		this(provider, w, round(x), round(y), round(z), round(r));
	}

	public static void print() {
		System.out.println("---PREGENERATION: ---");
		for (PlanetType p : types) {
			System.out.println(p.name + ":\t" + p.total);
		}

		for (PlanetType p : stonetypes) {
			System.out.println("-" + p.name + ":\t" + p.total);
		}
		System.out.println("---PREGENERATION END---");
	}

	public PlanetType getRandomPlanet() {
		this.rand.setSeed(this.x * 341873128712L + this.z * 132897987541L);

		ArrayList<PlanetType> list = new ArrayList<PlanetType>();

		list.addAll(types);

		PlanetType type = (PlanetType) WeightedRandom.getRandomItem(this.rand, list);

		if (type == STONE) {
			list.clear();
			list.addAll(stonetypes);

			type = (PlanetType) WeightedRandom.getRandomItem(this.rand, list);
		}

		type.total += 1;
		if (type.out == Blocks.stone)
			STONE.total += 1;
		return type;
	}

	public void generateChunk(int chunkX, int chunkZ, Block[] ablock, byte[] ameta) {
		this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
		TimeAnalyzer.start("generateChunk");
		for (int x2 = Math.max(chunkX * 16, this.x - this.radius); x2 <= Math.min(chunkX * 16 + 15, this.x + this.radius); x2++) {
			for (int y2 = this.y - this.radius; y2 <= this.y + this.radius; y2++) {
				for (int z2 = Math.max(chunkZ * 16, this.z - this.radius); z2 <= Math.min(chunkZ * 16 + 15, this.z + this.radius); z2++) {
					int d = round(distance(this.x, this.y, this.z, x2, y2, z2));
					if (d == this.radius) {
						if (isBottomBlock(x2, y2, z2))
							setBlock(x2, y2, z2, this.type.getBottomBlock(), 0, ablock, ameta);
						else if (isTopBlock(x2, y2, z2))
							setBlock(x2, y2, z2, this.type.getTopBlock(), 0, ablock, ameta);
						else
							setBlock(x2, y2, z2, this.type.out, 0, ablock, ameta);
					} else if (d < this.radius) {
						setBlock(x2, y2, z2, this.type.in, 0, ablock, ameta);
					}
					generateSpecial(x2, y2, z2, ablock, ameta);
				}
			}
		}

		if (!this.finished.contains(new Point(chunkX, chunkZ)))
			this.finished.add(new Point(chunkX, chunkZ));
		if (this.unfinished.contains(new Point(chunkX, chunkZ)))
			this.unfinished.remove(new Point(chunkX, chunkZ));
		TimeAnalyzer.end("generateChunk");
	}

	public void decorateChunk(World world, int chunkX, int chunkZ) {
		this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
		TimeAnalyzer.start("decorateChunk");
		for (int x2 = Math.max(chunkX * 16, this.x - this.radius); x2 <= Math.min(chunkX * 16 + 15, this.x + this.radius); x2++) {
			for (int y2 = this.y - this.radius; y2 <= this.y + this.radius; y2++) {
				for (int z2 = Math.max(chunkZ * 16, this.z - this.radius); z2 <= Math.min(chunkZ * 16 + 15, this.z + this.radius); z2++) {
					if (isTopBlock(x2, y2, z2)) {
						if ((this.type == SAND) && (this.rand.nextDouble() <= 0.05D) && (Blocks.cactus.canPlaceBlockAt(world, x2, y2 + 1, z2))) {
							for (int i = 1; i <= 1 + this.rand.nextInt(3); i++) {
								world.setBlock(x2, y2 + i, z2, Blocks.cactus);
							}
						} else if (this.type == DIRT) {
							if ((this.rand.nextDouble() <= 0.1D) && (Blocks.tallgrass.canPlaceBlockAt(world, x2, y2 + 1, z2))) {
								world.setBlock(x2, y2 + 1, z2, Blocks.tallgrass, 1, 3);
							} else if (this.rand.nextDouble() <= 0.004D) { // sugar cane
								boolean flag1 = (world.getBlock(x2 + 1, y2, z2) == Blocks.grass) && (world.getBlock(x2 - 1, y2, z2) == Blocks.grass)
										&& (world.getBlock(x2, y2, z2 + 1) == Blocks.grass) && (world.getBlock(x2, y2, z2 - 1) == Blocks.grass);
								boolean flag2 = (world.getBlock(x2 + 1, y2 + 1, z2) == Blocks.air) && (world.getBlock(x2 - 1, y2 + 1, z2) == Blocks.air)
										&& (world.getBlock(x2, y2 + 1, z2 + 1) == Blocks.air) && (world.getBlock(x2, y2 + 1, z2 - 1) == Blocks.air);

								if ((flag1) && (flag2)) {
									world.setBlock(x2, y2, z2, Blocks.water);
									for (int i = 1; i <= 1 + this.rand.nextInt(3); i++)
										world.setBlock(x2 + 1, y2 + i, z2, Blocks.reeds);
									for (int i = 1; i <= 1 + this.rand.nextInt(3); i++)
										world.setBlock(x2 - 1, y2 + i, z2, Blocks.reeds);
									for (int i = 1; i <= 1 + this.rand.nextInt(3); i++)
										world.setBlock(x2, y2 + i, z2 + 1, Blocks.reeds);
									for (int i = 1; i <= 1 + this.rand.nextInt(3); i++)
										world.setBlock(x2, y2 + i, z2 - 1, Blocks.reeds);
								}
							}
						}
					} else if (isBottomBlock(x2, y2, z2)) {
						if (this.type == WATER) {
							if (x2 == this.x && z2 == this.z) {
								world.setBlockToAir(x2, y2, z2);
							} else {
								if (world.getBlock(x2, y2 + 1, z2) == Blocks.water || world.getBlock(x2, y2 + 1, z2) == Blocks.flowing_water) {
									for (int i = 1; i <= 1 + this.rand.nextInt(2); i++)
										world.setBlock(x2, y2 + i, z2, Blocks.clay);
								} else if (world.getBlock(x2, y2 + 2, z2) == Blocks.water || world.getBlock(x2, y2 + 2, z2) == Blocks.flowing_water) {
									for (int i = 2; i <= 2 + this.rand.nextInt(2); i++)
										world.setBlock(x2, y2 + i, z2, Blocks.clay);
								}
							}
						}
					}
				}
			}
		}
		TimeAnalyzer.end("decorateChunk");
	}

	private void generateSpecial(int x, int y, int z, Block[] ablock, byte[] data) {
	}

	private boolean isTopBlock(int x2, int y2, int z2) {
		return (round(distance(this.x, this.y, this.z, x2, y2, z2)) == this.radius) && (round(distance(this.x, this.y, this.z, x2, y2 + 1, z2)) > this.radius);
	}

	private boolean isBottomBlock(int x2, int y2, int z2) {
		return (round(distance(this.x, this.y, this.z, x2, y2, z2)) == this.radius) && (round(distance(this.x, this.y, this.z, x2, y2 - 1, z2)) > this.radius);
	}

	public boolean shouldFinishChunk(int cx, int cz) {
		return this.unfinished.contains(new Point(cx, cz));
	}

	public boolean shouldDecorateChunk(int cx, int cz) {
		return this.finished.contains(new Point(cx, cz));
	}

	public boolean isAreaClear() {
		for (Planet p : this.chunkprovider.unfinished) {
			if (p.intersects(this))
				return false;
		}
		for (Planet p : this.chunkprovider.finished) {
			if (p.intersects(this))
				return false;
		}
		return true;
	}

	private boolean intersects(Planet planet) {
		return distance(planet.x, planet.y, planet.z, this.x, this.y, this.z) <= planet.radius + this.radius + 1;
	}

	public boolean isFinished() {
		return this.unfinished.size() == 0;
	}

	public static int getBlockNum(int x, int y, int z) {
		if (x < 0)
			x = 16 + x;
		if (z < 0)
			z = 16 + z;
		return y + z * 256 + x * 256 * 16;
	}

	public static void setBlock(int x, int y, int z, Block block, int meta, Block[] ablock, byte[] ameta) {
		if ((y < 0) || (y >= 256)) {
			return;
		}
		ablock[getBlockNum(x % 16, y, z % 16)] = block;
		ameta[getBlockNum(x % 16, y, z % 16)] = (byte) meta;
	}

	public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
	}

	public static int round(double d) {
		return (int) Math.round(d);
	}
}