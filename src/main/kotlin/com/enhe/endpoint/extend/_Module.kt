// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-09

package com.enhe.endpoint.extend

import com.intellij.openapi.module.Module

fun Module.getSimpleName() = this.name.replaceToEmpty("module:")

data class ModuleItem(
    val module: Module
) {
    override fun toString() = module.getSimpleName()
}