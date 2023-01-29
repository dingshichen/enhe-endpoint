// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.*
import com.enhe.endpoint.extend.findAttributeRealValue
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod
import com.intellij.ui.treeStructure.SimpleNode
import javax.swing.Icon

class EndpointNode(
    private val parentNode: SimpleNode,
    private val project: Project,
    private val restAnnotation: PsiAnnotation,
    private val method: PsiMethod
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

    fun getMethod() = method

    /**
     * 根据请求类型选择不同的图标
     */
    private fun getMethodIcon(): Icon {
        return when (restAnnotation.Qualified) {
            GET_MAPPING -> PluginIcons.getMapping
            POST_MAPPING -> PluginIcons.postMapping
            PUT_MAPPING -> PluginIcons.putMapping
            DELETE_MAPPING -> PluginIcons.deleteMapping
            else -> PluginIcons.requestMapping
        }
    }

}