// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.extend.getModules
import com.intellij.openapi.project.Project
import icons.MyIcons
import javax.swing.Icon

/**
 * 根节点
 */
class RootNode : BaseNode() {

    private val moduleNodes = mutableListOf<ModuleNode>()

    init {
        myClosedIcon = getCusIcon()
    }

    override fun clearAll() {
        super.clearAll()
        moduleNodes.clear()
    }

    override fun updateNode(project: Project) {
        clearAll()
        project.getModules().forEach {
            val moduleNode = ModuleNode(this, it, project)
            if (moduleNode.childCount > 0) {
                moduleNodes.add(moduleNode)
            }
        }
        moduleNodes.sortBy { it.name }
        update()
    }

    override fun getCusIcon(): Icon {
        return MyIcons.Logo
    }

    override fun buildChildren() = moduleNodes.toTypedArray()

    override fun getName() = "EnheV3"
}