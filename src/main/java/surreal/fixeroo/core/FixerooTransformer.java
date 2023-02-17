package surreal.fixeroo.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import surreal.fixeroo.FixerooConfig;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;

public class FixerooTransformer implements IClassTransformer {
    private final Map<String, Function<byte[], byte[]>> transform;
    private final Logger LOGGER = LogManager.getLogger("Fixeroo");

    private final boolean deobf = FMLLaunchHandler.isDeobfuscatedEnvironment();

    private final String HOOKS = "surreal/fixeroo/core/FixerooHooks";

    public FixerooTransformer() {
        transform = new Object2ObjectOpenHashMap<>();

        if (FixerooConfig.xpOrbClump.enable) {
            transform.put("net.minecraft.entity.item.EntityXPOrb", this::transformEntityXPOrb);

            if (FixerooConfig.xpOrbClump.changeOrbSize)
                transform.put("net.minecraft.client.renderer.entity.RenderXPOrb", this::transformRenderXPOrb);
        }

        if (FixerooConfig.golemTweaks.enable)
            transform.put("net.minecraft.block.BlockPumpkin", this::transformBlockPumpkin);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        Function<byte[], byte[]> func = transform.get(transformedName);
        byte[] bytes = basicClass;

        if (func != null) {
            LOGGER.info("Transforming " + name);
            bytes = func.apply(basicClass);
        }

        return bytes;
    }

