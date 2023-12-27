// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.window.EndpointModel
import com.enhe.endpoint.window.search.EndpointItemProvider
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.ui.SimpleTextAttributes
import javax.swing.Icon

/**
 * 控制器节点
 */
class ControllerNode(
    val parentNode: ModuleNode,
    val controller: EFController,
    private val project: Project,
) : BaseNode(parentNode) {

    private var endpointNodes: List<EndpointNode>? = null

    private val endpointItemProvider = EndpointItemProvider.getInstance(project)

    init {
        myClosedIcon = getCusIcon()
        updateNode(project)
    }

    override fun clearAll() {
        super.clearAll()
        endpointNodes = null
    }

    override fun updateNode(project: Project) {
        clearAll()
        endpointNodes = controller.endpoints.map { EndpointNode(this, it, project) }
        endpointNodes?.forEach {
            endpointItemProvider += EndpointModel(it.getCusIcon(), it.endpoint.fullPath, it.getMethod())
        }
        update()
    }

    override fun getCusIcon(): Icon {
        return AllIcons.General.ImplementingMethod
    }

    override fun buildChildren() = endpointNodes?.toTypedArray() ?: emptyArray()

    override fun doUpdateV2(presentation: PresentationData) {
        presentation.addText(getMajorText() + " ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.addText(controller.simpleName, SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }

    override fun getMajorText() = controller.path

}