package org.allaymc.bountyhunter.data;

import lombok.Data;
import java.util.UUID;

/**
 * Represents a bounty placed on a player.
 */
@Data
public class BountyData {
    private UUID targetId;
    private UUID placerId;
    private String targetName;
    private String placerName;
    private int rewardAmount;
    private long placedTime;
    private long expiryTime;
    private boolean claimed;
    private UUID claimedBy;
    private long claimedTime;
    
    public BountyData() {}
    
    public BountyData(UUID targetId, UUID placerId, String targetName, String placerName, 
                      int rewardAmount, long expiryDurationMs) {
        this.targetId = targetId;
        this.placerId = placerId;
        this.targetName = targetName;
        this.placerName = placerName;
        this.rewardAmount = rewardAmount;
        this.placedTime = System.currentTimeMillis();
        this.expiryTime = this.placedTime + expiryDurationMs;
        this.claimed = false;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
    
    public long getRemainingTime() {
        return Math.max(0, expiryTime - System.currentTimeMillis());
    }
}
