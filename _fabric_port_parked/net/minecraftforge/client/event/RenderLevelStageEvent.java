package net.minecraftforge.client.event;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
public class RenderLevelStageEvent {
    public enum Stage { AFTER_TRANSLUCENT_BLOCKS }
    public Stage getStage() { return Stage.AFTER_TRANSLUCENT_BLOCKS; }
    public PoseStack getPoseStack() { return new PoseStack(); }
    public Camera getCamera() { return null; }
}
