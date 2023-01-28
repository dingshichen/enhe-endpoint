// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.FEIGN_CLIENT
import com.enhe.endpoint.window.LibraryControlService
import com.enhe.endpoint.extend.findAttributeRealValue
import com.intellij.icons.AllIcons
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.ui.treeStructure.SimpleNode

/**
 * 模块节点
 */
class ModuleNode(
    private val parentNode: SimpleNode,
    private val module: Module,
    private val project: Project
) : BaseNode(parentNode) {

    private val controllerNodes = mutableListOf<ControllerNode>()

    init {
        myClosedIcon = AllIcons.Actions.ModuleDirectory
        updateNode(project)
    }

    override fun updateNode(project: Project) {
        cleanUpCache()
        controllerNodes.clear()
        val psiAnnotations = mutableListOf<PsiAnnotation>()
        LibraryControlService.getInstance(project).addPsiAnnotations(psiAnnotations, module, project)
        psiAnnotations.forEach {
            when (val serviceClass = it.parent.parent) {
                is PsiClass -> {
                    val feignService = serviceClass.supers.find { feign -> feign.hasAnnotation(FEIGN_CLIENT) }
                    feignService?.getAnnotation(FEIGN_CLIENT)?.findAttributeRealValue("value")?.let { value ->
                        ControllerNode(this, serviceClass, project, value).apply {
                            if (this@apply.children().isNotEmpty()) {
                                controllerNodes += this
                            }
                        }
                    }
                }
            }
        }
        controllerNodes.sortBy { it.name }
        update()
    }

    override fun buildChildren() = controllerNodes.toTypedArray()

    override fun getName() = module.name

}