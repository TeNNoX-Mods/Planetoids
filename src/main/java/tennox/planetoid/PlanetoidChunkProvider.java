package tennox.planetoid;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.RAVINE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ANIMALS;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.DUNGEON;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ICE;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAKE;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAVA;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.NoiseGenerator;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class PlanetoidChunkProvider implements IChunkProvider {

	private Random rand;

	ArrayList<Planet> finished = new ArrayList<Planet>();
	ArrayList<Planet> unfinished = new ArrayList<Planet>();
	ArrayList<Point> pregen = new ArrayList<Point>();
	int pregenChunkSize = 4;

	private World world;
	private long seed;
	private boolean mapFeaturesEnabled;
	private PlanetoidGeneratorInfo generatorInfo;

	private NoiseGeneratorOctaves field_147431_j;
	private NoiseGeneratorOctaves field_147432_k;
	private NoiseGeneratorOctaves field_147429_l;
	private NoiseGeneratorPerlin field_147430_m;
	double[] field_147427_d;
	double[] field_147428_e;
	double[] field_147425_f;
	double[] field_147426_g;
	private final double[] field_147434_q;
	private final float[] parabolicField;
	public NoiseGeneratorOctaves noiseGen5;
	public NoiseGeneratorOctaves noiseGen6;
	public NoiseGeneratorOctaves mobSpawnerNoise;
	private double[] stoneNoise = new double[256];
	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenStronghold strongholdGenerator = new MapGenStronghold();
	private MapGenVillage villageGenerator = new MapGenVillage();
	private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
	private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
	private MapGenBase ravineGenerator = new MapGenRavine();
	private BiomeGenBase[] biomesForGeneration;

	{
		caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
		strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);
		villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
		mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
		scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, SCATTERED_FEATURE);
		ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
	}

	// ChunkProviderGenerate
	public PlanetoidChunkProvider(World world, long seed, boolean mapFeaturesEnabled, String generatorOptions) {
		this.world = world;
		this.seed = seed;
		this.rand = new Random(seed);

		this.mapFeaturesEnabled = mapFeaturesEnabled;
		this.generatorInfo = PlanetoidGeneratorInfo.createGeneratorFromString(generatorOptions);

		this.field_147431_j = new NoiseGeneratorOctaves(this.rand, 16);
		this.field_147432_k = new NoiseGeneratorOctaves(this.rand, 16);
		this.field_147429_l = new NoiseGeneratorOctaves(this.rand, 8);
		this.field_147430_m = new NoiseGeneratorPerlin(this.rand, 4);
		this.noiseGen5 = new NoiseGeneratorOctaves(this.rand, 10);
		this.noiseGen6 = new NoiseGeneratorOctaves(this.rand, 16);
		this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.rand, 8);
		this.field_147434_q = new double[825];
		this.parabolicField = new float[25];

		for (int j = -2; j <= 2; ++j) {
			for (int k = -2; k <= 2; ++k) {
				float f = 10.0F / MathHelper.sqrt_float((float) (j * j + k * k) + 0.2F);
				this.parabolicField[j + 2 + (k + 2) * 5] = f;
			}
		}

		NoiseGenerator[] noiseGens = { field_147431_j, field_147432_k, field_147429_l, field_147430_m, noiseGen5, noiseGen6, mobSpawnerNoise };
		noiseGens = TerrainGen.getModdedNoiseGenerators(world, this.rand, noiseGens);
		this.field_147431_j = (NoiseGeneratorOctaves) noiseGens[0];
		this.field_147432_k = (NoiseGeneratorOctaves) noiseGens[1];
		this.field_147429_l = (NoiseGeneratorOctaves) noiseGens[2];
		this.field_147430_m = (NoiseGeneratorPerlin) noiseGens[3];
		this.noiseGen5 = (NoiseGeneratorOctaves) noiseGens[4];
		this.noiseGen6 = (NoiseGeneratorOctaves) noiseGens[5];
		this.mobSpawnerNoise = (NoiseGeneratorOctaves) noiseGens[6];
	}

	@Override
	public boolean chunkExists(int var1, int var2) {
		return true;
	}

	@Override
	public Chunk provideChunk(int chunkX, int chunkZ) {
		TimeAnalyzer.start("provideChunk");
		this.rand.setSeed((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
		preGenerate(chunkX, chunkZ);

		Block[] ablock = new Block[65536];
		byte[] abyte = new byte[65536];
		this.biomesForGeneration = this.world.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);

		generatePlanetoid(chunkX, chunkZ, ablock, abyte);

		// this.replaceBlocksForBiome(chunkX, chunkZ, ablock, abyte, this.biomesForGeneration);
		this.caveGenerator.func_151539_a(this, this.world, chunkX, chunkZ, ablock);
		this.ravineGenerator.func_151539_a(this, this.world, chunkX, chunkZ, ablock);

		if (this.mapFeaturesEnabled) {
			this.mineshaftGenerator.func_151539_a(this, this.world, chunkX, chunkZ, ablock);
			this.villageGenerator.func_151539_a(this, this.world, chunkX, chunkZ, ablock);
			this.strongholdGenerator.func_151539_a(this, this.world, chunkX, chunkZ, ablock);
			this.scatteredFeatureGenerator.func_151539_a(this, this.world, chunkX, chunkZ, ablock);
		}

		Chunk chunk = new Chunk(this.world, ablock, abyte, chunkX, chunkZ);
		byte[] abyte1 = chunk.getBiomeArray();

		for (int k = 0; k < abyte1.length; ++k) {
			abyte1[k] = (byte) this.biomesForGeneration[k].biomeID;
		}

		chunk.generateSkylightMap();
		TimeAnalyzer.end("provideChunk");
		return chunk;
	}

	public void preGenerate(int cx, int cz) {
		TimeAnalyzer.start("preGenerate");
		int x = round(cx / this.pregenChunkSize);
		int z = round(cz / this.pregenChunkSize);

		preGenerate2(x - 1, z - 1);
		preGenerate2(x - 1, z);
		preGenerate2(x - 1, z + 1);
		preGenerate2(x, z - 1);
		preGenerate2(x, z);
		preGenerate2(x, z + 1);
		preGenerate2(x + 1, z - 1);
		preGenerate2(x + 1, z);
		preGenerate2(x + 1, z + 1);

		TimeAnalyzer.end("preGenerate");
	}

	private void preGenerate2(int x, int z) {
		if (!this.pregen.contains(new Point(x, z))) {
			this.rand.setSeed(x * 341873128712L + z * 132897987541L);
			int x2 = x * this.pregenChunkSize * 16;
			int z2 = z * this.pregenChunkSize * 16;

			preGenerate_do(x2, z2, x2 + this.pregenChunkSize * 16, z2 + this.pregenChunkSize * 16);

			this.pregen.add(new Point(x, z));
		}
	}

	private void preGenerate_do(int x1, int z1, int x2, int z2) {
		TimeAnalyzer.start("preGenerate_do");

		for (int l = 0; l < Option.SPAWNTRIES.getValue(generatorInfo); l++) {
			double min = Option.MIN_RADIUS.getValue(generatorInfo);
			double max = Option.MAX_RADIUS.getValue(generatorInfo);
			double r = round(this.rand.nextDouble() * (max - min) + min);
			double x = x1 + this.rand.nextInt(x2 - x1);
			double y = round(r + (256 - 2 * r) * this.rand.nextDouble());
			double z = z1 + this.rand.nextInt(z2 - z1);

			Planet p = new Planet(this, this.world, x, y, z, round(r));

			if (p.isAreaClear()) {
				for (int i = round(x) - round(r); i <= round(x) + round(r); i++) {
					for (int k = round(z) - round(r); k <= round(z) + round(r); k++) {
						int cx = (int) Math.floor(i / 16.0D);
						int cz = (int) Math.floor(k / 16.0D);

						if (!p.unfinished.contains(new Point(cx, cz))) {
							p.unfinished.add(new Point(cx, cz));
						}
					}
				}
				this.unfinished.add(p);
			}
		}
		TimeAnalyzer.end("preGenerate_do");
	}

	public void generatePlanetoid(int chunkX, int chunkZ, Block[] ablock, byte[] ameta) {
		TimeAnalyzer.start("generate");

		TimeAnalyzer.start("finishPlanets");
		for (int i = 0; i < this.unfinished.size(); i++) {
			Planet p = (Planet) this.unfinished.get(i);
			if (p.shouldFinishChunk(chunkX, chunkZ))
				p.generateChunk(chunkX, chunkZ, ablock, ameta);
			if (p.isFinished()) {
				this.unfinished.remove(p);
				this.finished.add(p);

				i--;
			}
		}
		TimeAnalyzer.end("finishPlanets");

		TimeAnalyzer.start("generateWater");
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 16; z++) {
					Planet.setBlock(x, y, z, (y == 0 ? Blocks.bedrock : Blocks.water), 0, ablock, ameta);
				}
			}
		}
		TimeAnalyzer.end("generateWater");

		TimeAnalyzer.end("generate");
	}

	@Override
	public Chunk loadChunk(int var1, int var2) {
		return this.provideChunk(var1, var2);
	}

	@Override
	// ChunkProviderGenerate
	public void populate(IChunkProvider provider, int chunkX, int chunkZ) {
		BlockFalling.fallInstantly = true;
		int k = chunkX * 16;
		int l = chunkZ * 16;
		BiomeGenBase biomegenbase = this.world.getBiomeGenForCoords(k + 16, l + 16);
		this.rand.setSeed(this.world.getSeed());
		long i1 = this.rand.nextLong() / 2L * 2L + 1L;
		long j1 = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed((long) chunkX * i1 + (long) chunkZ * j1 ^ this.world.getSeed());
		boolean hasGeneratedVillage = false;

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage));

		if (this.mapFeaturesEnabled) {
			this.mineshaftGenerator.generateStructuresInChunk(this.world, this.rand, chunkX, chunkZ);
			hasGeneratedVillage = this.villageGenerator.generateStructuresInChunk(this.world, this.rand, chunkX, chunkZ);
			this.strongholdGenerator.generateStructuresInChunk(this.world, this.rand, chunkX, chunkZ);
			this.scatteredFeatureGenerator.generateStructuresInChunk(this.world, this.rand, chunkX, chunkZ);
		}

		int k1;
		int l1;
		int i2;

		if (biomegenbase != BiomeGenBase.desert && biomegenbase != BiomeGenBase.desertHills && !hasGeneratedVillage && this.rand.nextInt(4) == 0
				&& TerrainGen.populate(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage, LAKE)) {
			k1 = k + this.rand.nextInt(16) + 8;
			l1 = this.rand.nextInt(256);
			i2 = l + this.rand.nextInt(16) + 8;
			(new WorldGenLakes(Blocks.water)).generate(this.world, this.rand, k1, l1, i2);
		}

		if (TerrainGen.populate(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage, LAVA) && !hasGeneratedVillage && this.rand.nextInt(8) == 0) {
			k1 = k + this.rand.nextInt(16) + 8;
			l1 = this.rand.nextInt(this.rand.nextInt(248) + 8);
			i2 = l + this.rand.nextInt(16) + 8;

			if (l1 < 63 || this.rand.nextInt(10) == 0) {
				(new WorldGenLakes(Blocks.lava)).generate(this.world, this.rand, k1, l1, i2);
			}
		}

		boolean doGen = TerrainGen.populate(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage, DUNGEON);
		for (k1 = 0; doGen && k1 < 8; ++k1) {
			l1 = k + this.rand.nextInt(16) + 8;
			i2 = this.rand.nextInt(256);
			int j2 = l + this.rand.nextInt(16) + 8;
			(new WorldGenDungeons()).generate(this.world, this.rand, l1, i2, j2);
		}

		biomegenbase.decorate(this.world, this.rand, k, l);
		if (TerrainGen.populate(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage, ANIMALS)) {
			SpawnerAnimals.performWorldGenSpawning(this.world, biomegenbase, k + 8, l + 8, 16, 16, this.rand);
		}
		k += 8;
		l += 8;

		doGen = TerrainGen.populate(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage, ICE);
		for (k1 = 0; doGen && k1 < 16; ++k1) {
			for (l1 = 0; l1 < 16; ++l1) {
				i2 = this.world.getPrecipitationHeight(k + k1, l + l1);

				if (this.world.isBlockFreezable(k1 + k, i2 - 1, l1 + l)) {
					this.world.setBlock(k1 + k, i2 - 1, l1 + l, Blocks.ice, 0, 2);
				}

				if (this.world.func_147478_e(k1 + k, i2, l1 + l, true)) {
					this.world.setBlock(k1 + k, i2, l1 + l, Blocks.snow_layer, 0, 2);
				}
			}
		}

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage));

		BlockFalling.fallInstantly = false;
	}

	@Override
	public boolean saveChunks(boolean var1, IProgressUpdate var2) {
		return true;
	}

	@Override
	public boolean unloadQueuedChunks() {
		return false;
	}

	@Override
	public boolean canSave() {
		return true;
	}

	@Override
	public String makeString() {
		return "RandomLevelSource";
	}

	@Override
	public List getPossibleCreatures(EnumCreatureType par1EnumCreatureType, int par2, int par3, int par4) {
		BiomeGenBase biomegenbase = this.world.getBiomeGenForCoords(par2, par4);
		return par1EnumCreatureType == EnumCreatureType.monster && this.scatteredFeatureGenerator.func_143030_a(par2, par3, par4) ? this.scatteredFeatureGenerator
				.getScatteredFeatureSpawnList() : biomegenbase.getSpawnableList(par1EnumCreatureType);
	}

	@Override
	// findClosestStructure
	public ChunkPosition func_147416_a(World world, String structure, int x, int y, int z) {
		return "Stronghold".equals(structure) && this.strongholdGenerator != null ? this.strongholdGenerator.func_151545_a(world, x, y, z) : null;
	}

	@Override
	public int getLoadedChunkCount() {
		return 0;
	}

	@Override
	public void recreateStructures(int x, int z) {
		if (this.mapFeaturesEnabled) {
			this.mineshaftGenerator.func_151539_a(this, this.world, x, z, (Block[]) null);
			this.villageGenerator.func_151539_a(this, this.world, x, z, (Block[]) null);
			this.strongholdGenerator.func_151539_a(this, this.world, x, z, (Block[]) null);
			this.scatteredFeatureGenerator.func_151539_a(this, this.world, x, z, (Block[]) null);
		}
	}

	public static int round(double d) {
		return (int) Math.round(d);
	}

	@Override
	public void saveExtraData() {

	}

}