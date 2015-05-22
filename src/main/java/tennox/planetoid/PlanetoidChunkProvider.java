package tennox.planetoid;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.OCEAN_MONUMENT;
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

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderSettings;
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
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

// ref. ChunkProviderGenerate
public class PlanetoidChunkProvider implements IChunkProvider {

	private Random rand;

	ArrayList<Planet> finishedPlanets = new ArrayList<Planet>();
	ArrayList<Planet> unfinishedPlanets = new ArrayList<Planet>();
	ArrayList<Point> pregen = new ArrayList<Point>();
	int pregenChunkSize = 4;

	private World world;
	private long seed;
	private boolean mapFeaturesEnabled;
	private PlanetoidGeneratorInfo generatorInfo;

	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenStronghold strongholdGenerator = new MapGenStronghold();
	private MapGenVillage villageGenerator = new MapGenVillage();
	private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
	private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
	private MapGenBase ravineGenerator = new MapGenRavine();
	private StructureOceanMonument oceanMonumentGenerator;
	private BiomeGenBase[] biomesForGeneration;

	private PlanetoidProviderSettings settings;

	public static class PlanetoidProviderSettings {
		public boolean useCaves = true;
		public boolean useDungeons = true;
		public int dungeonChance = 8;
		public boolean useStrongholds = true;
		public boolean useVillages = false; // doesn't look very good (#2)
		public boolean useMineShafts = false; // doesn't look good either
		public boolean useTemples = true;
		public boolean useMonuments = true;
		public boolean useRavines = true;
		public boolean useWaterLakes = true;
		public int waterLakeChance = 4;
		public boolean useLavaLakes = true;
		public int lavaLakeChance = 80;
		public boolean useLavaOceans = false;
	}

