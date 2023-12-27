// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.SimpleNode
import com.intellij.util.net.HTTPMethod
import icons.MyIcons
import javax.swing.Icon

class EndpointNode(
    val parentNode: ControllerNode,
    val endpoint: EFEndpoint,
    private val project: Project,
) : BaseNode(parentNode) {

    init {
        myClosedIcon = getCusIcon()
        updateNode(project)
    }

    override fun updateNode(project: Project) {
        clearAll()
        update()
    }

    override fun buildChildren() = emptyArray<SimpleNode>()

    override fun getName() = endpoint.path

    fun getMethod() = endpoint.psiMethod

    override fun getCusIcon(): Icon {
        return when (endpoint.httpMethod) {
            HTTPMethod.GET -> MyIcons.GetMapping
            HTTPMethod.POST -> MyIcons.PostMapping
            HTTPMethod.PUT -> MyIcons.PutMapping
            HTTPMethod.DELETE -> MyIcons.DeleteMapping
            else -> MyIcons.RequestMapping
        }
    }

    override fun getMajorText(): String {
        return name
    }

}