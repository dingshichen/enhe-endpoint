// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.consts.FEIGN_CLIENT
import com.enhe.endpoint.consts.REST_MAPPINGS
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.treeStructure.SimpleNode

/**
 * 控制器节点
 */
class ControllerNode(
    private val parentNode: SimpleNode,
    private val service: PsiClass,
    private val project: Project,
    private val parentPath: String
) : BaseNode(parentNode) {

    private val endpointNodes = mutableListOf<EndpointNode>()

    init {
        myClosedIcon = AllIcons.General.ImplementingMethod
        updateNode(project)
    }

    fun children() = endpointNodes

    override fun updateNode(project: Project) {
        cleanUpCache()
        endpointNodes.clear()

        service.methods.forEach {
            it.findSuperMethods().forEach { superMethod ->
                superMethod.annotations.forEach { an ->
                    if (an.qualifiedName in REST_MAPPINGS) {
                        endpointNodes += EndpointNode(this, project, an, it)
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
                        endpointNodes += EndpointNode(this, project, an, method);
                    }
                }
            }
        }

        endpointNodes.sortBy { it.name }
        update()
    }

    override fun buildChildren() = endpointNodes.toTypedArray()

    override fun doUpdate(presentation: PresentationData) {
        val serviceName = when (val parentClass = service.parent) {
            is PsiClass -> "${parentClass.name}.${service.name}"
            else -> service.name
        }
        presentation.addText(subParentPath() + " ", SimpleTextAttributes.REGULAR_ATTRIBUTES)
        presentation.addText(serviceName, SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }

    /**
     * 排除项目前缀
     */
    private fun subParentPath(): String {
        val index = parentPath.indexOf("/")
        if (index == -1) {
            return parentPath.replace("//", "/")
        }
        return parentPath.substring(index + 1).replace("//", "/")
    }

}