    private byte[] transformEntityXPOrb(byte[] bytes) {
        ClassNode cls = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(cls, 0);

        for (MethodNode method : cls.methods) {
            if (method.name.equals(deobf ? "onUpdate" : "func_70071_h_")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node.getOpcode() == POP) {
                        node = node.getNext();

                        method.instructions.insertBefore(node, new VarInsnNode(ALOAD, 0));
                        method.instructions.insertBefore(node, new MethodInsnNode(INVOKESTATIC, HOOKS, "EntityXPOrb$onUpdate", "(Lnet/minecraft/entity/item/EntityXPOrb;)V", false));

                        break;
                    }
                }
            }
            else if (FixerooConfig.xpOrbClump.removeCooldown && method.name.equals(deobf ? "onCollideWithPlayer" : "func_70100_b_")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                boolean remove = false;

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (remove) iterator.remove();

                    if (node instanceof LineNumberNode && ((LineNumberNode) node).line == 243) {
                        remove = true;
                    } else if (node instanceof LabelNode && remove) break;
                }

                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cls.accept(writer);
        return writer.toByteArray();
    }

    private byte[] transformRenderXPOrb(byte[] bytes) {
        ClassNode cls = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(cls, 0);

        for (MethodNode method : cls.methods) {
            if (method.name.equals(deobf ? "doRender" : "func_76986_a")) {
                Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
                boolean first = true;

                while (iterator.hasNext()) {
                    AbstractInsnNode node = iterator.next();

                    if (node instanceof LdcInsnNode) {
                        Object cst = ((LdcInsnNode) node).cst;

                        if (cst instanceof Float && cst.equals(0.3F)) {
                            if (first) {
                                first = false;
                                method.instructions.insertBefore(node, new VarInsnNode(ALOAD, 1));
                                method.instructions.insertBefore(node, new MethodInsnNode(INVOKESTATIC, HOOKS, "RenderXPOrb$getSize", "(Lnet/minecraft/entity/item/EntityXPOrb;)F", false));
                            } else {
                                method.instructions.insertBefore(node, new VarInsnNode(FLOAD, 25));
                            }

                            iterator.remove();
                        }
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
            else if (method.name.equals(deobf ? "canDispenserPlace" : "func_176390_d")) {
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
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/util/math/BlockPos", deobf ? "down" : "func_177977_b", "()Lnet/minecraft/util/math/BlockPos;", false));
                    // Get BlockState Down Block
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", deobf ? "getBlockState" : "func_180495_p", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;", false));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/block/state/IBlockState", deobf ? "getBlock" : "func_177230_c", "()Lnet/minecraft/block/Block;", false));
                    list.add(new VarInsnNode(ASTORE, 3));

                    // Snow Block Check
                    LabelNode l1 = new LabelNode();
                    list.add(new VarInsnNode(ALOAD, 3));
                    list.add(new FieldInsnNode(GETSTATIC, "net/minecraft/init/Blocks", deobf ? "SNOW" : "field_150433_aE", "Lnet/minecraft/block/Block;"));
                    list.add(new JumpInsnNode(IF_ACMPNE, l1));

                    // Iron Block Check
                    LabelNode l2 = new LabelNode();
                    list.add(new VarInsnNode(ALOAD, 3));
                    list.add(new FieldInsnNode(GETSTATIC, "net/minecraft/init/Blocks", deobf ? "SNOW" : "field_150433_aE", "Lnet/minecraft/block/Block;"));
                    list.add(new JumpInsnNode(IF_ACMPNE, l2));

                    method.instructions.insertBefore(node, list);
                }
            }
            else if (method.name.equals(deobf ? "trySpawnGolem" : "func_180673_e")) {
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
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/block/BlockPumpkin", deobf ? "getSnowmanPattern" : "func_176391_l", "()Lnet/minecraft/block/state/pattern/BlockPattern;", false));
                    // Iron Golem Pattern
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/block/BlockPumpkin", deobf ? "getGolemPattern" : "func_176388_T", "()Lnet/minecraft/block/state/pattern/BlockPattern;", false));
                    // -----------
                    list.add(new VarInsnNode(ALOAD, 1));
                    list.add(new VarInsnNode(ALOAD, 2));
                    list.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "BlockPumpkin$trySpawnGolem", "(Lnet/minecraft/block/state/pattern/BlockPattern;Lnet/minecraft/block/state/pattern/BlockPattern;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", false));

                    method.instructions.insertBefore(node, list);
                }
            }
            else if (method.name.equals(deobf ? "getGolemPattern" : "func_176388_T")) {
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

                    method.instructions.insertBefore(node, new MethodInsnNode(INVOKESTATIC, HOOKS, "BlockPumpkin$predicateAny", "()Lcom/google/common/base/Predicate;", false));
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cls.accept(writer);
        return writer.toByteArray();
    }

    /*    private byte[] transformTest(byte[] bytes) {
        ClassNode cls = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(cls, 0);

        for (MethodNode method : cls.methods) {
            if (method.name.equals("isTest")) {
                for (AbstractInsnNode node : method.instructions.toArray()) {
                    if (node.getOpcode() == IRETURN) {
                        InsnList list = new InsnList();

                        LabelNode l1 = new LabelNode();
                        list.add(new JumpInsnNode(IFNE, l1));

                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new FieldInsnNode(GETFIELD, "surreal/fixeroo/Fixeroo", "test", "Ljava/lang/String;"));
                        list.add(new LdcInsnNode("t"));
                        list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false));
                        LabelNode l2 = new LabelNode();
                        list.add(new JumpInsnNode(IFEQ, l2));

                        list.add(new JumpInsnNode(IFNE, l1));
                        list.add(new VarInsnNode(ILOAD, 1));
                        LabelNode l4 = new LabelNode();
                        list.add(new JumpInsnNode(IFEQ, l4));

                        list.add(l1);
                        list.add(new FrameNode(F_SAME1, 0, null, 1, new Object[] { INTEGER }));
                        list.add(new InsnNode(ICONST_1));

                        LabelNode l3 = new LabelNode();
                        list.add(new JumpInsnNode(GOTO, l3));
                        list.add(l2);
                        list.add(new FrameNode(F_SAME, 0, null, 0, null));
                        list.add(new InsnNode(ICONST_0));
                        list.add(l4);
                        list.add(new FrameNode(F_SAME, 0, null, 0, null));
                        list.add(new InsnNode(ICONST_0));
                        list.add(l3);
                        list.add(new FrameNode(F_SAME1, 0, null, 1, new Object[] { INTEGER }));
                        method.instructions.insertBefore(node, list);
                        break;
                    }
                }
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cls.accept(writer);
        return writer.toByteArray();
    }*/
}
