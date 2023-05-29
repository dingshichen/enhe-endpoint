// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-26

package com.enhe.endpoint.window.tree

import com.enhe.endpoint.consts.FEIGN_CLIENT
import com.enhe.endpoint.extend.findAttributeRealValue
import com.enhe.endpoint.window.LibraryControlService
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.ui.SimpleTextAttributes
import javax.swing.Icon

/**
 * 模块节点
 */
class ModuleNode(
    private val parentNode: BaseNode,
    private val module: Module,
    private val project: Project
) : BaseNode(parentNode) {

    private val controllerNodes = mutableListOf<ControllerNode>()

    init {
        myClosedIcon = getCusIcon()
        updateNode(project)
    }

    override fun clearAll() {
        super.clearAll()
        controllerNodes.clear()
    }

    override fun updateNode(project: Project) {
        clearAll()
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

    override fun getCusIcon(): Icon {
        return AllIcons.Actions.ModuleDirectory
    }

    override fun buildChildren() = controllerNodes.toTypedArray()

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