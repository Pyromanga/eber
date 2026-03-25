package villagecraft.entity.npc

import villagecraft.world.GameWorld

// ── Dialogue-System ───────────────────────────────────────────────────────────

/**
 * Ein Dialogknoten im Gesprächsbaum.
 * [text] = was der NPC sagt.
 * [options] = Antwortmöglichkeiten des Spielers (leer = Gespräch endet).
 */
data class DialogueNode(
    val id: String,
    val text: String,
    val options: List<DialogueOption> = emptyList(),
    val onEnter: ((GameWorld) -> Unit)? = null,  // z.B. Quest starten
)

data class DialogueOption(
    val text: String,
    val nextNodeId: String?,       // null = Gespräch endet
    val condition: ((GameWorld) -> Boolean)? = null,  // nur sichtbar wenn true
    val onSelect: ((GameWorld) -> Unit)? = null,
)

data class DialogueTree(
    val startNodeId: String,
    val nodes: Map<String, DialogueNode>,
) {
    fun getNode(id: String): DialogueNode = nodes[id] ?: error("Dialogknoten '$id' nicht gefunden")
    fun startNode(): DialogueNode = getNode(startNodeId)
}

// ── NPC-Basis ─────────────────────────────────────────────────────────────────

/**
 * Basis-Klasse für alle NPCs.
 * Inspiriert von Animal Crossing: jeder NPC hat eine Persönlichkeit,
 * Catchphrase und kontextabhängige Dialoge.
 */
abstract class Npc(
    val id: String,
    val name: String,
    val emoji: String,
    val catchphrase: String,     // z.B. "...fluffig!" (AC-typisch)
    val personality: NpcPersonality,
) {
    var tileX: Int = 0
    var tileY: Int = 0

    var currentDialogueTree: DialogueTree? = null
    var isTalking: Boolean = false

    abstract fun getDialogue(world: GameWorld): DialogueTree

    open fun update(deltaSeconds: Float, world: GameWorld) {
        // Kann von Subklassen überschrieben werden (wandernde NPCs etc.)
    }

    /** Generiert eine zufällige Begrüßung basierend auf Persönlichkeit und Tageszeit */
    fun greet(world: GameWorld): String {
        val timeGreet = when (world.worldClock.hour) {
            in 6..11  -> "Guten Morgen"
            in 12..17 -> "Guten Tag"
            in 18..21 -> "Guten Abend"
            else      -> "Nacht-Eule, was?"
        }
        return when (personality) {
            NpcPersonality.CHEERFUL   -> "$timeGreet! $catchphrase"
            NpcPersonality.GRUMPY     -> "Hm. Du schon wieder."
            NpcPersonality.MYSTERIOUS -> "...Ich hab dich kommen sehen."
            NpcPersonality.SCHOLARLY  -> "$timeGreet. Interessant, dass du genau jetzt auftauchst."
            NpcPersonality.LAZY       -> "Oh... hey... zzz... $catchphrase"
            NpcPersonality.PEPPY      -> "OMG HI HI HI!! $catchphrase"
        }
    }
}

enum class NpcPersonality {
    CHEERFUL, GRUMPY, MYSTERIOUS, SCHOLARLY, LAZY, PEPPY
}

// ── Konkrete NPCs ─────────────────────────────────────────────────────────────

/** Händler Bart - kauft und verkauft Items, gibt erste Quest */
class MerchantBart : Npc(
    id = "merchant_bart",
    name = "Händler Bart",
    emoji = "🧔",
    catchphrase = "...ein Schnäppchen!",
    personality = NpcPersonality.CHEERFUL,
) {
    init {
        tileX = 23
        tileY = 21
    }

    override fun getDialogue(world: GameWorld): DialogueTree {
        val hasQuest = world.questManager.isActive("barts_first_quest")
        val questDone = world.questManager.isCompleted("barts_first_quest")

        return DialogueTree(
            startNodeId = "root",
            nodes = mapOf(
                "root" to DialogueNode(
                    id = "root",
                    text = "${greet(world)} Ich bin Händler Bart, ${catchphrase}! Was kann ich für dich tun?",
                    options = buildList {
                        add(DialogueOption("Was verkaufst du?", "shop"))
                        if (!hasQuest && !questDone)
                            add(DialogueOption("Hast du Arbeit für mich?", "quest_offer"))
                        if (hasQuest)
                            add(DialogueOption("Hier sind die Gegenstände die du wolltest.", "quest_turn_in",
                                condition = { w -> w.player.inventory.has("logs", 10) },
                                onSelect = { w ->
                                    w.player.inventory.remove("logs", 10)
                                    w.player.inventory.add("gold_coins", 50)
                                    w.questManager.complete("barts_first_quest")
                                }
                            ))
                        add(DialogueOption("Tschüss!", null))
                    }
                ),
                "shop" to DialogueNode(
                    id = "shop",
                    text = "Ich hab immer frische Ware! Schau dich um... ein Schnäppchen!",
                    options = listOf(DialogueOption("Zurück", "root"))
                ),
                "quest_offer" to DialogueNode(
                    id = "quest_offer",
                    text = "Tatsächlich! Mein Lager ist leer. Kannst du mir 10 Holzstämme bringen? Ich zahl 50 Münzen dafür.",
                    options = listOf(
                        DialogueOption("Klar, mach ich!", null,
                            onSelect = { w -> w.questManager.start("barts_first_quest") }),
                        DialogueOption("Vielleicht später.", "root")
                    )
                ),
                "quest_turn_in" to DialogueNode(
                    id = "quest_turn_in",
                    text = "Wunderbar! Genau das brauche ich. Hier sind deine 50 Münzen - ein Schnäppchen für uns beide! 🪙",
                ),
            )
        )
    }
}

