package tennox.planetoid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

public class PlanetoidSlider extends GuiButton {
	private float sliderValue;
	public boolean dragging;
	GuiCreatePlanetoidWorld gui;
	Option option;

	// GuiOptionSlider
	public PlanetoidSlider(int id, GuiCreatePlanetoidWorld gui, int x, int y, Option option) {
		super(id, x, y, 150, 20, "");
		this.gui = gui;
		this.option = option;
		this.sliderValue = option.getSliderValue(gui.generatorInfo);
		this.displayString = option.getDisplayString(sliderValue);
	}

	protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3) {
		if (this.visible) {
			if (this.dragging) { // dragging
				this.sliderValue = ((float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8));
				if (this.sliderValue < 0.0F) {
					this.sliderValue = 0.0F;
				}

				if (this.sliderValue > 1.0F) {
					this.sliderValue = 1.0F;
				}

				option.updateValue(gui.generatorInfo, sliderValue);
				Option.checkValues(gui.generatorInfo);
				this.sliderValue = option.getSliderValue(gui.generatorInfo);
				this.displayString = option.getDisplayString(sliderValue);
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (this.width - 8)), this.yPosition, 0, 66, 4, 20);
			drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
		}
	}

	public boolean mousePressed(Minecraft mc, int par2, int par3) {
		if (super.mousePressed(mc, par2, par3)) {
			this.sliderValue = ((float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8));

			if (this.sliderValue < 0.0F) {
				this.sliderValue = 0.0F;
			}

			if (this.sliderValue > 1.0F) {
				this.sliderValue = 1.0F;
			}

			this.displayString = option.getDisplayString(sliderValue);
			this.dragging = true;
			return true;
		}
		return false;
	}

	private void updateValue() {
		switch (this.id) {
		case 1:
			gui.actionPerformed(this);
			break;
		}
	}

	@Override
	public int getHoverState(boolean flag) {
		return 0;
	}

	public void mouseReleased(int i, int j) {
		this.dragging = false;
	}
}