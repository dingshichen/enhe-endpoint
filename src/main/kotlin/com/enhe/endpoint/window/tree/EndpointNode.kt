// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.DELETE_MAPPING
import com.enhe.endpoint.GET_MAPPING
import com.enhe.endpoint.POST_MAPPING
import com.enhe.endpoint.PUT_MAPPING
import com.enhe.endpoint.psi.findAttributeRealValue
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod
import com.intellij.ui.treeStructure.SimpleNode
import javax.swing.Icon

class EndpointNode(
    private val parentNode: SimpleNode,
    private val project: Project,
    private val parentPath: String,
    private val restAnnotation: PsiAnnotation,
    val method: PsiMethod
) : BaseNode(parentNode) {

    init {
        myClosedIcon = getMethodIcon()
        updateNode(project)
    }

    override fun updateNode(project: Project) {
        cleanUpCache()
        update()
    }

    override fun buildChildren() = emptyArray<SimpleNode>()

    override fun getName(): String {
        var childPath = restAnnotation.findAttributeRealValue("value")
        if (childPath.isNullOrBlank()) {
            val st = restAnnotation.text.indexOf("{\"") + 2
            val ed = restAnnotation.text.indexOf("\"}")
            childPath = restAnnotation.text.substring(st, ed)
        }
        return childPath
    }

    /**
     * 根据请求类型选择不同的图标
     */
    private fun getMethodIcon(): Icon {
        return when (restAnnotation.qualifiedName) {
            GET_MAPPING -> IconLoader.getIcon("/icons/GET.svg", this.javaClass)
            POST_MAPPING -> IconLoader.getIcon("/icons/POST.svg", this.javaClass)
            PUT_MAPPING -> IconLoader.getIcon("/icons/PUT.svg", this.javaClass)
            DELETE_MAPPING -> IconLoader.getIcon("/icons/DELETE.svg", this.javaClass)
            else -> IconLoader.getIcon("/icons/REQUEST.svg", this.javaClass)
        }
    }

}