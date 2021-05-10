package io.github.jamessoda.mc.chocolate.magic.spells.combat.fireball

class FireballSpellT1  : FireballSpell(100, "T1", 50, mapOf(
        Pair("cooldown", Pair(4.0, 4.0)),
        Pair("item-damage-chance", Pair(1.0, 0.7)),
        Pair("damage", Pair(10.0, 14.0)),
        Pair("radius", Pair(3.0, 4.0))))