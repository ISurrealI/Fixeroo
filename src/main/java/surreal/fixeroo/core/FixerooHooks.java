package surreal.fixeroo.core;

import com.google.common.base.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import surreal.fixeroo.FixerooConfig;

import java.util.List;

@SuppressWarnings("unused")
public class FixerooHooks {
    public static final Predicate<BlockWorldState> ANY = state -> true;

    public static void EntityXPOrb$update(EntityXPOrb orb) {
        World world = orb.world;
        double a = FixerooConfig.xpOrbClump.size/2;

        List<EntityXPOrb> orbs = world.getEntitiesWithinAABB(EntityXPOrb.class, new AxisAlignedBB(orb.posX-a, orb.posY-a, orb.posZ-a, orb.posX+a, orb.posY+a, orb.posZ+a), entity -> entity != null && entity.posX != orb.posX && entity.posY != orb.posY && entity.posZ != orb.posZ);
        if (orbs.size() >= FixerooConfig.xpOrbClump.orbCount && !world.isRemote) {
            EntityXPOrb xpOrb = orbs.get(0);
            xpOrb.xpValue += orb.xpValue;
            orb.setDead();
        }
    }

    public static void BlockPumpkin$trySpawnGolem(BlockPattern snowman, BlockPattern ironGolem, World worldIn, BlockPos pos) {
        Block blockBelow = worldIn.getBlockState(pos.down()).getBlock();
        boolean isSnowMan = false;

        BlockPattern.PatternHelper pattern = null;
        if (blockBelow == Blocks.SNOW) {
            pattern = snowman.match(worldIn, pos);
            isSnowMan = true;
        }
        else if (blockBelow == Blocks.IRON_BLOCK) pattern = ironGolem.match(worldIn, pos);

        if (pattern != null) {
            if (isSnowMan) {
                int i;
                for (i = 0; i < snowman.getThumbLength(); i++) {
                    BlockPos p = pattern.translateOffset(0, i, 0).getPos();
                    worldIn.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                    worldIn.notifyNeighborsRespectDebug(p, Blocks.AIR, false);
                }

                EntitySnowman entitysnowman = new EntitySnowman(worldIn);
                BlockPos blockpos1 = pattern.translateOffset(0, 2, 0).getPos();
                entitysnowman.setLocationAndAngles((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.05D, (double)blockpos1.getZ() + 0.5D, 0.0F, 0.0F);
                worldIn.spawnEntity(entitysnowman);

                for (EntityPlayerMP player : worldIn.getEntitiesWithinAABB(EntityPlayerMP.class, entitysnowman.getEntityBoundingBox().grow(5.0D))) {
                    CriteriaTriggers.SUMMONED_ENTITY.trigger(player, entitysnowman);
                }
            } else {
                for (int i = 0; i < ironGolem.getThumbLength(); i++) {
                    if (i == 1) {
                        for (int g = 0; g < ironGolem.getPalmLength(); g++) {
                            BlockPos p = pattern.translateOffset(g, i, 0).getPos();
                            worldIn.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                            worldIn.notifyNeighborsRespectDebug(p, Blocks.AIR, false);
                        }
                    } else {
                        BlockPos p = pattern.translateOffset(1, i, 0).getPos();
                        worldIn.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
                        worldIn.notifyNeighborsRespectDebug(p, Blocks.AIR, false);
                    }
                }

                BlockPos blockpos = pattern.translateOffset(1, 2, 0).getPos();
                EntityIronGolem entityirongolem = new EntityIronGolem(worldIn);
                entityirongolem.setPlayerCreated(true);
                entityirongolem.setLocationAndAngles((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.05D, (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
                worldIn.spawnEntity(entityirongolem);

                for (EntityPlayerMP player : worldIn.getEntitiesWithinAABB(EntityPlayerMP.class, entityirongolem.getEntityBoundingBox().grow(5.0D))) {
                    CriteriaTriggers.SUMMONED_ENTITY.trigger(player, entityirongolem);
                }
            }
        }
    }
}
