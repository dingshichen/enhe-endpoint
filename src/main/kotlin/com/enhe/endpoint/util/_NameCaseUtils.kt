// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-19

package com.enhe.endpoint.util

import com.intellij.webSymbols.utils.NameCaseUtils

fun NameCaseUtils.toUpperCamelCase(str: String): String {
    if (str.isNotEmpty()) {
        return toCamelCase(str).run { this[0].uppercase() + this.substring(1) }
    }
    return str
}