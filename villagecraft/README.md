# 🏘️ VillageCraft

> *Animal Crossing trifft Old-School RuneScape – in Kotlin*

Ein 2D-RPG mit Dorfleben, Skill-Grinding, Quests und Kampfsystem.
Gebaut mit reinem Kotlin + Swing/AWT, kein Framework-Overhead.

---

## 🚀 Starten

```bash
# Voraussetzung: JDK 17+, Gradle

./gradlew run
# oder
./gradlew jar && java -jar build/libs/villagecraft-0.1.0.jar
```

---

## 🏗️ Architektur

```
villagecraft/
├── core/
│   ├── GameEngine.kt       # Fenster, Lifecycle, Koordination
│   ├── GameLoop.kt         # Fester 60-FPS-Loop (ScheduledExecutor)
│   └── EventBus.kt         # Typsicherer Event-Bus (entkoppelte Kommunikation)
│
├── world/
│   ├── GameWorld.kt        # Root-State: Karte + Spieler + NPCs + Zeit
│   └── TileMap.kt          # 2D-Karte mit TileType-Enum
│
├── entity/
│   ├── player/
│   │   ├── Player.kt       # Spieler: Position, HP, Movement-Queue, Equipment
│   │   └── Inventory.kt    # Item-Stacks, Inventar (28 Slots), ItemRegistry
│   └── npc/
│       └── Npc.kt          # NPC-Basisklasse, Dialogue-Trees, NpcManager
│
├── skills/
│   └── Skills.kt           # SkillType-Enum, XpTable (OSRS-Formel), SkillSet
│
├── combat/
│   └── Combat.kt           # CombatEngine, Monster-Registry, ActiveCombat-Loop
│
├── quest/
│   └── QuestManager.kt     # QuestDefinition, Objectives, Rewards, QuestManager
│
└── ui/
    └── screens/
        └── GameScreen.kt   # Swing-Panel: Tile-Rendering, HUD, Dialogue-UI
```

---

## 🎮 Gameplay-Systeme

### Skills (OSRS-Style)
- 15 Skills in 4 Kategorien: Gathering, Crafting, Combat, Social
- OSRS-kompatible XP-Formel (exponentiell, Level 1–99)
- Events bei XP-Gewinn und Level-Up

### NPCs & Dialoge (Animal Crossing-Style)
- Persönlichkeitssystem (Cheerful, Grumpy, Mysterious, Scholarly, Lazy, Peppy)
- Baumbasierte Dialogue-Trees mit Bedingungen und Callbacks
- Kontextabhängige Begrüßungen (Tageszeit, Quests, Inventar)

### Kampf (OSRS-Style Ticks)
- Tick-basiert (0.6s pro Tick)
- Accuracy-Roll: Angriff vs. Verteidigung
- Max-Hit-Formel angelehnt an OSRS
- Loot-Tabellen mit Gewichtungen

### Quests
- Voraussetzungsketten (Quest A muss vor Quest B abgeschlossen sein)
- Objective-System: Item-Checks, Flags, Counter
- Rewards: Gold, XP, Items

### Welt & Zeit
- 64×64 Tile-Karte mit prozedural generierter Grundstruktur
- Uhrzeit (3 Spielminuten = 1 Sekunde real)
- Jahreszeiten (28 Tage/Jahr, 7 Tage pro Saison)
- Tages/Nacht-Wechsel via EventBus

---

## 🗺️ Nächste Schritte

1. **Skill-Aktionen**: Holzfällen/Mining/Angeln auf Tiles klicken
2. **Kampfbildschirm**: Separate Combat-UI mit Log
3. **Speichern/Laden**: JSON-Serialisierung mit kotlinx.serialization
4. **Inventar-Screen**: Visuelles Inventar-Fenster
5. **Crafting-Rezepte**: Aus Materialien Items herstellen
6. **NPC-Wanderung**: NPCs bewegen sich auf Tages-Routen
7. **Wetter-System**: Regen, Schnee je nach Jahreszeit
8. **Sound**: Einfache Swing-Beep-Töne als Platzhalter

---

## 🧰 Tech-Stack

| Was | Wie |
|-----|-----|
| Sprache | Kotlin 1.9 auf JVM 17 |
| GUI | Swing/AWT (kein Framework) |
| Serialisierung | kotlinx.serialization |
| Async | kotlinx.coroutines |
| Build | Gradle KTS |

---

*Gebaut mit ❤️ und zu viel RuneScape-Nostalgie*
