// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.consts.DELETE_MAPPING
import com.enhe.endpoint.consts.GET_MAPPING
import com.enhe.endpoint.consts.POST_MAPPING
import com.enhe.endpoint.consts.PUT_MAPPING
import com.enhe.endpoint.util.PathUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod
import com.intellij.ui.treeStructure.SimpleNode
import icons.MyIcons
import javax.swing.Icon

class EndpointNode(
    private val parentNode: BaseNode,
    private val project: Project,
    private val restAnnotation: PsiAnnotation,
    private val method: PsiMethod
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

    override fun getName() = PathUtil.getChildPath(restAnnotation)

    fun getMethod() = method

    override fun getCusIcon(): Icon {
        return when (restAnnotation.qualifiedName) {
            GET_MAPPING -> MyIcons.GetMapping
            POST_MAPPING -> MyIcons.PostMapping
            PUT_MAPPING -> MyIcons.PutMapping
            DELETE_MAPPING -> MyIcons.DeleteMapping
            else -> MyIcons.RequestMapping
        }
    }

    override fun getMajorText(): String {
        return name
    }

}