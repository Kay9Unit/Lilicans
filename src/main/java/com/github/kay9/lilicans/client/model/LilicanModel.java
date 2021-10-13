package com.github.kay9.lilicans.client.model;

import com.github.kay9.lilicans.entity.Lilican;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;

public class LilicanModel extends EntityModel<Lilican>
{
    public LilicanModel()
    {
        super(RenderType::entityCutoutNoCull);
    }

    @Override
    public void setupAnim(Lilican pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch)
    {

    }

    @Override
    public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha)
    {

    }
}