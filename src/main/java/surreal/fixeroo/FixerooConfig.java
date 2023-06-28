package surreal.fixeroo;

import net.minecraftforge.common.config.Config;

@Config(modid = Fixeroo.MODID)
public class FixerooConfig {
    public static XPOrbClump xpOrbClump = new XPOrbClump();
    public static GolemTweaks golemTweaks = new GolemTweaks();

    public static class XPOrbClump {
        @Config.Comment("Enable xp orb clumping")
        public boolean enable = true;

        @Config.Comment("Remove xp collecting cooldown")
        public boolean removeCooldown = true;

        @Config.Comment("Size of checking area")
        public double areaSize = 4D;

        @Config.Comment("How many xp orbs can be in that area")
        public int maxOrbCount = 1;

        public boolean changeOrbSize = false;
    }

    public static class GolemTweaks {
        @Config.Comment("Enable fixes to golem")
        public boolean enable = true;
    }
}
