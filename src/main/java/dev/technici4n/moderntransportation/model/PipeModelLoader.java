package dev.technici4n.moderntransportation.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraftforge.client.model.IModelLoader;
import org.lwjgl.system.CallbackI;

public class PipeModelLoader implements IModelLoader<PipeModelGeometry> {
    @Override
    public void apply(ResourceManager arg) {
    }

    @Override
    public PipeModelGeometry read(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
        Identifier textureId = new Identifier(JsonHelper.getString(jsonObject, "texture"));
        Identifier centerModel = new Identifier(JsonHelper.getString(jsonObject, "center"));
        Identifier connectionModel = new Identifier(JsonHelper.getString(jsonObject, "side"));

        return new PipeModelGeometry(textureId, centerModel, connectionModel);
    }
}
