package surreal.xporbclump;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

@Mod(modid = XPOrbClump.MODID, name = XPOrbClump.NAME, version = XPOrbClump.VERSION)
public class XPOrbClump {
    public static final String MODID = "xporbclump";
    public static final String NAME = "XP Orb Clump";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void construction(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        World world = event.getWorld();

        if (entity instanceof EntityXPOrb) {
            EntityXPOrb orb = (EntityXPOrb) entity;
            double a = XPOrbConfig.general.size/2;

            List<EntityXPOrb> orbs = world.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(orb.posX-a, orb.posY-a, orb.posZ-a, orb.posX+a, orb.posY+a, orb.posZ+a));
            if (orbs.size() >= XPOrbConfig.general.orbCount && !world.isRemote) {
                EntityXPOrb xpOrb = orbs.get(0);
                xpOrb.xpValue = orb.xpValue;
                event.setCanceled(true);
            }
        }
    }
}
