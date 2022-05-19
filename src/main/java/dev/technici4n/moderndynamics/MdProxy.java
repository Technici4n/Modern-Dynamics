package dev.technici4n.moderndynamics;

import net.fabricmc.loader.api.FabricLoader;

public class MdProxy {
	public static final MdProxy INSTANCE = switch (FabricLoader.getInstance().getEnvironmentType()) {
		case SERVER -> new MdProxy();
		case CLIENT -> {
			try {
				yield (MdProxy) Class.forName("dev.technici4n.moderndynamics.client.ClientProxy").getConstructor().newInstance();
			} catch (Exception exception) {
				throw new RuntimeException("Failed to instantiate Modern Dynamics client proxy.", exception);
			}
		}
	};

	public boolean isShiftDown() {
		return false;
	}
}
