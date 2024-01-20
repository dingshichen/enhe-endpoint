// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2024-01-20

package com.enhe.endpoint.consts

enum class DagpModule(
    val abbreviation: String,
) {
    ASSESSMENT("asm"),
    ASSET("ast"),
    MASTER("mst"),
    METADATA("mdm"),
    MODEL2("mdl"),
    QUALITY("dqm"),
    REQUIREMENT("req"),
    STANDARD("std"),
    PROFILE("prf"),
    SYSTEM("sys"),
    ;

    fun tableNamePrefix(): String {
        return abbreviation + "_"
    }
}

fun ofDagpModule(value: String): DagpModule? {
    return DagpModule.entries.find { it.name.equals(value, true) }
}