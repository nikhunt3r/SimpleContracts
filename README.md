# Simple Contracts 1.0.0

A contract-board mod for **Minecraft 1.21.1**, **NeoForge 21.1.265** and **Java 21**.

Simple Contracts adds individual tasks, progression and money rewards to Survival gameplay. It integrates with the separate Simple Economy mod.

## Features

- Placeable Contract Board block in the Functional Blocks creative tab.
- Vanilla-style contract screen with three contracts per page.
- Five reputation tiers:
  - Beginner: 0–50 reputation
  - Advanced: 51–120 reputation
  - Experienced: 121–250 reputation
  - Professional: 251–500 reputation
  - Legend: 501+ reputation
- Higher tiers unlock additional contract pages. Lower-tier pages remain available.
- 25 contract templates covering combat, mining, farming, fishing, woodcutting and exploration.
- Randomized objective counts and money rewards within the configured ranges.
- Individual player progress and reputation saved in world data.
- Up to three active contracts by default, shared across all tier pages.
- Accepted contracts survive daily board refreshes.
- Completed contracts must be claimed manually from the board.
- Server-authoritative progress and reward handling.
- Operator command: `/contracts refresh`.
- Server-side TOML configuration for board refresh, active contract limit, reputation thresholds and rewards.

## Required dependency: Simple Economy

Simple Contracts requires **Simple Economy 1.0.0** at runtime.

The current build uses the idempotent contract reward API from:

- Repository: [nikhunt3r/simple-economy-mod](https://github.com/nikhunt3r/simple-economy-mod)
- Compatible branch: `feature/idempotent-contract-rewards`

Use a Simple Economy JAR built from that branch (or a later compatible release). The four-argument reward API stores a stable token to prevent a contract payout from being added twice after a save interruption.

Simple Economy is a required runtime dependency and is **not bundled** inside the Simple Contracts JAR. Install both JAR files in the instance or server `mods` folder.

## Build on Windows

1. Clone or download the Simple Economy repository and check out `feature/idempotent-contract-rewards`.
2. Build Simple Economy with Java 21:

   ```bat
   gradlew.bat clean build
   ```

3. Copy `simpleeconomy-1.0.0.jar` from Simple Economy's `build\libs` folder into the Simple Contracts project's `libs` folder. Rename it to exactly `simpleeconomy-1.0.0.jar` if necessary.
4. Build Simple Contracts:

   ```bat
   gradlew.bat clean build
   ```

The finished mod is created at:

```text
build/libs/simplecontracts-1.0.0.jar
```

A GitHub Actions workflow also builds both projects and uploads the Simple Contracts JAR as a workflow artifact.

## Configuration

The server config is generated after the world starts:

```text
saves/<world>/serverconfig/simplecontracts-server.toml
```

See [CONFIG.md](CONFIG.md) for every setting and its default value. Stop the world or server before editing the file, then restart it.

## Commands

- `/contracts refresh` — immediately refreshes contract offers for online players. Requires permission level 2 or higher.

## Installation

1. Install NeoForge 21.1.265 for Minecraft 1.21.1.
2. Put both `simpleeconomy-1.0.0.jar` and `simplecontracts-1.0.0.jar` in the `mods` folder.
3. Start the game or server.
4. Find the Contract Board in Functional Blocks, place it and right-click it.

