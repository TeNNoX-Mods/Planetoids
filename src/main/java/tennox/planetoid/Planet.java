package tennox.planetoid;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class Planet {
	static PlanetType DIRT = new PlanetType(Blocks.dirt.getDefaultState(), 20, "Dirt").setTopBlockState(Blocks.grass.getDefaultState());
	static PlanetType WOOD = new PlanetType(Blocks.leaves.getDefaultState(), Blocks.log.getDefaultState(), 10, "Wood");
	static PlanetType WATER = new PlanetType(Blocks.glass.getDefaultState(), Blocks.water.getDefaultState(), 5, "Water");
	static PlanetType SAND = new PlanetType(Blocks.sand.getDefaultState(), 10, "Sand").setBottomBlockState(Blocks.sandstone.getDefaultState());
	static PlanetType GLOWSTONE = new PlanetType(Blocks.glowstone.getDefaultState(), 3, "Glowstone");
	static PlanetType STONE = new PlanetType(Blocks.stone.getDefaultState(), 20, "Stone");

	static PlanetType GRAVEL = new PlanetType(Blocks.stone.getDefaultState(), Blocks.gravel.getDefaultState(), 40, "Gravel");
	static PlanetType COBBLESTONE = new PlanetType(Blocks.stone.getDefaultState(), Blocks.cobblestone.getDefaultState(), 60, "Cobblestone");
	static PlanetType LAVA = new PlanetType(Blocks.stone.getDefaultState(), Blocks.lava.getDefaultState(), 60, "Lava");
	static PlanetType COAL = new PlanetType(Blocks.stone.getDefaultState(), Blocks.coal_ore.getDefaultState(), 60, "Coal");
	static PlanetType IRON = new PlanetType(Blocks.stone.getDefaultState(), Blocks.iron_ore.getDefaultState(), 60, "Iron");
	static PlanetType GOLD = new PlanetType(Blocks.stone.getDefaultState(), Blocks.gold_ore.getDefaultState(), 30, "Gold");
	static PlanetType REDSTONE = new PlanetType(Blocks.stone.getDefaultState(), Blocks.redstone_ore.getDefaultState(), 30, "Redstone");
	static PlanetType LAPISLAZULI = new PlanetType(Blocks.stone.getDefaultState(), Blocks.lapis_ore.getDefaultState(), 15, "Lapislazuli");
	static PlanetType TNT = new PlanetType(Blocks.stone.getDefaultState(), Blocks.tnt.getDefaultState(), 2, "TNT");
	static PlanetType DIAMOND = new PlanetType(Blocks.stone.getDefaultState(), Blocks.diamond_ore.getDefaultState(), 2, "Diamond");
	static PlanetType EMERALD = new PlanetType(Blocks.stone.getDefaultState(), Blocks.emerald_ore.getDefaultState(), 1, "Emerald");

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
	
	/** List of not generated Chunks this Plantoid expands to **/
	ArrayList<Point> unfinishedChunks = new ArrayList<Point>();
	/** List of finished Chunks this Plantoid expands to **/
	ArrayList<Point> finishedChunks = new ArrayList<Point>();

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

	public void generateChunk(int chunkX, int chunkZ, ChunkPrimer primer) {
		this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
		TimeAnalyzer.start("generateChunk");
		for (int x2 = Math.max(chunkX * 16, this.x - this.radius); x2 <= Math.min(chunkX * 16 + 15, this.x + this.radius); x2++) {
			for (int y2 = this.y - this.radius; y2 <= this.y + this.radius; y2++) {
				for (int z2 = Math.max(chunkZ * 16, this.z - this.radius); z2 <= Math.min(chunkZ * 16 + 15, this.z + this.radius); z2++) {
					int d = round(distance(this.x, this.y, this.z, x2, y2, z2));
					if (d == this.radius) {
						if (isBottomBlock(x2, y2, z2))
							setBlock(x2, y2, z2, this.type.getBottom(), primer, chunkX, chunkZ);
						else if (isTopBlock(x2, y2, z2))
							setBlock(x2, y2, z2, this.type.getTop(), primer, chunkX, chunkZ);
						else
							setBlock(x2, y2, z2, this.type.out, primer, chunkX, chunkZ);
					} else if (d < this.radius) {
						setBlock(x2, y2, z2, this.type.in, primer, chunkX, chunkZ);
					}
					generateSpecial(x2, y2, z2, primer);
				}
			}
		}

		if (!this.finishedChunks.contains(new Point(chunkX, chunkZ)))
			this.finishedChunks.add(new Point(chunkX, chunkZ));
		if (this.unfinishedChunks.contains(new Point(chunkX, chunkZ)))
			this.unfinishedChunks.remove(new Point(chunkX, chunkZ));
		TimeAnalyzer.end("generateChunk");
	}

	public void decorateChunk(World world, int chunkX, int chunkZ) {
		this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
		TimeAnalyzer.start("decorateChunk");
		for (int x2 = Math.max(chunkX * 16, this.x - this.radius); x2 <= Math.min(chunkX * 16 + 15, this.x + this.radius); x2++) {
			for (int y2 = this.y - this.radius; y2 <= this.y + this.radius; y2++) {
				for (int z2 = Math.max(chunkZ * 16, this.z - this.radius); z2 <= Math.min(chunkZ * 16 + 15, this.z + this.radius); z2++) {
					BlockPos pos = new BlockPos(x2, y2, z2);
					BlockPos above = pos.up();
					if (isTopBlock(x2, y2, z2)) {
						if ((this.type == SAND) && (this.rand.nextDouble() <= 0.05D) && (Blocks.cactus.canPlaceBlockAt(world, above))) {
							for (int i = 1; i <= 1 + this.rand.nextInt(3); i++) {
								world.setBlockState(pos.up(i), Blocks.cactus.getDefaultState());
							}
						} else if (this.type == DIRT) {
							if ((this.rand.nextDouble() <= 0.1D) && (Blocks.tallgrass.canPlaceBlockAt(world, above))) {
								world.setBlockState(above, Blocks.tallgrass.getDefaultState());
							} else if (this.rand.nextDouble() <= 0.004D) { // ref. BlockReeds
								boolean current = world.getBlockState(pos) == Blocks.grass;
								// flag1 - surrounded by grass, flag2 - above air
								boolean flag1 = (world.getBlockState(pos.north()) == Blocks.grass) && (world.getBlockState(pos.south()) == Blocks.grass)
										&& (world.getBlockState(pos.east()) == Blocks.grass) && (world.getBlockState(pos.west()) == Blocks.grass);
								boolean flag2 = (world.getBlockState(above.north()) == Blocks.air) && (world.getBlockState(above.south()) == Blocks.air)
										&& (world.getBlockState(above.east()) == Blocks.air) && (world.getBlockState(above.west()) == Blocks.air);

								if (current && flag1 && flag2) { // if all requirements are met, start actual generation
									world.setBlockState(pos, Blocks.water.getDefaultState());
									for (int i = 1; i <= 1 + this.rand.nextInt(3); i++) {
										if (world.getBlockState(pos.north().up(i)).getBlock() == Blocks.air)
											world.setBlockState(pos.north().up(i), Blocks.reeds.getDefaultState());
									}
									for (int i = 1; i <= 1 + this.rand.nextInt(3); i++) {
										if (world.getBlockState(pos.south().up(i)).getBlock() == Blocks.air)
											world.setBlockState(pos.south().up(i), Blocks.reeds.getDefaultState());
									}
									for (int i = 1; i <= 1 + this.rand.nextInt(3); i++) {
										if (world.getBlockState(pos.east().up(i)).getBlock() == Blocks.air)
											world.setBlockState(pos.east().up(i), Blocks.reeds.getDefaultState());
									}
									for (int i = 1; i <= 1 + this.rand.nextInt(3); i++) {
										if (world.getBlockState(pos.west().up(i)).getBlock() == Blocks.air)
											world.setBlockState(pos.west().up(i), Blocks.reeds.getDefaultState());
									}
								}
							}
						}
					} else if (isBottomBlock(x2, y2, z2)) {
						if (this.type == WATER) {
							if (x2 == this.x && z2 == this.z) {
								world.setBlockToAir(pos);
							} else {
								if (world.getBlockState(above) == Blocks.water || world.getBlockState(above) == Blocks.flowing_water) {
									for (int i = 1; i <= 1 + this.rand.nextInt(2); i++)
										world.setBlockState(pos.up(i), Blocks.clay.getDefaultState());
								} else if (world.getBlockState(pos.up(2)) == Blocks.water || world.getBlockState(pos.up(2)) == Blocks.flowing_water) {
									for (int i = 2; i <= 2 + this.rand.nextInt(2); i++)
										world.setBlockState(pos.up(i), Blocks.clay.getDefaultState());
								}
							}
						}
					}
				}
			}
		}
		TimeAnalyzer.end("decorateChunk");
	}

	private void generateSpecial(int x, int y, int z, ChunkPrimer primer) {
	}

	private boolean isTopBlock(int x2, int y2, int z2) {
		return (round(distance(this.x, this.y, this.z, x2, y2, z2)) == this.radius) && (round(distance(this.x, this.y, this.z, x2, y2 + 1, z2)) > this.radius);
	}

	private boolean isBottomBlock(int x2, int y2, int z2) {
		return (round(distance(this.x, this.y, this.z, x2, y2, z2)) == this.radius) && (round(distance(this.x, this.y, this.z, x2, y2 - 1, z2)) > this.radius);
	}

	public boolean shouldFinishChunk(int cx, int cz) {
		return this.unfinishedChunks.contains(new Point(cx, cz));
	}

	public boolean shouldDecorateChunk(int cx, int cz) {
		return this.finishedChunks.contains(new Point(cx, cz));
	}

	public boolean isAreaClear() {
		for (Planet p : this.chunkprovider.unfinishedPlanets) {
			if (p.intersects(this))
				return false;
		}
		for (Planet p : this.chunkprovider.finishedPlanets) {
			if (p.intersects(this))
				return false;
		}
		return true;
	}

	private boolean intersects(Planet planet) {
		return distance(planet.x, planet.y, planet.z, this.x, this.y, this.z) <= planet.radius + this.radius + 1;
	}

	public boolean isFinished() {
		return this.unfinishedChunks.size() == 0;
	}

	public static int getBlockNum(int x, int y, int z) {
		if (x < 0)
			x = 16 + x;
		if (z < 0)
			z = 16 + z;
		return y + z * 256 + x * 256 * 16;
	}

	// This is a wrapper for the setBlockState, to prevent OutOfBoundsExceptions
	public static void setBlock(int x, int y, int z, IBlockState state, ChunkPrimer primer, int chunkX, int chunkZ) {
		if ((y < 0) || (y >= 256)) { // TODO: remove this and fix the errors themselves
			return;
		}
		try {
			int cx = x - chunkX * 16;
			int cz = z - chunkZ * 16;
			primer.setBlockState(cx, y, cz, Blocks.dirt.getDefaultState());
		} catch (RuntimeException e) {
			System.out.println("ERROR setting block: " + x + "(" + (x % 16) + ")," + y + "," + z + "(" + (z % 16) + ") !" + (x << 12 | z << 8 | y) + " >= 65536");
			throw e;
		}
	}

	public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
	}

	public static int round(double d) {
		return (int) Math.round(d);
	}
}