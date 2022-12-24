package surreal.fixeroo.core;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import surreal.fixeroo.FixerooConfig;

import java.util.List;

@SuppressWarnings("unused")
public class FixerooHooks {
    public static void updateXPOrb(EntityXPOrb orb) {
        World world = orb.world;
        double a = FixerooConfig.xpOrbClump.size/2;

        List<EntityXPOrb> orbs = world.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(orb.posX-a, orb.posY-a, orb.posZ-a, orb.posX+a, orb.posY+a, orb.posZ+a), entity -> entity != null && entity.posX != orb.posX && entity.posY != orb.posY && entity.posZ != orb.posZ);
        if (orbs.size() >= FixerooConfig.xpOrbClump.orbCount && !world.isRemote) {
            EntityXPOrb xpOrb = orbs.get(0);
            xpOrb.xpValue += orb.xpValue;
            orb.setDead();
        }
    }
}
