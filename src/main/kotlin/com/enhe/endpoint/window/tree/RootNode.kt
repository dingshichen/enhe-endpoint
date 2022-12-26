// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.intellij.icons.AllIcons
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

/**
 * 根节点
 */
class RootNode : BaseNode() {

    private val moduleNodes = mutableListOf<ModuleNode>()

    init {
        myClosedIcon = AllIcons.Actions.Colors
    }

    override fun updateNode(project: Project) {
        cleanUpCache()
        moduleNodes.clear()
        project.getService(ModuleManager::class.java).modules.forEach {
            val moduleNode = ModuleNode(this, it, project)
            if (moduleNode.childCount > 0) {
                moduleNodes.add(moduleNode)
            }
        }
        moduleNodes.sortBy { it.name }
        update()
    }

    override fun buildChildren() = moduleNodes.toTypedArray()

    override fun getName() = "EnheV3"
}