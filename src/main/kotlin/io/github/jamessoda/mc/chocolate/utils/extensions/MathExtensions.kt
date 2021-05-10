package io.github.jamessoda.mc.chocolate.utils.extensions

fun Double.format(digits: Int) : String {
    return if(this != 0.0) {
        "%.${digits}f".format(this)
    } else {
        "%.0f".format(this)
    }
}