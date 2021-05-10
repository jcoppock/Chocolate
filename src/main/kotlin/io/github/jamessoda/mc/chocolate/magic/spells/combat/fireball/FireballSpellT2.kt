package io.github.jamessoda.mc.chocolate.magic.spells.combat.fireball

class FireballSpellT2  : FireballSpell(110, "T2", 50, mapOf(
        Pair("cooldown", Pair(4.0, 3.0)),
        Pair("item-damage-chance", Pair(1.0, 0.6)),
        Pair("damage", Pair(14.0, 18.0)),
        Pair("radius", Pair(4.0, 5.0))))