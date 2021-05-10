package io.github.jamessoda.mc.chocolate.magic.spells.combat.fireball

class FireballSpellT3  : FireballSpell(120, "T3", 50, mapOf(
        Pair("cooldown", Pair(4.0, 2.0)),
        Pair("item-damage-chance", Pair(1.0, 0.5)),
        Pair("damage", Pair(20.0, 22.0)),
        Pair("radius", Pair(5.0, 6.0))))