	// ChunkProviderGenerate
	public PlanetoidChunkProvider(World world, long seed, boolean mapFeaturesEnabled, String generatorOptions) {
		this.world = world;
		this.seed = seed;
		this.rand = new Random(seed);

		this.settings = new PlanetoidProviderSettings();
		settings.useVillages = false;
		settings.useMineShafts = false;

		this.caveGenerator = new MapGenCaves();
		this.strongholdGenerator = new MapGenStronghold();
		this.villageGenerator = new MapGenVillage();
		this.mineshaftGenerator = new MapGenMineshaft();
		this.scatteredFeatureGenerator = new MapGenScatteredFeature();
		this.ravineGenerator = new MapGenRavine();
		this.oceanMonumentGenerator = new StructureOceanMonument();
		{
			caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
			strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);
			villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
			mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
			scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, SCATTERED_FEATURE);
			ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
			oceanMonumentGenerator = (StructureOceanMonument) TerrainGen.getModdedMapGen(oceanMonumentGenerator, OCEAN_MONUMENT);
		}

		this.mapFeaturesEnabled = mapFeaturesEnabled;
		this.generatorInfo = PlanetoidGeneratorInfo.createGeneratorFromString(generatorOptions);
	}

	@Override
	public boolean chunkExists(int var1, int var2) {
		return true;
	}

	@Override
	public Chunk provideChunk(int chunkX, int chunkZ) {
		TimeAnalyzer.start("provide");
		this.rand.setSeed((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
		ChunkPrimer primer = new ChunkPrimer();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				primer.setBlockState(x, 0, z, Blocks.diamond_block.getDefaultState());
			}
		}

		preGenerate(chunkX, chunkZ);

		this.biomesForGeneration = this.world.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);

		generatePlanetoid(chunkX, chunkZ, primer);

		// this.replaceBlocksForBiome(chunkX, chunkZ, ablock, abyte, this.biomesForGeneration);
		// -> we don't want this. this replaces all blocks because they would be generated all out of stone in default generation

		// TODO: remove map features not applicable for planetoids
		if (this.settings.useCaves) {
			this.caveGenerator.generate(this, this.world, chunkX, chunkZ, primer);
		}
		if (this.settings.useRavines) {
			this.ravineGenerator.generate(this, this.world, chunkX, chunkZ, primer);
		}
		if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
			this.mineshaftGenerator.generate(this, this.world, chunkX, chunkZ, primer);
		}
		if (this.settings.useVillages && this.mapFeaturesEnabled) {
			this.villageGenerator.generate(this, this.world, chunkX, chunkZ, primer);
		}
		if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
			this.strongholdGenerator.generate(this, this.world, chunkX, chunkZ, primer);
		}
		if (this.settings.useTemples && this.mapFeaturesEnabled) {
			this.scatteredFeatureGenerator.generate(this, this.world, chunkX, chunkZ, primer);
		}
		if (this.settings.useMonuments && this.mapFeaturesEnabled) {
			this.oceanMonumentGenerator.generate(this, this.world, chunkX, chunkZ, primer);
		}

		Chunk chunk = new Chunk(this.world, primer, chunkX, chunkZ);
		byte[] abyte1 = chunk.getBiomeArray();

		for (int k = 0; k < abyte1.length; ++k) {
			abyte1[k] = (byte) this.biomesForGeneration[k].biomeID;
		}

		chunk.generateSkylightMap();
		TimeAnalyzer.end();
		return chunk;
	}

	public Chunk provideChunk(BlockPos blockPosIn) {
		return this.provideChunk(blockPosIn.getX() >> 4, blockPosIn.getZ() >> 4);
	}

	public void preGenerate(int cx, int cz) {
		TimeAnalyzer.start("pregen");
		int x = round(cx / this.pregenChunkSize); // TODO: shouldn't this be floor
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

		TimeAnalyzer.end();
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
		TimeAnalyzer.start("pregen_do");

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

						if (!p.unfinishedChunks.contains(new Point(cx, cz))) {
							p.unfinishedChunks.add(new Point(cx, cz));
						}
					}
				}
				this.unfinishedPlanets.add(p);
			}
		}
		TimeAnalyzer.end();
	}

	public void generatePlanetoid(int chunkX, int chunkZ, ChunkPrimer primer) {
		TimeAnalyzer.start("gen");

		TimeAnalyzer.start("planet");
		System.out.println("finishing: " + unfinishedPlanets.size() + " planetoids (" + finishedPlanets.size() + " finished)");
		for (int i = 0; i < this.unfinishedPlanets.size(); i++) {
			Planet p = (Planet) this.unfinishedPlanets.get(i);
			if (p.shouldFinishChunk(chunkX, chunkZ))
				p.generateChunk(chunkX, chunkZ, primer);
			if (p.isFinished()) {
				this.unfinishedPlanets.remove(p);
				this.finishedPlanets.add(p);

				i--;
			}
		}
		TimeAnalyzer.end();

		TimeAnalyzer.start("water");
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 4; y++) {
				for (int z = 0; z < 16; z++) {
					primer.setBlockState(x, y, z, (y == 0 ? Blocks.bedrock.getDefaultState() : Blocks.water.getDefaultState()));
				}
			}
		}
		TimeAnalyzer.end();

		TimeAnalyzer.end();
	}

	@Override
	// ChunkProviderGenerate
	public void populate(IChunkProvider provider, int chunkX, int chunkZ) {
		TimeAnalyzer.start("populate");
		BlockFalling.fallInstantly = false; // TODO: shouldn't this be off for performance?
		int k = chunkX * 16;
		int l = chunkZ * 16;
		BlockPos blockpos = new BlockPos(k, 0, l);
		BiomeGenBase biomegenbase = this.world.getBiomeGenForCoords(blockpos.add(16, 0, 16));
		this.rand.setSeed(this.world.getSeed());
		long i1 = this.rand.nextLong() / 2L * 2L + 1L;
		long j1 = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed((long) chunkX * i1 + (long) chunkZ * j1 ^ this.world.getSeed());
		ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(chunkX, chunkZ);
		boolean hasGeneratedVillage = false;

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage));

		if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
			this.mineshaftGenerator.generateStructure(this.world, this.rand, chunkcoordintpair);
		}

		if (this.settings.useVillages && this.mapFeaturesEnabled) {
			hasGeneratedVillage = this.villageGenerator.generateStructure(this.world, this.rand, chunkcoordintpair);
		}

		if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
			this.strongholdGenerator.generateStructure(this.world, this.rand, chunkcoordintpair);
		}

		if (this.settings.useTemples && this.mapFeaturesEnabled) {
			this.scatteredFeatureGenerator.generateStructure(this.world, this.rand, chunkcoordintpair);
		}

		if (this.settings.useMonuments && this.mapFeaturesEnabled) {
			this.oceanMonumentGenerator.generateStructure(this.world, this.rand, chunkcoordintpair);
		}

		int k1;
		int l1;
		int i2;

		if (biomegenbase != BiomeGenBase.desert && biomegenbase != BiomeGenBase.desertHills && this.settings.useWaterLakes && !hasGeneratedVillage
				&& this.rand.nextInt(this.settings.waterLakeChance) == 0 && TerrainGen.populate(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage, LAKE)) {
			k1 = this.rand.nextInt(16) + 8;
			l1 = this.rand.nextInt(256);
			i2 = this.rand.nextInt(16) + 8;
			(new WorldGenLakes(Blocks.water)).generate(this.world, this.rand, blockpos.add(k1, l1, i2));
		}

		if (TerrainGen.populate(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage, LAVA) && !hasGeneratedVillage
				&& this.rand.nextInt(this.settings.lavaLakeChance / 10) == 0 && this.settings.useLavaLakes) {
			k1 = this.rand.nextInt(16) + 8;
			l1 = this.rand.nextInt(this.rand.nextInt(248) + 8);
			i2 = this.rand.nextInt(16) + 8;

			if (l1 < 63 || this.rand.nextInt(this.settings.lavaLakeChance / 8) == 0) {
				(new WorldGenLakes(Blocks.lava)).generate(this.world, this.rand, blockpos.add(k1, l1, i2));
			}
		}

		if (this.settings.useDungeons) {
			boolean doGen = TerrainGen.populate(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage, DUNGEON);
			for (k1 = 0; doGen && k1 < this.settings.dungeonChance; ++k1) {
				l1 = this.rand.nextInt(16) + 8;
				i2 = this.rand.nextInt(256);
				int j2 = this.rand.nextInt(16) + 8;
				(new WorldGenDungeons()).generate(this.world, this.rand, blockpos.add(l1, i2, j2));
			}
		}

		biomegenbase.decorate(this.world, this.rand, new BlockPos(k, 0, l));
		if (TerrainGen.populate(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage, ANIMALS)) {
			SpawnerAnimals.performWorldGenSpawning(this.world, biomegenbase, k + 8, l + 8, 16, 16, this.rand);
		}
		blockpos = blockpos.add(8, 0, 8);

		boolean doGen = TerrainGen.populate(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage, ICE);
		for (k1 = 0; doGen && k1 < 16; ++k1) {
			for (l1 = 0; l1 < 16; ++l1) {
				BlockPos blockpos1 = this.world.getPrecipitationHeight(blockpos.add(k1, 0, l1));
				BlockPos blockpos2 = blockpos1.down();

				if (this.world.canBlockFreezeWater(blockpos2)) {
					this.world.setBlockState(blockpos2, Blocks.ice.getDefaultState(), 2);
				}

				if (this.world.canSnowAt(blockpos1, true)) {
					this.world.setBlockState(blockpos1, Blocks.snow_layer.getDefaultState(), 2);
				}
			}
		}

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(provider, world, rand, chunkX, chunkZ, hasGeneratedVillage));

		BlockFalling.fallInstantly = false;
		TimeAnalyzer.end();
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
		return "RandomPlanetoidLevelSource";
	}

	@Override
	public List getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		BiomeGenBase biomegenbase = this.world.getBiomeGenForCoords(pos);

		if (this.mapFeaturesEnabled) {
			if (creatureType == EnumCreatureType.MONSTER && this.scatteredFeatureGenerator.func_175798_a(pos)) {
				return this.scatteredFeatureGenerator.getScatteredFeatureSpawnList();
			}
			if (creatureType == EnumCreatureType.MONSTER && this.settings.useMonuments && this.oceanMonumentGenerator.func_175796_a(this.world, pos)) {
				return this.oceanMonumentGenerator.func_175799_b();
			}
		}

		return biomegenbase.getSpawnableList(creatureType);
	}

	@Override
	// was called: findClosestStructure
	public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos pos) {
		return "Stronghold".equals(structureName) && this.strongholdGenerator != null ? this.strongholdGenerator.getClosestStrongholdPos(worldIn, pos) : null;
	}

	@Override
	public int getLoadedChunkCount() {
		return 0;
	}

	@Override
	public void recreateStructures(Chunk chunk, int x, int z) {
		if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
			this.mineshaftGenerator.generate(this, this.world, x, z, (ChunkPrimer) null);
		}
		if (this.settings.useVillages && this.mapFeaturesEnabled) {
			this.villageGenerator.generate(this, this.world, x, z, (ChunkPrimer) null);
		}
		if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
			this.strongholdGenerator.generate(this, this.world, x, z, (ChunkPrimer) null);
		}
		if (this.settings.useTemples && this.mapFeaturesEnabled) {
			this.scatteredFeatureGenerator.generate(this, this.world, x, z, (ChunkPrimer) null);
		}
		if (this.settings.useMonuments && this.mapFeaturesEnabled) {
			this.oceanMonumentGenerator.generate(this, this.world, x, z, (ChunkPrimer) null);
		}
	}

	public static int round(double d) {
		return (int) Math.round(d);
	}

	@Override
	public void saveExtraData() {

	}

	@Override
	// TODO: what's this? - 'ocean monument' func_177460_a
	public boolean func_177460_a(IChunkProvider p_177460_1_, Chunk p_177460_2_, int p_177460_3_, int p_177460_4_) {
		boolean flag = false;

		if (this.settings.useMonuments && this.mapFeaturesEnabled && p_177460_2_.getInhabitedTime() < 3600L) {
			flag |= this.oceanMonumentGenerator.generateStructure(this.world, this.rand, new ChunkCoordIntPair(p_177460_3_, p_177460_4_));
		}

		return flag;
	}
}