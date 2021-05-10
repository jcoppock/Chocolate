package io.github.jamessoda.mc.chocolate.utils.extensions

import org.apache.commons.lang.WordUtils

fun String.makePretty() : String {
    var str = this

    str = str.replace("_", " ")
    str = WordUtils.capitalizeFully(str)

    return str
}