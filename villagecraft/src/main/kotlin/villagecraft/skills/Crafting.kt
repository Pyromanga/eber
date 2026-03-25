package villagecraft.skills

import villagecraft.entity.player.Inventory
import villagecraft.entity.player.ItemRegistry

/**
 * Crafting-System: Rezepte + Schmelzen + Kochen.
 *
 * Inspiration: OSRS Smithing/Crafting/Cooking,
 * kombiniert mit AC-Bastelworkshop-Feeling.
 */

data class CraftingIngredient(val itemId: String, val amount: Int)

data class CraftingRecipe(
    val id: String,
    val name: String,
    val ingredients: List<CraftingIngredient>,
    val output: String,
    val outputAmount: Int = 1,
    val requiredSkill: SkillType,
    val requiredLevel: Int,
    val xpGained: Int,
    val station: CraftingStation = CraftingStation.INVENTORY,
    val ticksDuration: Int = 1,
)

enum class CraftingStation(val displayName: String, val emoji: String) {
    INVENTORY  ("Inventar",   "🎒"),
    FURNACE    ("Schmelzofen","🔥"),
    ANVIL      ("Amboss",     "⚒️"),
    COOKING_POT("Kochtopf",  "🍳"),
    WORKBENCH  ("Werkbank",   "🪚"),
}

object CraftingRegistry {

    val recipes: List<CraftingRecipe> = listOf(
        // ── Schmelzen ──────────────────────────────────────────────────────────
        CraftingRecipe(
            id = "smelt_copper",
            name = "Kupferbarren schmelzen",
            ingredients = listOf(CraftingIngredient("copper_ore", 2)),
            output = "copper_bar", xpGained = 30,
            requiredSkill = SkillType.SMITHING, requiredLevel = 1,
            station = CraftingStation.FURNACE,
        ),
        CraftingRecipe(
            id = "smelt_iron",
            name = "Eisenbarren schmelzen",
            ingredients = listOf(CraftingIngredient("iron_ore", 3)),
            output = "iron_bar", xpGained = 70,
            requiredSkill = SkillType.SMITHING, requiredLevel = 15,
            station = CraftingStation.FURNACE,
        ),

        // ── Schmieden ──────────────────────────────────────────────────────────
        CraftingRecipe(
            id = "smith_bronze_sword",
            name = "Bronzeschwert schmieden",
            ingredients = listOf(CraftingIngredient("copper_bar", 2)),
            output = "bronze_sword", xpGained = 50,
            requiredSkill = SkillType.SMITHING, requiredLevel = 5,
            station = CraftingStation.ANVIL, ticksDuration = 3,
        ),
        CraftingRecipe(
            id = "smith_iron_sword",
            name = "Eisenschwert schmieden",
            ingredients = listOf(CraftingIngredient("iron_bar", 2)),
            output = "iron_sword", xpGained = 120,
            requiredSkill = SkillType.SMITHING, requiredLevel = 20,
            station = CraftingStation.ANVIL, ticksDuration = 4,
        ),

        // ── Kochen ─────────────────────────────────────────────────────────────
        CraftingRecipe(
            id = "cook_fish",
            name = "Fisch kochen",
            ingredients = listOf(CraftingIngredient("fish_raw", 1)),
            output = "fish_cooked", xpGained = 30,
            requiredSkill = SkillType.COOKING, requiredLevel = 1,
            station = CraftingStation.COOKING_POT,
        ),

        // ── Handwerk (Inventar) ────────────────────────────────────────────────
        CraftingRecipe(
            id = "craft_wooden_shield",
            name = "Holzschild bauen",
            ingredients = listOf(CraftingIngredient("logs", 5)),
            output = "wooden_shield", xpGained = 40,
            requiredSkill = SkillType.CRAFTING, requiredLevel = 5,
            station = CraftingStation.WORKBENCH,
        ),
    )

    fun byId(id: String) = recipes.find { it.id == id }

    fun availableAt(station: CraftingStation) = recipes.filter { it.station == station }
}

/**
 * Prüft und führt ein Rezept aus.
 */
object CraftingEngine {

    data class CraftResult(
        val success: Boolean,
        val message: String,
        val xpGained: Int = 0,
    )

    fun canCraft(recipe: CraftingRecipe, inventory: Inventory, skillSet: SkillSet): Boolean {
        if (!skillSet.meetsRequirement(recipe.requiredSkill, recipe.requiredLevel)) return false
        return recipe.ingredients.all { ing -> inventory.has(ing.itemId, ing.amount) }
    }

    fun craft(recipe: CraftingRecipe, inventory: Inventory, skillSet: SkillSet): CraftResult {
        if (!skillSet.meetsRequirement(recipe.requiredSkill, recipe.requiredLevel)) {
            return CraftResult(false, "Du brauchst Level ${recipe.requiredLevel} ${recipe.requiredSkill.displayName}.")
        }
        recipe.ingredients.forEach { ing ->
            if (!inventory.has(ing.itemId, ing.amount)) {
                val item = ItemRegistry.get(ing.itemId)
                return CraftResult(false, "Du brauchst ${ing.amount}× ${item.name}.")
            }
        }
        if (inventory.isFull) {
            return CraftResult(false, "Dein Inventar ist voll!")
        }

        // Zutaten entfernen
        recipe.ingredients.forEach { ing -> inventory.remove(ing.itemId, ing.amount) }

        // Output hinzufügen
        inventory.add(recipe.output, recipe.outputAmount)

        // XP
        skillSet.addXp(recipe.requiredSkill, recipe.xpGained)

        val outputItem = ItemRegistry.get(recipe.output)
        return CraftResult(true, "Du hast ${recipe.outputAmount}× ${outputItem.name} ${outputItem.emoji} hergestellt!", recipe.xpGained)
    }
}
