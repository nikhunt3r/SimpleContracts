# Simple Contracts server configuration

Simple Contracts registers a NeoForge **SERVER** configuration.

After the world is started once, the file is created here:

- Singleplayer: `<minecraft>/saves/<world>/serverconfig/simplecontracts-server.toml`
- Dedicated server: `<server folder>/<world>/serverconfig/simplecontracts-server.toml`

Stop the world/server before editing the file, then start it again.

## General

| Setting | Default | Meaning |
|---|---:|---|
| `maxActiveContracts` | `3` | Maximum number of accepted, unclaimed contracts across all pages per player. Allowed range: 1–15. |
| `refreshIntervalDays` | `1` | Number of in-game days between automatic shared board refreshes. Allowed range: 1–30. |
| `objectiveAmountMultiplier` | `1.0` | Multiplier for generated resource and mob targets. For example, 1.5 increases generated targets by 50%. Allowed range: 0.1–5.0. |

## Reputation

| Setting | Default | Meaning |
|---|---:|---|
| `level1Max` | `50` | Highest reputation total for Beginner. |
| `level2Max` | `120` | Highest reputation total for Advanced. |
| `level3Max` | `250` | Highest reputation total for Experienced. |
| `level4Max` | `500` | Highest reputation total for Professional. |
| `level1Reward` | `5` | Reputation granted when claiming a level 1 contract. |
| `level2Reward` | `10` | Reputation granted when claiming a level 2 contract. |
| `level3Reward` | `15` | Reputation granted when claiming a level 3 contract. |
| `level4Reward` | `20` | Reputation granted when claiming a level 4 contract. |
| `level5Reward` | `25` | Reputation granted when claiming a level 5 contract. |

The five levels are calculated from the four maximum thresholds. Thresholds are normalized to remain strictly increasing if the values in the file are entered out of order.

## Money rewards

The minimum and maximum money rewards can be configured independently for each contract level.

| Setting | Default |
|---|---:|
| `level1Minimum` / `level1Maximum` | `90` / `150` |
| `level2Minimum` / `level2Maximum` | `200` / `300` |
| `level3Minimum` / `level3Maximum` | `400` / `650` |
| `level4Minimum` / `level4Maximum` | `800` / `1200` |
| `level5Minimum` / `level5Maximum` | `2500` / `3000` |

When a minimum is larger than its maximum, the mod swaps the effective bounds so that contract generation still works.

## When changes take effect

The settings are read from the server configuration. Money rewards and objective amounts are rolled when a new contract offer is generated, so changing their values does not rewrite contracts already shown on a player's board. The active-contract limit, reputation rewards and refresh interval are read when the corresponding action occurs.
