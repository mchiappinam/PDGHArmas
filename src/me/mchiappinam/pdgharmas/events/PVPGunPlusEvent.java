package me.mchiappinam.pdgharmas.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PVPGunPlusEvent extends Event implements Cancellable
{
    private static final HandlerList handlers;
    private boolean cancelled;
    
    static {
        handlers = new HandlerList();
    }
    
    public PVPGunPlusEvent() {
        this.cancelled = false;
    }
    
    public HandlerList getHandlers() {
        return PVPGunPlusEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PVPGunPlusEvent.handlers;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean arg0) {
        this.cancelled = arg0;
    }
}
