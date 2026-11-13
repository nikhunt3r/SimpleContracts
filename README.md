# Simple Contracts

Separate NeoForge mod for Minecraft 1.21.1 / Java 21.

## Current implementation

- Separate mod ID: `simplecontracts`
- Separate package: `com.simplecontracts`
- Placeable contract board block
- Right-clicking the board opens a server-authoritative menu
- Crisp vanilla-style GUI shell with a working Close button
- Required runtime dependency on `simpleeconomy`
- Reward integration through `com.simpleeconomy.api.EconomyAPI`

## Connect Simple Economy

1. Build the `feature/contracts-reward-api` branch of Simple Economy.
2. Copy its built JAR to `libs/simpleeconomy-1.0.0.jar` for development compilation.
3. Keep Simple Economy installed alongside Simple Contracts in the game/server `mods` folder.
4. Run the Gradle build.

The local JAR is used by the development project. It is not shaded into the Simple Contracts JAR. The NeoForge metadata declares Simple Economy as a required runtime mod.

## Current test

1. Launch the client with both mods installed.
2. Find **Contract Board** in the Simple Contracts creative tab/search.
3. Place it and right-click with an empty hand.
4. Confirm the board window opens, the text is sharp, and the Close button works.
5. Walk away from the board and confirm the menu closes when the block is no longer valid.

## Next development steps

1. Add server-owned per-player contract data and persistence.
2. Implement a server-wide refresh timestamp with individual task generation per player.
3. Add authoritative progress tracking and one-time reward claiming.
4. Add task categories, levels, reputation and balancing.
