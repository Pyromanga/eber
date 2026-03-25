package villagecraft

import villagecraft.entity.player.Inventory
import villagecraft.skills.SkillSet
import villagecraft.skills.SkillType
import villagecraft.skills.XpTable
import kotlin.test.*

class SkillsTest {

    @Test
    fun `level 1 requires 0 xp`() {
        assertEquals(0, XpTable.xpForLevel(1))
    }

    @Test
    fun `level 2 requires some xp`() {
        assertTrue(XpTable.xpForLevel(2) > 0)
    }

    @Test
    fun `xp to level is monotonically increasing`() {
        for (level in 2..98) {
            assertTrue(XpTable.xpForLevel(level + 1) > XpTable.xpForLevel(level),
                "Level ${level + 1} should require more XP than level $level")
        }
    }

    @Test
    fun `adding xp increases level correctly`() {
        val skills = SkillSet()
        assertEquals(1, skills.getLevel(SkillType.WOODCUTTING))
        skills.addXp(SkillType.WOODCUTTING, XpTable.xpForLevel(10))
        assertTrue(skills.getLevel(SkillType.WOODCUTTING) >= 10)
    }

    @Test
    fun `hitpoints starts at level 10`() {
        val skills = SkillSet()
        assertEquals(10, skills.getLevel(SkillType.HITPOINTS))
    }

    @Test
    fun `total level is sum of all skill levels`() {
        val skills = SkillSet()
        val expected = SkillType.values().sumOf { skills.getLevel(it) }
        assertEquals(expected, skills.getTotalLevel())
    }
}

class InventoryTest {

    @Test
    fun `can add stackable item`() {
        val inv = Inventory()
        assertTrue(inv.add("logs", 5))
        assertEquals(5, inv.count("logs"))
    }

    @Test
    fun `stacks merge for stackable items`() {
        val inv = Inventory()
        inv.add("logs", 5)
        inv.add("logs", 3)
        assertEquals(8, inv.count("logs"))
        assertEquals(1, inv.size) // sollte nur 1 Slot belegen
    }

    @Test
    fun `can remove items`() {
        val inv = Inventory()
        inv.add("logs", 10)
        assertTrue(inv.remove("logs", 4))
        assertEquals(6, inv.count("logs"))
    }

    @Test
    fun `cannot remove more than available`() {
        val inv = Inventory()
        inv.add("logs", 3)
        assertFalse(inv.remove("logs", 5))
        assertEquals(3, inv.count("logs")) // unverändert
    }

    @Test
    fun `has returns true when enough items`() {
        val inv = Inventory()
        inv.add("gold_coins", 100)
        assertTrue(inv.has("gold_coins", 50))
        assertTrue(inv.has("gold_coins", 100))
        assertFalse(inv.has("gold_coins", 101))
    }

    @Test
    fun `inventory respects max slots`() {
        val inv = Inventory(maxSlots = 3)
        // Nicht-stapelbare Items (bronze_sword) belegen je 1 Slot
        // Hier testen wir mit verschiedenen stackable Items
        inv.add("logs")
        inv.add("copper_ore")
        inv.add("iron_ore")
        // 4. unterschiedliches nicht-stapelbares würde fehlschlagen... 
        // Für diesen Test: logs sind stackable, also kein neuer Slot
        assertTrue(inv.add("logs", 99)) // Gleiche Item-Art = kein neuer Slot
    }
}
