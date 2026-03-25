package villagecraft.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import villagecraft.entity.player.Player
import villagecraft.quest.QuestManager
import villagecraft.quest.QuestRegistry
import villagecraft.quest.QuestStatus
import villagecraft.skills.SkillSet
import villagecraft.skills.SkillType
import villagecraft.world.GameWorld
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Speichersystem – JSON-basiert via kotlinx.serialization.
 *
 * Speicherformat enthält:
 * - Spieler-Name, Position, HP
 * - Inventar
 * - Skills (XP-Werte)
 * - Equipment
 * - Quest-Status
 * - Weltzeit
 */

private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

// ── Data Transfer Objects ─────────────────────────────────────────────────────

@Serializable
data class SaveFile(
    val version: Int = SAVE_VERSION,
    val savedAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val player: PlayerSave,
    val skills: SkillsSave,
    val inventory: InventorySave,
    val quests: QuestsSave,
    val worldTime: Float,
)

@Serializable
data class PlayerSave(
    val name: String,
    val tileX: Int,
    val tileY: Int,
    val currentHp: Int,
    val currentMana: Int,
)

@Serializable
data class SkillsSave(
    val xpMap: Map<String, Int>  // SkillType.name → XP
)

@Serializable
data class InventorySave(
    val slots: List<ItemStackSave>
)

@Serializable
data class ItemStackSave(
    val itemId: String,
    val amount: Int,
)

@Serializable
data class QuestsSave(
    val statuses: Map<String, String>,  // questId → QuestStatus.name
    val flags: Map<String, Map<String, Boolean>>,
    val counters: Map<String, Map<String, Int>>,
)

// ── Serializer / Deserializer ─────────────────────────────────────────────────

object SaveSystem {

    const val SAVE_VERSION = 1
    val saveDir: File get() = File(System.getProperty("user.home"), ".villagecraft/saves").also { it.mkdirs() }

    fun save(world: GameWorld, slotName: String = "autosave"): Result<File> = runCatching {
        val saveFile = buildSaveFile(world)
        val file = File(saveDir, "$slotName.json")
        file.writeText(json.encodeToString(saveFile))
        file
    }

    fun load(slotName: String = "autosave"): Result<SaveFile> = runCatching {
        val file = File(saveDir, "$slotName.json")
        require(file.exists()) { "Speicherstand '$slotName' nicht gefunden." }
        json.decodeFromString<SaveFile>(file.readText())
    }

    fun applyToWorld(saveFile: SaveFile, world: GameWorld) {
        // Spieler
        with(world.player) {
            name = saveFile.player.name
            tileX = saveFile.player.tileX
            tileY = saveFile.player.tileY
            currentHp = saveFile.player.currentHp
        }

        // Skills
        saveFile.skills.xpMap.forEach { (skillName, xp) ->
            runCatching {
                val skill = SkillType.valueOf(skillName)
                // Direkt XP setzen ohne Events feuern (beim Laden)
                repeat(xp) {} // Placeholder – echte Implementierung setzt xpMap direkt
            }
        }

        // Inventar (vorher leeren)
        // world.player.inventory.clear() – TODO wenn clear() implementiert
        saveFile.inventory.slots.forEach { stack ->
            world.player.inventory.add(stack.itemId, stack.amount)
        }

        // Weltzeit
        // world.worldClock.setTime(saveFile.worldTime) – TODO

        // Quests
        // world.questManager.restore(saveFile.quests) – TODO
    }

    fun listSaves(): List<SaveInfo> {
        return saveDir.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { file ->
                runCatching {
                    val save = json.decodeFromString<SaveFile>(file.readText())
                    SaveInfo(
                        slotName = file.nameWithoutExtension,
                        savedAt = save.savedAt,
                        playerName = save.player.name,
                        version = save.version,
                    )
                }.getOrNull()
            } ?: emptyList()
    }

    private fun buildSaveFile(world: GameWorld): SaveFile {
        val player = world.player

        val skillsSave = SkillsSave(
            xpMap = SkillType.values().associate { skill ->
                skill.name to player.skills.getXp(skill)
            }
        )

        val inventorySave = InventorySave(
            slots = player.inventory.getSlots().map { stack ->
                ItemStackSave(stack.item.id, stack.amount)
            }
        )

        val questsSave = QuestsSave(
            statuses = QuestRegistry.ALL_QUESTS.associate { q ->
                q.id to world.questManager.getStatus(q.id).name
            },
            flags = emptyMap(),   // TODO: Flags exportieren
            counters = emptyMap(),
        )

        return SaveFile(
            player = PlayerSave(
                name = player.name,
                tileX = player.tileX,
                tileY = player.tileY,
                currentHp = player.currentHp,
                currentMana = player.currentMana,
            ),
            skills = skillsSave,
            inventory = inventorySave,
            quests = questsSave,
            worldTime = world.worldClock.totalMinutes,
        )
    }
}

data class SaveInfo(
    val slotName: String,
    val savedAt: String,
    val playerName: String,
    val version: Int,
)
