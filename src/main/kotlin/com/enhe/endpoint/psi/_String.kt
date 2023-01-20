// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-20

package com.enhe.endpoint.psi


fun String.replaceToEmpty(oldValue: String, ignoreCase: Boolean = false) = replace(oldValue, "", ignoreCase)

fun String.replaceFirstToEmpty(oldValue: String, ignoreCase: Boolean = false) = replaceFirst(oldValue, "", ignoreCase)