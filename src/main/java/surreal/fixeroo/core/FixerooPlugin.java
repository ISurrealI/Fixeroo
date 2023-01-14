package surreal.fixeroo.core;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name(FixerooPlugin.NAME)
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(1249)
public class FixerooPlugin implements IFMLLoadingPlugin {
    public static final String NAME = "Fixeroo";
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public static boolean deobf = FMLLaunchHandler.isDeobfuscatedEnvironment();

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return "surreal.fixeroo.core.FixerooTransformer";
    }
}