# PreventPotions

Blocks potions on your server: drinking, splash and lingering potions, tipped arrows, and optionally effects from beacons and `/effect`. Each rule can be switched on or off on its own, and the whole thing can be paused with one command.

## Requirements

| Platform | Versions | Jar |
|---|---|---|
| Paper (and forks) | 1.21.1 – 26.3 | `PreventPotions-paper-<version>.jar` |
| Folia | 1.21.4 – 26.3 | same jar as Paper |
| Fabric (server or singleplayer) | 1.21.11 | `PreventPotions-fabric-1.21.11-<version>.jar` + Fabric API |
| Fabric | 26.3 | `PreventPotions-fabric-26.3-<version>.jar` + Fabric API |

Paper 26.x and every Fabric 26.x server need Java 25; 1.21.x needs Java 21. On Fabric, permissions go through
[fabric-permissions-api](https://github.com/lucko/fabric-permissions-api) (bundled), so LuckPerms works as usual;
without a permissions mod, the admin command falls back to permission level 2.

## Installation

1. Download the jar for your platform (or [build it yourself](#building)).
2. Paper/Folia: drop it into `plugins/`. Fabric: drop it and Fabric API into `mods/`.
3. Start the server. The config is generated automatically (`plugins/PreventPotions/config.yml` or `config/preventpotions.json`).
4. Edit the config to taste and run `/preventpotions reload`.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/preventpotions reload` | Reloads the config. | `preventpotions.admin` |
| `/preventpotions toggle` | Pauses or resumes all blocking. Resets to on after a restart. | `preventpotions.admin` |

## Permissions

| Permission | Description | Default |
|---|---|---|
| `preventpotions.admin` | `/preventpotions reload` and `toggle`. | Operators |

## Configuration

`plugins/PreventPotions/config.yml`

| Option | Default | Blocks |
|---|---|---|
| `prevent.drink` | `true` | Drinking potions. |
| `prevent.splash` | `true` | Throwing splash potions. |
| `prevent.lingering` | `true` | Throwing lingering potions. |
| `prevent.tipped_arrows` | `true` | Firing, crafting, trading for and picking up tipped arrows (details below). |
| `prevent.beacon` | `false` | Beacon effects on players. |
| `prevent.commands` | `false` | Effects given to players with `/effect`. |
| `messages.<rule>` | | Message sent when that rule blocks something. `&` color codes. |

On Fabric the same keys live in `config/preventpotions.json`, as a `prevent` map and a `messages` map.

## What gets blocked

A refused drink, throw or shot doesn't use the item up. Tipped arrows are the exception in one way: with
`tipped_arrows` on, the plugin actively removes them from inventories (see the table).

| Rule | Paper / Folia | Fabric |
|---|---|---|
| Drink | The potion can't be drunk. | Same. |
| Splash / lingering | The potion isn't thrown; dispensers can't fire them either. | The potion isn't thrown by players. |
| Tipped arrows | Bows and crossbows skip tipped arrows when choosing ammo. Crafting results are cleared, villagers can't offer them, they can't be picked up, clicking one deletes it, and they're removed from inventories on join. | Can't be fired or picked up. Any that end up in an inventory (crafting, trading, commands) are removed on join and within a second otherwise. |
| Beacon | Beacon effects aren't applied to players. | Same. |
| Commands | `/effect` doesn't apply to players. | Same; the command reports the player as a failed target. |

## Building

Requires JDK 21 and JDK 25 (Gradle toolchains pick them up, or downloads them via foojay).

```bash
cd PreventPotions
./gradlew buildAll
```

All jars end up in `build/dist/`. `./gradlew :paper:check` also re-compiles the Paper sources against the newest
Paper API to catch removed API early, and `./gradlew :paper:runServer` / `:paper:runFolia` start a dev server.

Made by **Eyman**.
