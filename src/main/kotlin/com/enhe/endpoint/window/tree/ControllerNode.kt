// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.REST_MAPPINGS
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
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
        endpointNodes.sortBy { it.name }
        update()
    }

    override fun buildChildren() = endpointNodes.toTypedArray()

    override fun getName(): String {
        val serviceName = when(val parentClass = service.parent) {
            is PsiClass -> "${parentClass.name}.${service.name}"
            else -> service.name
        }
        return "${subParentPath()} - $serviceName"
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