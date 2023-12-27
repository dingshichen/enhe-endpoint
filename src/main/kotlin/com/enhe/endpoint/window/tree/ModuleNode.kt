// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import javax.swing.Icon

/**
 * 模块节点
 */
class ModuleNode(
    val parentNode: RootNode,
    val module: EFModule,
    private val project: Project
) : BaseNode(parentNode) {

    private var controllerNodes: List<ControllerNode>? = null

    init {
        myClosedIcon = getCusIcon()
        updateNode(project)
    }

    override fun clearAll() {
        super.clearAll()
        controllerNodes = null
    }

    override fun updateNode(project: Project) {
        clearAll()
        controllerNodes = module.controllers.map { ControllerNode(this, it, project) }
        update()
    }

    override fun getCusIcon(): Icon {
        return AllIcons.Actions.ModuleDirectory
    }

    override fun buildChildren() = controllerNodes?.toTypedArray() ?: emptyArray()

    override fun doUpdateV2(presentation: PresentationData) {
        val moduleName = module.name
        if (moduleName.contains(".")) {
            presentation.addText(moduleName.substring(0, moduleName.indexOf(".")), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
            presentation.addText(moduleName.substring(moduleName.indexOf(".")), SimpleTextAttributes.REGULAR_ATTRIBUTES)
        } else {
            presentation.addText(moduleName, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        }
    }

}