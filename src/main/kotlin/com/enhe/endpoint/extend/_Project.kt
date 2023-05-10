// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-08

package com.enhe.endpoint.extend

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

/**
 * 获取模块
 */
fun Project.getModules(): Array<Module> = this.getService(ModuleManager::class.java).modules