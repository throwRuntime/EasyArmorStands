package me.m56738.easyarmorstands.coreprotect;

import me.m56738.easyarmorstands.addon.AddonFactory;
import org.bukkit.Bukkit;

public class CoreProtectAddonFactory implements AddonFactory<CoreProtectAddon> {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("CoreProtect") != null;
    }

    @Override
    public CoreProtectAddon create() {
        return new CoreProtectAddon();
    }
}
