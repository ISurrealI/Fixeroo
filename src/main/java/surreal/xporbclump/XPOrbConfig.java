package surreal.xporbclump;

import net.minecraftforge.common.config.Config;

@Config(modid = XPOrbClump.MODID)
public class XPOrbConfig {
    public static General general = new General();

    public static class General {
        @Config.Comment("size of checking")
        public double size = 4D;

        @Config.Comment("how many xp orbs can be in that area")
        public int orbCount = 1;

        @Config.Comment("remove xp collecting cooldown")
        public boolean removeCooldown = true;
    }
}
