# Simple Contracts

Separate NeoForge mod for Minecraft 1.21.1 / Java 21.

## Current scaffold

- Separate mod ID: `simplecontracts`
- Separate package: `com.simplecontracts`
- Contract board block and block item registered
- Required runtime dependency on `simpleeconomy`
- Reward integration through `com.simpleeconomy.api.EconomyAPI`

## Connect Simple Economy

1. Build the `feature/contracts-reward-api` branch of Simple Economy.
2. Copy its built JAR to `libs/simpleeconomy-1.0.0.jar`.
3. Keep Simple Economy installed alongside Simple Contracts in the game/server mods folder.
4. Run the Gradle build.

The local JAR is a compile-time dependency. The NeoForge metadata declares Simple Economy as a required runtime mod.

## Next development steps

1. Add the contract board menu using standard Minecraft widgets.
2. Add server-owned per-player contract data and persistence.
3. Implement a server-wide refresh timestamp with individual task generation per player.
4. Add authoritative progress tracking and one-time reward claiming.
5. Add task categories, levels, reputation and balancing.
