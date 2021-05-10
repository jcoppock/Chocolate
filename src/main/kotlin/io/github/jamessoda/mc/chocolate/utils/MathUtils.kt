package io.github.jamessoda.mc.chocolate.utils

import kotlin.random.Random


object MathUtils {


    fun probabilityCheck(probability: Double) : Boolean {
        return Random.nextDouble() <= probability
    }

    fun lerp(min: Double, max: Double, factor: Double) : Double {
        return (min * (1 - factor)) + (max * factor)
    }

    fun lerp(min: Int, max: Int, factor: Double) : Int {
        return ((min * (1 - factor)) + (max * factor))
                .coerceIn(min.toDouble(), max.toDouble()).toInt()
    }

}