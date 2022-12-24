package surreal.fixeroo.core.transformers;

import org.objectweb.asm.tree.*;
import surreal.fixeroo.FixerooConfig;
import surreal.fixeroo.core.AbstractClassTransformer;
import surreal.fixeroo.core.FixerooPlugin;

import java.util.Iterator;

import static org.objectweb.asm.Opcodes.*;

public class EntityXPOrbTransformer extends AbstractClassTransformer {
    public static final String entityXPOrb = "net.minecraft.entity.item.EntityXPOrb";

    public EntityXPOrbTransformer() {
        super(entityXPOrb);
    }

    @Override
    public void transform(String transformedName, ClassNode cls) {
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
    }
}
