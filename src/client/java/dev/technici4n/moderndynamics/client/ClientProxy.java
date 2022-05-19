package dev.technici4n.moderndynamics.client;

import dev.technici4n.moderndynamics.MdProxy;
import net.minecraft.client.gui.screens.Screen;

public class ClientProxy extends MdProxy {
	@Override
	public boolean isShiftDown() {
		return Screen.hasShiftDown();
	}
}
