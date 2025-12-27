package dev.technici4n.moderndynamics.client.model;

import com.google.common.base.Suppliers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.client.renderer.item.BlockModelWrapper.computeExtents;

public record PipeModelSubPart(
        List<BakedQuad> quads,
        Supplier<Vector3fc[]> extents,
        ModelRenderProperties renderProperties,
        @Nullable RenderType renderType) implements BlockModelPart {

    public PipeModelSubPart(List<BakedQuad> quads, ModelRenderProperties renderProperties, @Nullable RenderType renderType) {
        this(quads, Suppliers.memoize(() -> computeExtents(quads)), renderProperties, renderType);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return direction == null ? quads : List.of();
    }

    @Override
    public TriState ambientOcclusion() {
        return TriState.FALSE;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return renderProperties.particleIcon();
    }

    @Override
    public ChunkSectionLayer getRenderType(BlockState state) {
        return ChunkSectionLayer.CUTOUT;
    }

    public static PipeModelSubPart bake(ModelBaker modelBaker, Identifier id, ModelState pipeBakeSetting) {
        var baseModel = modelBaker.getModel(id);

        var baseModelTextures = baseModel.getTopTextureSlots();
        List<BakedQuad> baseModelQuads = baseModel
                .bakeTopGeometry(baseModelTextures, modelBaker, pipeBakeSetting).getAll();

        var modelRenderProperties = ModelRenderProperties.fromResolvedModel(modelBaker, baseModel, baseModelTextures);
        var renderTypeGroup = baseModel.getTopAdditionalProperties().getOptional(NeoForgeModelProperties.RENDER_TYPE);
        var renderType = renderTypeGroup == null ? null : renderTypeGroup.entityBlock();

        return new PipeModelSubPart(baseModelQuads, modelRenderProperties, renderType);
    }

    public void applyToLayer(ItemStackRenderState.LayerRenderState layer, ItemDisplayContext context) {
        layer.setExtents(extents);
        renderProperties.applyToLayer(layer, context);
        layer.prepareQuadList().addAll(quads);
        if (renderType != null) {
            layer.setRenderType(renderType);
        } else {
            layer.setRenderType(Sheets.translucentBlockItemSheet());
        }
    }
}
