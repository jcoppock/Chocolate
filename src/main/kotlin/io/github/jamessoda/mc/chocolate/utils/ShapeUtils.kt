package io.github.jamessoda.mc.chocolate.utils

import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin


object ShapeUtils {

    // Saves the vector offsets for each radius and degreeChance combination
    // first - radius
    // second - degreeChange
    private val sphereOffsets = mutableMapOf<Pair<Int, Int>, List<Vector>>()
    private val hollowSphereOffsets = mutableMapOf<Pair<Int, Int>, List<Vector>>()
    private val circleOffsets = mutableMapOf<Pair<Int, Int>, List<Vector>>()
    private val hollowCircleOffsets = mutableMapOf<Pair<Int, Int>, List<Vector>>()

    // 0i - radius
    // 1i - degreeChange
    // 2i - height (double)
    val spiralOffsets = mutableMapOf<Array<Number>, List<Vector>>()

    fun getSphereOffsets(radius: Int, degreeChange: Int): List<Vector> {
        val savedOffsets = sphereOffsets[Pair(radius, degreeChange)]

        if(savedOffsets != null) {
            return savedOffsets
        } else {
            val sphere = mutableListOf<Vector>()
            for (i in 0 until radius) {
                var azimuthalAngle = 0
                while (azimuthalAngle < 360) {
                    var polarAngle = 0
                    while (polarAngle < 180) {
                        val x = sin(Math.toRadians(polarAngle.toDouble())) * cos(Math.toRadians(azimuthalAngle.toDouble())) * radius
                        val y = sin(Math.toRadians(polarAngle.toDouble())) * sin(Math.toRadians(azimuthalAngle.toDouble())) * radius
                        val z = cos(Math.toRadians(polarAngle.toDouble())) * radius
                        sphere.add(Vector(x, y, z))
                        polarAngle += degreeChange
                    }
                    azimuthalAngle += degreeChange
                }
            }
            sphereOffsets[Pair(radius, degreeChange)] = sphere
            return sphere
        }
    }

    fun getHollowSphereOffsets(radius: Int, degreeChange: Int): List<Vector> {
        val sphere = mutableListOf<Vector>()
        var azimuthalAngle = 0
        while (azimuthalAngle < 360) {
            var polarAngle = 0
            while (polarAngle < 180) {
                val x = sin(Math.toRadians(polarAngle.toDouble())) * cos(Math.toRadians(azimuthalAngle.toDouble())) * radius
                val y = sin(Math.toRadians(polarAngle.toDouble())) * sin(Math.toRadians(azimuthalAngle.toDouble())) * radius
                val z = cos(Math.toRadians(polarAngle.toDouble())) * radius
                sphere.add(Vector(x, y, z))
                polarAngle += degreeChange
            }
            azimuthalAngle += degreeChange
        }
        return sphere
    }

    fun getHollowSphereOffsets(radius: Double, degreeChange: Int): List<Vector> {
        val sphere = mutableListOf<Vector>()
        var azimuthalAngle = 0
        while (azimuthalAngle < 360) {
            var polarAngle = 0
            while (polarAngle < 180) {
                val x = sin(Math.toRadians(polarAngle.toDouble())) * cos(Math.toRadians(azimuthalAngle.toDouble())) * radius
                val y = sin(Math.toRadians(polarAngle.toDouble())) * sin(Math.toRadians(azimuthalAngle.toDouble())) * radius
                val z = cos(Math.toRadians(polarAngle.toDouble())) * radius
                sphere.add(Vector(x, y, z))
                polarAngle += degreeChange
            }
            azimuthalAngle += degreeChange
        }
        return sphere
    }

    fun getCircleOffsets(radius: Int, degreeChange: Int): List<Vector> {
        val circle = mutableListOf<Vector>()
        for (i in 0 until radius) {
            var degree = 0
            while (degree < 360) {
                val radians = Math.toRadians(degree.toDouble())
                val x = cos(radians) * radius
                val z = sin(radians) * radius
                circle.add(Vector(x, 0.0, z))
                degree += degreeChange
            }
        }
        return circle
    }

    fun getSpiralOffsets(radius: Int, degreeChange: Int, heightChange: Double, height: Double) : List<Vector> {
        val spiralOffsets = spiralOffsets[arrayOf(radius, degreeChange, heightChange, height)]

        if (spiralOffsets != null) {
            return spiralOffsets
        } else {
            val spiral = mutableListOf<Vector>()

            var h = 0.0
            while (h < height) {
                var degree = 0
                while (degree < 360 && h < height) {
                    val radians = Math.toRadians(degree.toDouble())
                    val x = cos(radians) * radius
                    val z = sin(radians) * radius
                    spiral.add(Vector(x, h, z))
                    degree += degreeChange
                    h += heightChange
                }
            }
            ShapeUtils.spiralOffsets[arrayOf(radius, degreeChange, heightChange, height)] = spiral
            return spiral
        }
    }

}