package surreal.fixeroo.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import surreal.fixeroo.FixerooConfig;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static surreal.fixeroo.core.FixerooConstants.HOOKS;

public class FixerooTransformer implements IClassTransformer {
    private final Map<String, Function<byte[], byte[]>> transform;

    public FixerooTransformer() {
        transform = new Object2ObjectOpenHashMap<>();

        if (FixerooConfig.xpOrbClump.enable)
            transform.put("net.minecraft.entity.item.EntityXPOrb", this::transformEntityXPOrb);
        if (FixerooConfig.golemTweaks.enable)
            transform.put("net.minecraft.block.BlockPumpkin", this::transformBlockPumpkin);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        Function<byte[], byte[]> func = transform.get(transformedName);
        byte[] bytes = basicClass;

        if (func != null) {
            FixerooPlugin.LOGGER.info("Transforming " + name);
            bytes = func.apply(basicClass);
        }

        return bytes;
    }

    private byte[] transformEntityXPOrb(byte[] bytes) {
        ClassNode cls = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(cls, 0);

        for (MethodNode method : cls.methods) {
            if (method.name.equals(FixerooPlugin.deobf ? "onUpdate" : "func_70071_h_")) {
                AbstractInsnNode node = null;
                for (AbstractInsnNode n : method.instructions.toArray()) {
                    if (n.getOpcode() == INVOKEVIRTUAL) node = n.getNext();
                }

                if (node != null) {
                    method.instructions.insertBefore(node, new VarInsnNode(ALOAD, 0));
                    method.instructions.insertBefore(node, new MethodInsnNode(INVOKESTATIC, HOOKS, "updateXPOrb", "(Lnet/minecraft/entity/item/EntityXPOrb;)V", false));
                }
            }

            if (method.name.equals(FixerooPlugin.deobf ? "onCollideWithPlayer" : "func_70100_b_") && FixerooConfig.xpOrbClump.removeCooldown) {
                int start = -1;
                int end = -1;

                for (int i = 0; i < method.instructions.size(); i++) {
                    AbstractInsnNode node = method.instructions.get(i);
                    if (node.getOpcode() == GETFIELD && ((FieldInsnNode) node).name.equals(FixerooPlugin.deobf ? "delayBeforeCanPickup" : "field_70532_c")) start = i - 1;
                    if (node.getOpcode() == GETFIELD && ((FieldInsnNode) node).name.equals(FixerooPlugin.deobf ? "xpCooldown" : "field_71090_bL")) end = i + 2;
                }

                if (start > -1 && end > -1) {
                    Iterator<AbstractInsnNode> nodes = method.instructions.iterator(start);
                    int i = start;

                    while (nodes.hasNext() && i < end) {
                        method.instructions.remove(nodes.next());
                        i++;
                    }
                }

                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cls.accept(writer);
        return writer.toByteArray();
    }

    private byte[] transformBlockPumpkin(byte[] bytes) {
        ClassNode cls = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(cls, 0);

        for (MethodNode method : cls.methods) {
            // Remove unnecessary random tick
            if (method.name.equals("<init>")) {
                for (AbstractInsnNode n : method.instructions.toArray()) {
                    if (n.getOpcode() == ICONST_1) {
                        for (int i = 0; i < 4; i++) {
                            method.instructions.remove(n.getPrevious());
                            n = n.getNext();
                        }

                        break;
                    }
                }
            }
            // There isn't much people use dispensers let alone know about this feature
            else if (method.name.equals(FixerooPlugin.deobf ? "canDispenserPlace" : "func_176390_d")) {
                AbstractInsnNode node = null;
                boolean remove = false;
                for (AbstractInsnNode n : method.instructions.toArray()) {
                    if (n.getOpcode() == ALOAD) remove = true;
                    if (remove) {
                        method.instructions.remove(n);
                        if (n.getOpcode() == IFNULL) {
                            node = n.getNext();
                            break;
                        }
                    }
                }

                if (node != null) {
                    InsnList list = new InsnList();

                    list.add(new VarInsnNode(ALOAD, 1));
                    // Get Pos Down
                    list.add(new VarInsnNode(ALOAD, 2));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", FixerooPlugin.deobf ? "down" : "func_177977_b", "()Lnet/minecraft/util/math/BlockPos;", false));
                    // Get BlockState Down Block
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", FixerooPlugin.deobf ? "getBlockState" : "func_180495_p", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;", false));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/block/state/IBlockState", FixerooPlugin.deobf ? "getBlock" : "func_177230_c", "()Lnet/minecraft/block/Block;", false));
                    list.add(new VarInsnNode(ASTORE, 3));

                    // Snow Block Check
                    LabelNode l1 = new LabelNode();
                    list.add(new VarInsnNode(ALOAD, 3));
                    list.add(new FieldInsnNode(GETSTATIC, "net/minecraft/init/Blocks", FixerooPlugin.deobf ? "SNOW" : "field_150433_aE", "Lnet/minecraft/block/Block;"));
                    list.add(new JumpInsnNode(IF_ACMPNE, l1));

                    // Iron Block Check
                    LabelNode l2 = new LabelNode();
                    list.add(new VarInsnNode(ALOAD, 3));
                    list.add(new FieldInsnNode(GETSTATIC, "net/minecraft/init/Blocks", FixerooPlugin.deobf ? "SNOW" : "field_150433_aE", "Lnet/minecraft/block/Block;"));
                    list.add(new JumpInsnNode(IF_ACMPNE, l2));

                    method.instructions.insertBefore(node, list);
                }
            }
            else if (method.name.equals(FixerooPlugin.deobf ? "trySpawnGolem" : "func_180673_e")) {
                AbstractInsnNode node = null;
                boolean remove = false;
                for (AbstractInsnNode n : method.instructions.toArray()) {
                    if (n.getOpcode() == ALOAD) remove = true;
                    if (n.getOpcode() == RETURN) {
                        node = n;
                        break;
                    }
                    else if (remove) method.instructions.remove(n);
                }

                if (node != null) {
                    InsnList list = new InsnList();
                    // Snowman Pattern
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/block/BlockPumpkin", FixerooPlugin.deobf ? "getSnowmanPattern" : "func_176391_l", "()Lnet/minecraft/block/state/pattern/BlockPattern;", false));
                    // Iron Golem Pattern
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/block/BlockPumpkin", FixerooPlugin.deobf ? "getGolemPattern" : "func_176388_T", "()Lnet/minecraft/block/state/pattern/BlockPattern;", false));
                    // -----------
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new VarInsnNode(ALOAD, 2));
                    list.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "trySpawnGolem$BlockPumpkin", "(Lnet/minecraft/block/state/pattern/BlockPattern;Lnet/minecraft/block/state/pattern/BlockPattern;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", false));

                    method.instructions.insertBefore(node, list);
                }
            }
            else if (method.name.equals(FixerooPlugin.deobf ? "getGolemPattern" : "func_176388_T")) {
                AbstractInsnNode node = null;
                for (AbstractInsnNode n : method.instructions.toArray()) {
                    if (n.getOpcode() == BIPUSH && ((IntInsnNode) n).operand == 126) {
                        node = n.getNext();
                        break;
                    }
                }

                if (node != null) {
                    for (int i = 0; i < 3; i++) {
                        node = node.getNext();
                        method.instructions.remove(node.getPrevious());
                    }

                    method.instructions.insertBefore(node, new FieldInsnNode(GETSTATIC, HOOKS, "ANY", "Lcom/google/common/base/Predicate;"));
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cls.accept(writer);
        return writer.toByteArray();
    }
}
