package net.minecraftforge.fml;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.api.distmarker.Dist;
import java.util.function.Supplier;
public class DistExecutor {
    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        EnvType env = FabricLoader.getInstance().getEnvironmentType();
        if (dist == Dist.CLIENT && env == EnvType.CLIENT) {
            toRun.get().run();
        } else if (dist == Dist.DEDICATED_SERVER && env == EnvType.SERVER) {
            toRun.get().run();
        }
    }
}
