package org.allaymc.bountyhunter;

import lombok.Getter;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import org.allaymc.bountyhunter.commands.BountyCommand;
import org.allaymc.bountyhunter.data.BountyManager;
import org.allaymc.bountyhunter.listeners.PlayerDeathListener;

public class BountyHunterPlugin extends Plugin {
    
    @Getter
    private static BountyHunterPlugin instance;
    
    @Getter
    private BountyManager bountyManager;
    
    private PlayerDeathListener deathListener;
    
    @Override
    public void onLoad() {
        instance = this;
        this.pluginLogger.info("[BountyHunter] Plugin is loading...");
        
        this.bountyManager = new BountyManager(this);
        this.bountyManager.loadData();
        
        this.pluginLogger.info("[BountyHunter] Data loaded successfully!");
    }
    
    @Override
    public void onEnable() {
        this.pluginLogger.info("[BountyHunter] Plugin is enabling...");
        
        this.deathListener = new PlayerDeathListener(bountyManager);
        Server.getInstance().getEventBus().registerListener(this.deathListener);
        
        Registries.COMMANDS.register(new BountyCommand(bountyManager));
        
        this.pluginLogger.info("[BountyHunter] Plugin enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        this.pluginLogger.info("[BountyHunter] Plugin is disabling...");
        
        if (bountyManager != null) {
            bountyManager.saveData();
        }
        
        if (deathListener != null) {
            Server.getInstance().getEventBus().unregisterListener(this.deathListener);
        }
        
        this.pluginLogger.info("[BountyHunter] Plugin disabled.");
    }
}
