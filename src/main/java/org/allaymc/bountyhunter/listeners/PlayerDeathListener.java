package org.allaymc.bountyhunter.listeners;

import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.entity.EntityDieEvent;
import org.allaymc.api.server.Server;
import org.allaymc.bountyhunter.data.BountyData;
import org.allaymc.bountyhunter.data.BountyManager;

import java.util.UUID;

public class PlayerDeathListener {
    
    private final BountyManager bountyManager;
    
    public PlayerDeathListener(BountyManager bountyManager) {
        this.bountyManager = bountyManager;
    }
    
    @EventHandler
    public void onEntityDie(EntityDieEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer victim)) {
            return;
        }
        
        UUID victimId = victim.getUniqueId();
        
        if (!bountyManager.hasActiveBounty(victimId)) {
            return;
        }
        
        var damageSource = victim.getLastDamage();
        if (damageSource == null || damageSource.getAttacker() == null) {
            return;
        }
        
        var attacker = damageSource.getAttacker();
        if (!(attacker instanceof EntityPlayer hunter)) {
            return;
        }
        
        UUID hunterId = hunter.getUniqueId();
        BountyData bounty = bountyManager.getBounty(victimId);
        
        if (bounty.getPlacerId().equals(hunterId)) {
            return;
        }
        
        boolean claimed = bountyManager.claimBounty(victimId, hunterId);
        if (claimed) {
            int reward = bounty.getRewardAmount();
            hunter.sendMessage("You claimed a bounty of " + reward + " coins for eliminating " + victim.getDisplayName() + "!");
            victim.sendMessage("Your bounty was claimed by " + hunter.getDisplayName() + "!");
            Server.getInstance().getMessageChannel().broadcastMessage(hunter.getDisplayName() + " claimed the " + reward + " coin bounty on " + victim.getDisplayName() + "!");
        }
    }
}
