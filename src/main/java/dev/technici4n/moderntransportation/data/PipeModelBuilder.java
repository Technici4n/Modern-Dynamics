package dev.technici4n.moderntransportation.data;

import com.google.gson.JsonObject;
import dev.technici4n.moderntransportation.model.PipeModelLoader;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Builds model JSON files for {@link dev.technici4n.moderntransportation.model.PipeModelLoader}
 */
public class PipeModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

	public static <T extends ModelBuilder<T>> PipeModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
		return new PipeModelBuilder<>(parent, existingFileHelper);
	}

	ModelBuilder<?> connectionNone;
	ModelBuilder<?> connectionPipe;
	ModelBuilder<?> connectionInventory;

	protected PipeModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
		super(PipeModelLoader.ID, parent, existingFileHelper);
	}

	public PipeModelBuilder<T> connectionNone(ModelBuilder<?> model) {
		this.connectionNone = model;
		return this;
	}

	public PipeModelBuilder<T> connectionPipe(ModelBuilder<?> model) {
		this.connectionPipe = model;
		return this;
	}

	public PipeModelBuilder<T> connectionInventory(ModelBuilder<?> model) {
		this.connectionInventory = model;
		return this;
	}

	@Override
	public JsonObject toJson(JsonObject json) {
		json = super.toJson(json);
		json.addProperty("connection_none", connectionNone.getLocation().toString());
		json.addProperty("connection_pipe", connectionPipe.getLocation().toString());
		json.addProperty("connection_inventory", connectionInventory.getLocation().toString());
		return json;
	}
}