/** Weise Eule Minerva - Questgeberin, erklärt die Welt */
class WiseOwlMinerva : Npc(
    id = "wise_owl_minerva",
    name = "Weise Minerva",
    emoji = "🦉",
    catchphrase = "...hoo hoo!",
    personality = NpcPersonality.SCHOLARLY,
) {
    init {
        tileX = 25
        tileY = 24
    }

    override fun getDialogue(world: GameWorld): DialogueTree = DialogueTree(
        startNodeId = "root",
        nodes = mapOf(
            "root" to DialogueNode(
                id = "root",
                text = "${greet(world)} Ich beobachte dieses Dorf seit 300 Jahren. Hoo hoo. Was möchtest du wissen?",
                options = listOf(
                    DialogueOption("Erzähl mir von diesem Ort.", "lore"),
                    DialogueOption("Welche Skills sollte ich zuerst leveln?", "skills_advice"),
                    DialogueOption("Was ist das Geheimnis des mysteriösen Samens?", "seed_mystery",
                        condition = { w -> w.player.inventory.has("strange_seed") }),
                    DialogueOption("Auf Wiedersehen.", null)
                )
            ),
            "lore" to DialogueNode(
                id = "lore",
                text = "Dieses Dorf - VillageCraft - entstand vor langer Zeit. Die Alten sagten, wer alle Skills meistert, " +
                       "erlangt Zugang zum Urwald im Norden. Dort soll ein Schatz liegen... hoo hoo.",
                options = listOf(DialogueOption("Interessant! Mehr.", "lore2"), DialogueOption("Zurück", "root"))
            ),
            "lore2" to DialogueNode(
                id = "lore2",
                text = "Achte auf die Jahreszeiten. Im Winter schläft die Natur - und die Monster werden hungriger. " +
                       "Im Frühling blüht alles auf und seltene Kräuter erscheinen. Beobachte die Welt, hoo!",
                options = listOf(DialogueOption("Zurück", "root"))
            ),
            "skills_advice" to DialogueNode(
                id = "skills_advice",
                text = "Hoo hoo, gute Frage! Fang mit Holzfällen und Angeln an - so hast du immer Vorräte. " +
                       "Dann Kampf, für die Wälder im Norden. Handwerk verbindet alles zusammen.",
                options = listOf(DialogueOption("Danke für den Rat.", "root"))
            ),
            "seed_mystery" to DialogueNode(
                id = "seed_mystery",
                text = "HOO! Du hast ihn! Dieser Samen... er ist uralt. Pflanz ihn bei Vollmond in guten Boden. " +
                       "Was wächst, wird dich überraschen. Hüte ihn gut... hoo hoo hoo.",
                options = listOf(DialogueOption("Klingt geheimnisvoll...", "root"))
            ),
        )
    )
}

/** Fauler Bauer Bruno - gibt Farm-Quests, schläft meistens */
class LazyFarmerBruno : Npc(
    id = "lazy_farmer_bruno",
    name = "Bauer Bruno",
    emoji = "👨‍🌾",
    catchphrase = "...zzz...",
    personality = NpcPersonality.LAZY,
) {
    init {
        tileX = 30
        tileY = 26
    }

    override fun getDialogue(world: GameWorld): DialogueTree {
        val isNight = world.worldClock.isNight
        return DialogueTree(
            startNodeId = "root",
            nodes = mapOf(
                "root" to DialogueNode(
                    id = "root",
                    text = if (isNight)
                        "Zzz... hä? Oh. Du. Mitten in der Nacht... zzz... kann nicht warten?"
                    else
                        "Oh hey... ich wollt grade... zzz... kurz ausruhen. ${catchphrase} Was ist?",
                    options = listOf(
                        DialogueOption("Brauchst du Hilfe auf dem Feld?", "farm_help"),
                        DialogueOption("Wie geht's dir?", "how_are_you"),
                        DialogueOption("Lass mich schlafen.", null)
                    )
                ),
                "farm_help" to DialogueNode(
                    id = "farm_help",
                    text = "Ach weißt du... da wären schon ein paar Äpfel zu ernten... " +
                           "und Unkraut zu jäten... und Bewässern... zzz... " +
                           "Ja, eigentlich alles. Ich kümmer mich drum. Morgen. Vielleicht.",
                    options = listOf(DialogueOption("Ich helfe dir!", null,
                        onSelect = { w -> w.questManager.start("brunos_farm_quest") }),
                        DialogueOption("Zurück", "root"))
                ),
                "how_are_you" to DialogueNode(
                    id = "how_are_you",
                    text = "Müde. Sehr müde. Immer müde. Aber der Kohl wächst gut! Das ist... das Wichtige. Zzz.",
                    options = listOf(DialogueOption("Stimmt irgendwie.", null))
                ),
            )
        )
    }
}

// ── NPC-Manager ───────────────────────────────────────────────────────────────

class NpcManager {

    private val npcs = mutableListOf<Npc>()

    fun spawnDefaultNpcs(tileMap: villagecraft.world.TileMap) {
        npcs.add(MerchantBart())
        npcs.add(WiseOwlMinerva())
        npcs.add(LazyFarmerBruno())
    }

    fun update(deltaSeconds: Float, world: GameWorld) {
        npcs.forEach { it.update(deltaSeconds, world) }
    }

    fun all(): List<Npc> = npcs.toList()

    fun getById(id: String): Npc? = npcs.find { it.id == id }

    fun getNpcAt(x: Int, y: Int): Npc? = npcs.find { it.tileX == x && it.tileY == y }
}
