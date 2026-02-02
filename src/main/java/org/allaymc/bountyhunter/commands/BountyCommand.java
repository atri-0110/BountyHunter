package org.allaymc.bountyhunter.commands;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.server.Server;
import org.allaymc.bountyhunter.data.BountyData;
import org.allaymc.bountyhunter.data.BountyManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BountyCommand extends Command {
    
    private final BountyManager bountyManager;
    
    public BountyCommand(BountyManager bountyManager) {
        super("bounty", "Bounty hunting system commands", "bountyhunter.command");
        this.bountyManager = bountyManager;
    }
    
    @Override
    public void prepareCommandTree(CommandTree tree) {
        tree.getRoot()
            .key("place")
            .str("target")
            .intNum("amount")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("This command can only be used by players");
                    return context.fail();
                }
                
                String targetName = context.getResult(1);
                int amount = context.getResult(2);
                
                EntityPlayer target = findPlayer(targetName);
                if (target == null) {
                    player.sendMessage("Player not found: " + targetName);
                    return context.fail();
                }
                
                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage("You cannot place a bounty on yourself!");
                    return context.fail();
                }
                
                if (amount < bountyManager.getMinBountyAmount()) {
                    player.sendMessage("Minimum bounty amount is " + bountyManager.getMinBountyAmount());
                    return context.fail();
                }
                
                boolean success = bountyManager.placeBounty(
                    target.getUniqueId(),
                    player.getUniqueId(),
                    target.getDisplayName(),
                    player.getDisplayName(),
                    amount
                );
                
                if (success) {
                    player.sendMessage("Bounty placed on " + targetName + " for " + amount + " coins!");
                    Server.getInstance().getMessageChannel().broadcastMessage("A bounty of " + amount + " coins has been placed on " + targetName + "!");
                } else {
                    player.sendMessage("Failed to place bounty. Target may already have a bounty, or you have reached your limit.");
                }
                
                return context.success();
            })
            .root()
            .key("list")
            .exec(context -> {
                var bounties = bountyManager.getAllActiveBounties();
                if (bounties.isEmpty()) {
                    context.getSender().sendMessage("No active bounties available.");
                    return context.success();
                }
                
                context.getSender().sendMessage("Active Bounties:");
                for (BountyData bounty : bounties) {
                    long hoursRemaining = TimeUnit.MILLISECONDS.toHours(bounty.getRemainingTime());
                    context.getSender().sendMessage("- " + bounty.getTargetName() + ": " + bounty.getRewardAmount() + " coins (" + hoursRemaining + "h remaining)");
                }
                
                return context.success();
            })
            .root()
            .key("info")
            .str("target")
            .exec(context -> {
                String targetName = context.getResult(1);
                EntityPlayer target = findPlayer(targetName);
                
                if (target == null) {
                    context.getSender().sendMessage("Player not found: " + targetName);
                    return context.fail();
                }
                
                BountyData bounty = bountyManager.getBounty(target.getUniqueId());
                if (bounty == null || bounty.isExpired()) {
                    context.getSender().sendMessage(targetName + " has no active bounty.");
                    return context.success();
                }
                
                long hoursRemaining = TimeUnit.MILLISECONDS.toHours(bounty.getRemainingTime());
                context.getSender().sendMessage("Bounty Info for " + targetName + ":");
                context.getSender().sendMessage("- Reward: " + bounty.getRewardAmount() + " coins");
                context.getSender().sendMessage("- Placed by: " + bounty.getPlacerName());
                context.getSender().sendMessage("- Time remaining: " + hoursRemaining + " hours");
                
                return context.success();
            })
            .root()
            .key("cancel")
            .str("target")
            .exec(context -> {
                if (!(context.getSender() instanceof EntityPlayer player)) {
                    context.getSender().sendMessage("This command can only be used by players");
                    return context.fail();
                }
                
                String targetName = context.getResult(1);
                EntityPlayer target = findPlayer(targetName);
                
                if (target == null) {
                    player.sendMessage("Player not found: " + targetName);
                    return context.fail();
                }
                
                boolean success = bountyManager.cancelBounty(target.getUniqueId(), player.getUniqueId());
                if (success) {
                    player.sendMessage("Bounty on " + targetName + " has been cancelled.");
                } else {
                    player.sendMessage("You don't have an active bounty on " + targetName);
                }
                
                return context.success();
            })
            .root()
            .key("help")
            .exec(context -> {
                context.getSender().sendMessage("BountyHunter Commands:");
                context.getSender().sendMessage("/bounty place <player> <amount> - Place a bounty");
                context.getSender().sendMessage("/bounty list - List all active bounties");
                context.getSender().sendMessage("/bounty info <player> - View bounty info");
                context.getSender().sendMessage("/bounty cancel <player> - Cancel your bounty");
                context.getSender().sendMessage("/bounty help - Show this help");
                return context.success();
            });
    }
    
    private EntityPlayer findPlayer(String name) {
        final EntityPlayer[] result = new EntityPlayer[1];
        Server.getInstance().getPlayerManager().forEachPlayer(player -> {
            EntityPlayer entity = player.getControlledEntity();
            if (entity != null && entity.getDisplayName().equalsIgnoreCase(name)) {
                result[0] = entity;
            }
        });
        return result[0];
    }
}
