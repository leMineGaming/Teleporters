# Teleporters Plugin
![test](https://badgen.net/badge/status/stable/green?icon=github)
![test2](https://badgen.net/badge/latest/v1.3/blue?icon=version)

![avast](https://i.ibb.co/pr2hn5z/Avast-Safe2.png)

| Architecture  | Support |
| ------------- | ------------- |
| PaperMC  | ✅  |
| Spigot  | ✅  |
| Sponge  | ❌  |
| Forge  | ❌  |
| NeoForge  | ❌  |
| BungeeCord  | ❌  |
| Bukkit Legacy  | ❌  |

One jar supports every version listed below.

| Version          | Support | Java |
|------------------|---------|------|
| 26.3             | ✅       | 25   |
| 26.2             | ✅       | 25   |
| 26.1             | ✅       | 25   |
| 1.21.11          | ✅       | 21   |
| 1.21.10          | ✅       | 21   |
| 1.21.9           | ✅       | 21   |
| 1.21.8           | ✅       | 21   |
| 1.21.7           | ✅       | 21   |
| 1.21.6           | ✅       | 21   |
| 1.21.5           | ✅       | 21   |
| 1.21.4           | ✅       | 21   |
| 1.21.3           | ✅       | 21   |
| 1.21.2           | ✅       | 21   |
| 1.21.1           | ✅       | 21   |
| 1.20.X and older | ❌       |      |

The plugin declares `api-version: 1.21`, which is a minimum: it is built
against the newest Paper API (26.3) but only uses API that has existed since
1.21.1, so the same jar loads on all supported versions.

# Installation
1. download from releases or build yourself using maven
2. add to plugins directory
3. Run your Server

# Teleporter Anleitung

Hinweis: Maximal ?? Teleporter pro Person möglich.

### 1. Positionen setzen

Beide Positionen müssen auf einem Emerald Block sein.
Commands zum Positionen setzen:

``/teleport1``
``/teleport2``

### 2. Den Teleporter erstellen

*Öffentlicher Teleporter (Jeder kann ihn benutzen)*
``/setupteleporter``

*Privater Teleporter (Nur du kannst ihn benutzen)*
``/setupteleporter true``

### 3. Den Teleporter verwenden

Einfach auf den Teleporter laufen.
(Hinweis: Du kannst dich nur alle 5 Sekunden teleportieren)

### 4. Den Teleporter löschen

Einen der beiden Emeraldblöcke abbauen
Nachricht im chat sollte ``Teleporter removed.``
