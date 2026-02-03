package org.allaymc.bountyhunter;

import lombok.Getter;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import org.allaymc.bountyhunter.commands.BountyCommand;
import org.allaymc.bountyhunter.data.BountyManager;
import org.allaymc.bountyhunter.listeners.PlayerDeathListener;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BountyHunterPlugin extends Plugin {
    
    @Getter
    private static BountyHunterPlugin instance;
    
    @Getter
    private BountyManager bountyManager;
    
    private PlayerDeathListener deathListener;
    private final Set<String> activeCleanupTasks = ConcurrentHashMap.newKeySet();

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

        // Schedule periodic cleanup of expired bounties (every hour)
        String taskId = UUID.randomUUID().toString();
        activeCleanupTasks.add(taskId);
        Server.getInstance().getScheduler().scheduleRepeating(this, () -> {
            if (!activeCleanupTasks.contains(taskId)) {
                return false; // Stop this task
            }
            int removed = bountyManager.cleanupExpiredBounties();
            if (removed > 0) {
                this.pluginLogger.info("[BountyHunter] Cleaned up " + removed + " expired bounties.");
            }
            return true;
        }, 20 * 60 * 60); // Every hour (60 minutes * 60 seconds)

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

        // Stop all cleanup tasks by clearing the tracking set
        activeCleanupTasks.clear();
        // Tasks will stop on their next run when they check the tracking set

        this.pluginLogger.info("[BountyHunter] Plugin disabled.");
    }
}
