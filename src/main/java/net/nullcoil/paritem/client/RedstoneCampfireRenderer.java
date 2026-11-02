package net.nullcoil.paritem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.CampfireRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.paritem.blocks.custom.RedstoneCampfireBlock;
import net.nullcoil.paritem.blocks.custom.RedstoneCampfireBlockEntity;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RedstoneCampfireRenderer implements BlockEntityRenderer<RedstoneCampfireBlockEntity, CampfireRenderState> {
    private static final float SIZE = 0.375f;
    private final ItemModelResolver itemModelResolver;

    public RedstoneCampfireRenderer(final BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public CampfireRenderState createRenderState() { return new CampfireRenderState(); }

    public void extractRenderState(
            final RedstoneCampfireBlockEntity entity,
            final CampfireRenderState state,
            final float partialTicks,
            final Vec3 cameraPosition,
            final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
            ) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = entity.getBlockState().getValue(RedstoneCampfireBlock.FACING);
        int seed = (int) entity.getBlockPos().asLong();
        state.items = new ArrayList();

        for (int slot = 0; slot < entity.getItems().size(); slot++) {
            ItemStackRenderState itemState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(itemState, entity.getItems().get(slot), ItemDisplayContext.FIXED,
                                                    entity.getLevel(), null, seed + slot);
            state.items.add(itemState);
        }
    }

    @Override
    public void submit(CampfireRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        Direction facing = state.facing;
        List<ItemStackRenderState> items = state.items;

        for (int slot = 0; slot < items.size(); slot++) {
            ItemStackRenderState itemState = (ItemStackRenderState) items.get(slot);
            if (!itemState.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.5f, 0.44921875f, 0.5f);
                Direction direction = Direction.from2DDataValue((slot + facing.get2DDataValue()) % 4);
                float angle = -direction.toYRot();
                poseStack.mulPose(new Matrix4f().rotation(Axis.YP.rotationDegrees(angle)));
                poseStack.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees(90f)));
                poseStack.translate(-0.3125f, -0.3125f, 0f);
                poseStack.scale(0.375f, 0.375f, 0.375f);
                itemState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }
    }
}