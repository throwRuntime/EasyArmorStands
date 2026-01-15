package me.m56738.easyarmorstands.coreprotect;

import me.m56738.easyarmorstands.EasyArmorStandsPlugin;
import me.m56738.easyarmorstands.addon.Addon;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public class CoreProtectAddon implements Addon {

    @Override
    public String name() {
        return "CoreProtect";
    }

    @Override
    public void enable() {
        Plugin coreProtectPlugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");

        if (!(coreProtectPlugin instanceof CoreProtect)) return;
        CoreProtectAPI coreProtectAPI = ((CoreProtect) coreProtectPlugin).getAPI();

        if (coreProtectAPI.isEnabled()) {
            Listener listener = new CoreProtectListener(coreProtectAPI);
            Bukkit.getServer().getPluginManager().registerEvents(listener, EasyArmorStandsPlugin.getInstance());
        } else {
            EasyArmorStandsPlugin.getInstance().getLogger()
                    .log(Level.SEVERE, "Failed to enable CoreProtect addon. Enable `api-enabled` in CoreProtect config.");
        }
    }

    @Override
    public void disable() {
    }

    @Override
    public void reload() {
    }
}
