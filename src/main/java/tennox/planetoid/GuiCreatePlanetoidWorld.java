package tennox.planetoid;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCreatePlanetoidWorld extends GuiScreen {
	private final GuiCreateWorld createWorldGui;
	PlanetoidGeneratorInfo generatorInfo = PlanetoidGeneratorInfo.getDefaultGenerator(); // FlatGeneratorInfo

	// GuiCreateFlatWorld
	public GuiCreatePlanetoidWorld(GuiCreateWorld guiCreateWorld, String str) {
		this.createWorldGui = guiCreateWorld;
		this.generatorInfo = PlanetoidGeneratorInfo.createGeneratorFromString(str);
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@SuppressWarnings("unchecked")
	public void initGui() {
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, Planetoid.translate("gui.done")));
		this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, Planetoid.translate("gui.cancel")));
		this.buttonList.add(new GuiButton(2, this.width / 2 - 155, 52, 150, 20, Planetoid.translate("planetoid.gui.defaultgeneration") + ": "
				+ (generatorInfo.defaultGeneration ? "on" : "off")));
		this.buttonList.add(new PlanetoidSlider(3, this, this.width / 2 - 155, 74, Option.MIN_RADIUS));
		this.buttonList.add(new PlanetoidSlider(4, this, this.width / 2 - 155, 98, Option.MAX_RADIUS));
		this.buttonList.add(new PlanetoidSlider(5, this, this.width / 2 + 5, 74, Option.SPAWNTRIES));
	}

	// ref. GuiCreateFlatWorld.actionPerformed
	protected void actionPerformed(GuiButton button) {
		if (button.id == 1) {
			this.mc.displayGuiScreen(this.createWorldGui);
		} else if (button.id == 0) {
			this.createWorldGui.chunkProviderSettingsJson = generatorInfo.toString();
			this.mc.displayGuiScreen(this.createWorldGui);
		} else if (button.id == 2) {
			generatorInfo.defaultGeneration = !generatorInfo.defaultGeneration;
			button.displayString = I18n.format("planetoid.gui.defaultgeneration", new Object[0]) + ": " + (generatorInfo.defaultGeneration ? "on" : "off");
		}

	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, "Planetoid World", this.width / 2, 8, 16777215);
		super.drawScreen(par1, par2, par3);
	}
}