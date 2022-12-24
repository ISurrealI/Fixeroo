package surreal.fixeroo;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import surreal.fixeroo.events.EventXPOrb;

@Mod(modid = Fixeroo.MODID, name = Fixeroo.NAME, version = Fixeroo.VERSION)
public class Fixeroo {
    public static final String MODID = "xporbclump";
    public static final String NAME = "Fixeroo";
    public static final String VERSION = "1.3";

    @Mod.EventHandler
    public void construction(FMLConstructionEvent event) {
        if (FixerooConfig.xpOrbClump.enable) MinecraftForge.EVENT_BUS.register(EventXPOrb.class);
    }
}
