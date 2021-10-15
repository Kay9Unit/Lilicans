package com.github.kay9.lilicans.client.render;

import com.github.kay9.lilicans.Lilicans;
import com.github.kay9.lilicans.client.model.LilicanModel;
import com.github.kay9.lilicans.entity.Lilican;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LilicanRenderer extends MobRenderer<Lilican, LilicanModel>
{
    public static final ResourceLocation TEXTURE = Lilicans.id("textures/entity/lilican.png");

    public LilicanRenderer(EntityRendererProvider.Context context)
    {
        super(context, new LilicanModel(context.bakeLayer(LilicanModel.LAYER_LOCATION)), 0.45f);
        addLayer(new LilicanModel.FlowerLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Lilican lilican)
    {
        return TEXTURE;
    }
}
