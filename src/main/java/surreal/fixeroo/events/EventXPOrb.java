package surreal.fixeroo.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.registries.GameData;
import surreal.fixeroo.FixerooConfig;

import javax.annotation.Nonnull;
import java.util.List;

public class EventXPOrb {
    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        World world = event.getWorld();

        if (entity instanceof EntityXPOrb) {
            EntityXPOrb orb = (EntityXPOrb) entity;
            double a = FixerooConfig.xpOrbClump.size/2;

            List<EntityXPOrb> orbs = world.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(orb.posX-a, orb.posY-a, orb.posZ-a, orb.posX+a, orb.posY+a, orb.posZ+a));
            if (orbs.size() >= FixerooConfig.xpOrbClump.orbCount && !world.isRemote) {
                EntityXPOrb xpOrb = orbs.get(0);
                xpOrb.xpValue += orb.xpValue;
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void remapLegacyClumps(@Nonnull RegistryEvent.MissingMappings<EntityEntry> event) {
        for(RegistryEvent.MissingMappings.Mapping<EntityEntry> mapping : event.getAllMappings()) {
            if(mapping.key.equals(new ResourceLocation("clumps", "xp_orb_big"))) {
                mapping.remap(GameData.getEntityRegistry().getValue(2));
                return;
            }
        }
    }
}
