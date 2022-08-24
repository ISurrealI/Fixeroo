package surreal.xporbclump.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import surreal.xporbclump.XPOrbConfig;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.*;

public class ClumpClassTransformer implements IClassTransformer {
    public static String HOOKS = Type.getInternalName(ClumpHooks.class);

    public static final String EntityXPOrb = "net.minecraft.entity.item.EntityXPOrb";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(EntityXPOrb)) {
            ClumpLoadingPlugin.LOGGER.info("Manipulating " + transformedName);
            return transform(basicClass);
        }
        return basicClass;
    }

    private byte[] transform(byte[] classBeingTransformed) {
        try {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(classBeingTransformed);
            classReader.accept(classNode, 0);

            entityXPOrb(classNode);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(classWriter);
            return classWriter.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classBeingTransformed;
    }

    public static void entityXPOrb(ClassNode cls) {
        for (MethodNode method : cls.methods) {
            if (method.name.equals(ClumpLoadingPlugin.deobf ? "onUpdate" : "func_70071_h_")) {
                AbstractInsnNode node = null;

                for (AbstractInsnNode n : method.instructions.toArray()) {
                    if (n.getOpcode() == RETURN) {
                        node = n.getPrevious();
                        break;
                    }
                }

                if (node != null) {
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(INVOKESTATIC, HOOKS, "updateXPOrb", "(Lnet/minecraft/entity/item/EntityXPOrb;)V", false));
                    method.instructions.insertBefore(node, list);
                }

                if (!XPOrbConfig.general.removeCooldown) break;
            }

            if (method.name.equals(ClumpLoadingPlugin.deobf ? "onCollideWithPlayer" : "func_70100_b_")) {
                int start = -1;
                int end = -1;

                for (int i = 0; i < method.instructions.size(); i++) {
                    AbstractInsnNode node = method.instructions.get(i);
                    if (node.getOpcode() == GETFIELD && ((FieldInsnNode) node).name.equals("delayBeforeCanPickup")) start = i - 1;
                    if (node.getOpcode() == GETFIELD && ((FieldInsnNode) node).name.equals("xpCooldown")) end = i + 2;
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
    }
}
