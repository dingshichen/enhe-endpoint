// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.intellij.openapi.project.Project
import icons.MyIcons
import javax.swing.Icon

/**
 * 根节点
 */
class RootNode : BaseNode() {

    private var moduleNodes: List<ModuleNode>? = null

    init {
        myClosedIcon = getCusIcon()
    }

    override fun clearAll() {
        super.clearAll()
        moduleNodes = null
    }

    override fun updateNode(project: Project) {
        clearAll()
        moduleNodes = EndpointContext.efModules.map { ModuleNode(this, it, project) }
        update()
    }

    override fun getCusIcon(): Icon {
        return MyIcons.Logo
    }

    override fun buildChildren() = moduleNodes?.toTypedArray() ?: emptyArray()

    override fun getName() = "DAGP3"
}