# BountyHunter

A player bounty hunting system for AllayMC servers that allows players to place bounties on each other and claim rewards for eliminating targets.

## Features

- **Place Bounties**: Players can place bounties on other players with monetary or item rewards
- **Bounty Board**: View all active bounties with `/bounty list`
- **Claim Rewards**: Hunters earn rewards when they eliminate targets with active bounties
- **Expiry System**: Bounties automatically expire after a configurable time period
- **Persistent Storage**: All bounty data is saved to JSON files
- **Cross-Dimension Support**: Bounties work across all dimensions (Overworld, Nether, End)

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/bounty place <player> <amount>` | Place a bounty on a player | `bountyhunter.place` |
| `/bounty list` | List all active bounties | `bountyhunter.list` |
| `/bounty info <player>` | View bounty info on a specific player | `bountyhunter.info` |
| `/bounty cancel <player>` | Cancel a bounty you placed | `bountyhunter.cancel` |
| `/bounty help` | Show help message | `bountyhunter.help` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `bountyhunter.place` | Place bounties on players | Everyone |
| `bountyhunter.list` | View active bounty list | Everyone |
| `bountyhunter.info` | View bounty information | Everyone |
| `bountyhunter.cancel` | Cancel own bounties | Everyone |
| `bountyhunter.help` | View help | Everyone |
| `bountyhunter.admin` | Admin commands (clear all) | OP only |

## Configuration

The plugin automatically creates data files at:
```
plugins/BountyHunter/bounties.json
```

### Default Settings
- **Bounty Expiry**: 7 days (configurable)
- **Minimum Bounty Amount**: 100 (configurable)
- **Maximum Active Bounties Per Player**: 5 (configurable)

## Installation

1. Download the latest release from GitHub
2. Place the JAR file in your server's `plugins` folder
3. Start or restart the server
4. The plugin will create necessary data files automatically

## Building from Source

```bash
./gradlew shadowJar
```

The compiled JAR will be in `build/libs/BountyHunter-0.1.0-shaded.jar`

## Requirements

- AllayMC Server with API 0.24.0 or higher
- Java 21 or higher

## How It Works

1. A player places a bounty on another player using `/bounty place <target> <amount>`
2. The reward amount is held in escrow (reserved)
3. Any player can view active bounties with `/bounty list`
4. When a hunter kills the target, they automatically receive the reward
5. If the bounty expires, the original placer gets their reward back

## API Usage

Other plugins can integrate with BountyHunter:

```java
BountyHunterPlugin plugin = BountyHunterPlugin.getInstance();
BountyManager manager = plugin.getBountyManager();

// Check if player has active bounty
boolean hasBounty = manager.hasActiveBounty(targetUUID);

// Get bounty amount
int amount = manager.getBountyAmount(targetUUID);
```

## Future Plans

- [ ] EconomyAPI integration for monetary rewards
- [ ] Item-based bounties (specific items as rewards)
- [ ] Anonymous bounties option
- [ ] Bounty hunter leaderboard
- [ ] Bounty completion announcements
- [ ] Configurable bounty expiry times
- [ ] MySQL database support

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

- **atri-0110** - [GitHub](https://github.com/atri-0110)

## Support

For issues and feature requests, please use the [GitHub issue tracker](https://github.com/atri-0110/BountyHunter/issues).

## Credits

- Built for [AllayMC](https://github.com/AllayMC/Allay) Server Software
