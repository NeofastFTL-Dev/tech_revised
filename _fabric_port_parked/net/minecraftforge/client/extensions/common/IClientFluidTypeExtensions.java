package net.minecraftforge.client.extensions.common;
import net.minecraft.resources.ResourceLocation;
public interface IClientFluidTypeExtensions {
    ResourceLocation getStillTexture();
    ResourceLocation getFlowingTexture();
    default int getTintColor() { return 0xFFFFFFFF; }
}
