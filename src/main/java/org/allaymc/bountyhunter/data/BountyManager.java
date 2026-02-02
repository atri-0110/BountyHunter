package org.allaymc.bountyhunter.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import org.allaymc.api.server.Server;
import org.allaymc.bountyhunter.BountyHunterPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BountyManager {
    
    private static final long DEFAULT_EXPIRY_MS = 7 * 24 * 60 * 60 * 1000L;
    private static final int MIN_BOUNTY_AMOUNT = 100;
    private static final int MAX_ACTIVE_BOUNTIES_PER_PLAYER = 5;
    
    private final BountyHunterPlugin plugin;
    private final Gson gson;
    private final Map<UUID, BountyData> activeBounties;
    private final File dataFile;
    
    @Getter
    private long expiryDurationMs = DEFAULT_EXPIRY_MS;
    
    public BountyManager(BountyHunterPlugin plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.activeBounties = new ConcurrentHashMap<>();
        this.dataFile = new File(BountyHunterPlugin.getInstance().getPluginContainer().dataFolder().toFile(), "bounties.json");
        
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdirs();
        }
    }
    
    public void loadData() {
        if (!dataFile.exists()) {
            plugin.getPluginLogger().info("[BountyHunter] No existing data file found. Starting fresh.");
            return;
        }
        
        try (FileReader reader = new FileReader(dataFile)) {
            List<BountyData> loadedBounties = gson.fromJson(reader, new TypeToken<List<BountyData>>(){}.getType());
            if (loadedBounties != null) {
                activeBounties.clear();
                for (BountyData bounty : loadedBounties) {
                    if (!bounty.isClaimed() && !bounty.isExpired()) {
                        activeBounties.put(bounty.getTargetId(), bounty);
                    }
                }
                plugin.getPluginLogger().info("[BountyHunter] Loaded " + activeBounties.size() + " active bounties.");
            }
        } catch (IOException e) {
            plugin.getPluginLogger().error("[BountyHunter] Failed to load bounty data: " + e.getMessage());
        }
    }
    
    public void saveData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            List<BountyData> bountyList = new ArrayList<>(activeBounties.values());
            gson.toJson(bountyList, writer);
            plugin.getPluginLogger().info("[BountyHunter] Saved " + bountyList.size() + " bounties.");
        } catch (IOException e) {
            plugin.getPluginLogger().error("[BountyHunter] Failed to save bounty data: " + e.getMessage());
        }
    }
    
    public boolean placeBounty(UUID targetId, UUID placerId, String targetName, String placerName, int amount) {
        if (amount < MIN_BOUNTY_AMOUNT) {
            return false;
        }
        
        if (activeBounties.containsKey(targetId)) {
            return false;
        }
        
        long placerBountyCount = activeBounties.values().stream()
            .filter(b -> b.getPlacerId().equals(placerId))
            .count();
        
        if (placerBountyCount >= MAX_ACTIVE_BOUNTIES_PER_PLAYER) {
            return false;
        }
        
        BountyData bounty = new BountyData(targetId, placerId, targetName, placerName, amount, expiryDurationMs);
        activeBounties.put(targetId, bounty);
        saveData();
        
        return true;
    }
    
    public boolean hasActiveBounty(UUID playerId) {
        BountyData bounty = activeBounties.get(playerId);
        return bounty != null && !bounty.isExpired() && !bounty.isClaimed();
    }
    
    public BountyData getBounty(UUID playerId) {
        return activeBounties.get(playerId);
    }
    
    public Collection<BountyData> getAllActiveBounties() {
        return activeBounties.values().stream()
            .filter(b -> !b.isExpired() && !b.isClaimed())
            .collect(Collectors.toList());
    }
    
    public boolean claimBounty(UUID targetId, UUID hunterId) {
        BountyData bounty = activeBounties.get(targetId);
        if (bounty == null || bounty.isClaimed() || bounty.isExpired()) {
            return false;
        }
        
        if (bounty.getPlacerId().equals(hunterId)) {
            return false;
        }
        
        bounty.setClaimed(true);
        bounty.setClaimedBy(hunterId);
        bounty.setClaimedTime(System.currentTimeMillis());
        
        activeBounties.remove(targetId);
        saveData();
        
        return true;
    }
    
    public boolean cancelBounty(UUID targetId, UUID requesterId) {
        BountyData bounty = activeBounties.get(targetId);
        if (bounty == null || bounty.isClaimed() || bounty.isExpired()) {
            return false;
        }
        
        if (!bounty.getPlacerId().equals(requesterId)) {
            return false;
        }
        
        activeBounties.remove(targetId);
        saveData();
        
        return true;
    }
    
    public int getBountyAmount(UUID playerId) {
        BountyData bounty = activeBounties.get(playerId);
        return bounty != null ? bounty.getRewardAmount() : 0;
    }
    
    public int cleanupExpiredBounties() {
        int removed = 0;
        Iterator<Map.Entry<UUID, BountyData>> iterator = activeBounties.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, BountyData> entry = iterator.next();
            if (entry.getValue().isExpired()) {
                iterator.remove();
                removed++;
            }
        }
        
        if (removed > 0) {
            saveData();
        }
        
        return removed;
    }
    
    public int getMinBountyAmount() {
        return MIN_BOUNTY_AMOUNT;
    }
    
    public int getMaxBountiesPerPlayer() {
        return MAX_ACTIVE_BOUNTIES_PER_PLAYER;
    }
    
    public long getPlayerBountyCount(UUID placerId) {
        return activeBounties.values().stream()
            .filter(b -> b.getPlacerId().equals(placerId))
            .count();
    }
}
