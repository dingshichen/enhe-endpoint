// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-20

package com.enhe.endpoint.extend

import com.google.common.base.CaseFormat


fun String.replaceToEmpty(oldValue: String, ignoreCase: Boolean = false) = replace(oldValue, "", ignoreCase)

fun String.replaceFirstToEmpty(oldValue: String, ignoreCase: Boolean = false) = replaceFirst(oldValue, "", ignoreCase)

fun String.firstCharLower(): String {
    if (this.isEmpty()) {
        return this
    } else if (this.length == 1) {
        return this.lowercase()
    } else {
        return this.substring(0, 1).lowercase() + this.substring(1)
    }
}

fun String.lowerCamel() = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this)

fun String?.or(or: String) = if (isNullOrEmpty()) or else this

/**
 * 截取
 */
fun String.simpleSubstring(startString: String, endString: String): String {
    val a = indexOf(startString)
    val b = lastIndexOf(endString)
    if (a == -1 || b == -1) {
        return ""
    }
    return substring(a + startString.length, b)
}

/**
 * 分割符转小驼峰
 */
fun String.splitToSmallHump(split: String): String {
    val builder = StringBuilder()
    for (s in this.split(split)) {
        if (builder.toString() == "") {
            builder.append(s)
        } else {
            builder.append(s[0].uppercase())
            builder.append(s.substring(1))
        }
    }
    return builder.toString()
}