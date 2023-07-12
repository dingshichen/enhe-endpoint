// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.consts.FEIGN_CLIENT
import com.enhe.endpoint.consts.REST_MAPPINGS
import com.enhe.endpoint.util.PathUtil
import com.enhe.endpoint.window.EndpointModel
import com.enhe.endpoint.window.search.EndpointItemProvider
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.ui.SimpleTextAttributes
import javax.swing.Icon

/**
 * 控制器节点
 */
class ControllerNode(
    private val parentNode: BaseNode,
    private val service: PsiClass,
    private val project: Project,
    private val parentPath: String
) : BaseNode(parentNode) {

    private val endpointNodes = mutableListOf<EndpointNode>()

    private val endpointItemProvider = EndpointItemProvider.getInstance(project)

    init {
        myClosedIcon = getCusIcon()
        updateNode(project)
    }

    fun children() = endpointNodes

    override fun clearAll() {
        super.clearAll()
        endpointNodes.clear()
    }

    override fun updateNode(project: Project) {
        clearAll()

        service.methods.forEach {
            it.findSuperMethods().forEach { superMethod ->
                superMethod.annotations.forEach { an ->
                    if (an.qualifiedName in REST_MAPPINGS) {
                        val endpointNode = EndpointNode(this, project, an, it)
                        val fullPath = (getMajorText() + endpointNode.getMajorText()).let { path -> if (path.startsWith("/")) path else "/$path" }

                        endpointNodes += endpointNode
                        endpointItemProvider += EndpointModel(endpointNode.getCusIcon(), fullPath, endpointNode.getMethod())
                    }
                }
            }
        }

        // find default method in service interface
        val overrideMethodNames = endpointNodes.map { it.getMethod().name }.toHashSet();
        service.supers.filter { it.hasAnnotation(FEIGN_CLIENT) }.forEach {
            it.methods.filter { method -> !overrideMethodNames.contains(method.name) }.forEach { method ->
                method.annotations.forEach { an ->
                    if (an.qualifiedName in REST_MAPPINGS) {
                        val endpointNode = EndpointNode(this, project, an, method)
                        val fullPath = (getMajorText() + endpointNode.getMajorText()).let { path -> if (path.startsWith("/")) path else "/$path" }

                        endpointNodes += endpointNode
                        endpointItemProvider += EndpointModel(endpointNode.getCusIcon(), fullPath, endpointNode.getMethod())
                    }
                }
            }
        }

        endpointNodes.sortBy { it.name }
        update()
    }

    override fun getCusIcon(): Icon {
        return AllIcons.General.ImplementingMethod
    }

    override fun buildChildren() = endpointNodes.toTypedArray()

    override fun doUpdateV2(presentation: PresentationData) {
        val serviceName = when (val parentClass = service.parent) {
            is PsiClass -> "${parentClass.name}.${service.name}"
            else -> service.name
        }
        presentation.addText(getMajorText() + " ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.addText(serviceName, SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }

    override fun getMajorText() = PathUtil.subParentPath(parentPath)

}