package io.github.jamessoda.mc.chocolate.utils

import kotlin.math.pow

object PlayerUtils {

    // Calculate amount of EXP needed to level up
    fun getExpToLevelUp(level: Int): Int {
        return if (level <= 15) {
            2 * level + 7
        } else if (level <= 30) {
            5 * level - 38
        } else {
            9 * level - 158
        }
    }

    // Calculate total experience up to a level
    fun getExpAtLevel(level: Int): Int {
        return if (level <= 16) {
            (Math.pow(level.toDouble(), 2.0) + 6 * level).toInt()
        } else if (level <= 31) {
            (2.5 * level.toDouble().pow(2.0) - 40.5 * level + 360.0).toInt()
        } else {
            (4.5 * level.toDouble().pow(2.0) - 162.5 * level + 2220.0).toInt()
        }
    }

}