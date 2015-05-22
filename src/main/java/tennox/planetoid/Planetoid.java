package tennox.planetoid;

import net.minecraft.block.BlockSand;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Logger;

@Mod(modid = "TeNNoX_Planetoid", name = "Planetoid", version = "0.9.9.1")
public class Planetoid {

	public static final int ADDITIONAL_SPAWN_TRIES = 0;
	public static WorldType planetoid = new PlanetoidWorld("planetoid");
	static Configuration config;
	public static Logger logger;

	int tick = 0;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}

	@SubscribeEvent
	public void prePopulate(PopulateChunkEvent.Pre event) {
		BlockSand.fallInstantly = false;
	}

	@SubscribeEvent
	public void onChat(ServerChatEvent e) {
		if (e.message.equals("ta"))
			TimeAnalyzer.print();
		else if (e.message.equals("reset")) {
			TimeAnalyzer.reset();
			System.out.println("TA RESET");
		}
	}

	public static String translate(String string) {
		return I18n.format(string, new Object[0]);
	}
}