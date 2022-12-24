package surreal.fixeroo.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public abstract class AbstractClassTransformer implements IClassTransformer {
    private final String name;
    protected static final String HOOKS = Type.getInternalName(FixerooHooks.class);

    public AbstractClassTransformer(String name) {
        this.name = name;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return transform(transformedName, basicClass);
    }

    private byte[] transform(String transformedName, byte[] clazz) {
        if (transformedName.equals(name)) {
            try {
                ClassNode classNode = new ClassNode();
                ClassReader classReader = new ClassReader(clazz);
                classReader.accept(classNode, 0);

                FixerooPlugin.LOGGER.info("Transforming " + transformedName + " class...");

                transform(transformedName, classNode);
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                return writer.toByteArray();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return clazz;
    }

    public abstract void transform(String transformedName, ClassNode cls);
}
