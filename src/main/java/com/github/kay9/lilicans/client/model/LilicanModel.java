package com.github.kay9.lilicans.client.model;

import com.github.kay9.lilicans.Lilicans;
import com.github.kay9.lilicans.entity.Lilican;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class LilicanModel extends EntityModel<Lilican>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Lilicans.id(Lilicans.LILICAN.getId().getPath()), "main");

    private final Map<ModelPart, PartPose> defaultPositions = new HashMap<>();
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart pad;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public LilicanModel(ModelPart parent)
    {
        super(RenderType::entityCutoutNoCull);

        body = getChild(parent, "body");
        head = getChild(body, "head");
        pad = getChild(head, "pad");
        leftArm = getChild(body, "left_arm");
        rightArm = getChild(body, "right_arm");
        leftLeg = getChild(body, "left_leg");
        rightLeg = getChild(body, "right_leg");
    }

    public ModelPart getChild(ModelPart parent, String child)
    {
        ModelPart part = parent.getChild(child);
        defaultPositions.put(part, part.storePose());
        return part;
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bodyBranch = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 22).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0, 20.5f, 0));
        PartDefinition headBranch = bodyBranch.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 15).addBox(-3f, -3f, -2f, 6f, 3f, 4f), PartPose.offset(0, -1.5f, 0));
        headBranch.addOrReplaceChild("pad", CubeListBuilder.create().addBox(-6f, -3.02f, -6f, 12f, 3f, 12f), PartPose.offset(0, -3f, 0));
        bodyBranch.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 3).addBox(-1f, -0.5f, -0.5f, 1f, 3f, 1f), PartPose.offset(-1.5f, 0, 0));
        bodyBranch.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 3).addBox(0, -0.5f, -0.5f, 1f, 3f, 1f), PartPose.offset(1.5f, 0, 0));
        bodyBranch.addOrReplaceChild("right_leg", CubeListBuilder.create().addBox(-0.5f, 0, -0.5f, 1f, 2f, 1f), PartPose.offset(1f, 1.5f, 0));
        bodyBranch.addOrReplaceChild("left_leg", CubeListBuilder.create().addBox(-0.5f, 0, -0.5f, 1f, 2f, 1f), PartPose.offset(-1f, 1.5f, 0));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Lilican pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {
        defaultPositions.forEach(ModelPart::loadPose);

        head.xRot = pHeadPitch * ((float) Math.PI / 180f);
        head.yRot = pNetHeadYaw * ((float) Math.PI / 180f);

        if (pEntity.isInSittingPose())
        {
            body.y += 1.5f;
            leftLeg.z = rightLeg.z = -1f;
            leftLeg.xRot = rightLeg.xRot = -1.5f;
            leftLeg.yRot = -(rightLeg.yRot = -0.5f);
        }
    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
    {
        body.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
    }

    public static class FlowerLayer extends RenderLayer<Lilican, LilicanModel>
    {
        public FlowerLayer(RenderLayerParent<Lilican, LilicanModel> parent)
        {
            super(parent);
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource pBuffer, int pPackedLight, Lilican lilican, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
        {
            if (lilican.isTame())
            {
                Minecraft minecraft = Minecraft.getInstance();
                boolean flag = minecraft.shouldEntityAppearGlowing(lilican) && lilican.isInvisible();
                if (!lilican.isInvisible() || flag)
                {
                    BlockRenderDispatcher renderer = minecraft.getBlockRenderer();
                    BlockState daisy = Blocks.OXEYE_DAISY.defaultBlockState();
                    int overlay = LivingEntityRenderer.getOverlayCoords(lilican, 0.0F);
                    BakedModel bakedmodel = renderer.getBlockModel(daisy);
                    ps.pushPose();
                    getParentModel().body.translateAndRotate(ps);
                    getParentModel().head.translateAndRotate(ps);
                    ps.translate(0, -0.24, 0);
                    ps.scale(-1.0F, -1.0F, 1.0F);
                    ps.scale(0.6f, 0.6f, 0.6f);
                    ps.translate(-0.5f, -0.5f, -0.5f);
                    if (flag)
                        renderer.getModelRenderer().renderModel(ps.last(), pBuffer.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), daisy, bakedmodel, 0, 0, 0, pPackedLight, overlay);
                    else renderer.renderSingleBlock(daisy, ps, pBuffer, pPackedLight, overlay);
                    ps.popPose();
                }
            }
        }
    }
}
