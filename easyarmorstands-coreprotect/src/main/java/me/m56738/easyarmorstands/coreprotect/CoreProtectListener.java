package me.m56738.easyarmorstands.coreprotect;

import me.m56738.easyarmorstands.api.element.Element;
import me.m56738.easyarmorstands.api.event.player.PlayerCommitElementEvent;
import me.m56738.easyarmorstands.api.event.player.PlayerCreateElementEvent;
import me.m56738.easyarmorstands.api.event.player.PlayerDestroyElementEvent;
import me.m56738.easyarmorstands.api.property.type.EntityPropertyTypes;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CoreProtectListener implements Listener {
    private final CoreProtectAPI api;

    public CoreProtectListener(CoreProtectAPI api) {
        this.api = api;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreate(PlayerCreateElementEvent event) {
        Location location = event.getProperties().get(EntityPropertyTypes.LOCATION).getValue();
        api.logPlacement(event.getPlayer().getName() + "-create", location, Material.ARMOR_STAND, null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommit(PlayerCommitElementEvent event) {
        Location location = getReferenceLocation(event.getElement());
        api.logRemoval(event.getPlayer().getName() + "-edit", location, Material.ARMOR_STAND, null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDestroy(PlayerDestroyElementEvent event) {
        Location location = getReferenceLocation(event.getElement());
        api.logRemoval(event.getPlayer().getName() + "-destroy", location, Material.ARMOR_STAND, null);
    }

    private @NotNull Location getReferenceLocation(Element element) {
        Element reference = element.getReference().getElement();
        if (reference == null) {
            throw new IllegalArgumentException("element reference cannot be null");
        }
        return reference.getProperties().get(EntityPropertyTypes.LOCATION).getValue();
    }

}
