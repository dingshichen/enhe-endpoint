// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-05-09

package com.enhe.endpoint.action

import com.enhe.endpoint.consts.FEIGN_CLIENT
import com.enhe.endpoint.consts.REST_MAPPINGS
import com.enhe.endpoint.consts.WINDOW_PANE
import com.enhe.endpoint.extend.findAttributeRealValue
import com.enhe.endpoint.extend.or
import com.enhe.endpoint.util.PathUtil
import com.enhe.endpoint.window.tree.ControllerNode
import com.enhe.endpoint.window.tree.EndpointNode
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.PsiMethod
import java.awt.datatransfer.StringSelection

/**
 * 复制接口路径到粘贴板
 */
class CopyEndpointPathAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        e.getData(WINDOW_PANE)?.getSelected()?.let {
            when (it) {
                is EndpointNode -> {
                    val parent = it.parent
                    if (parent is ControllerNode) {
                        copyAndPrintStatus(project, parent.getMajorText() + it.getMajorText())
                    }
                }
            }
        }
        e.getData(CommonDataKeys.PSI_ELEMENT)?.let {
            when (it) {
                is PsiMethod -> {
                    val fullPath: String? = it.containingClass?.let path@{ psiClass ->
                        val feignService = if (psiClass.isInterface) psiClass else psiClass.supers.find { feign -> feign.hasAnnotation(FEIGN_CLIENT) }
                        return@path feignService?.getAnnotation(FEIGN_CLIENT)?.findAttributeRealValue("value")?.let { parentPath ->
                            if (psiClass.isInterface) {
                                return@path getPath(it, parentPath)
                            } else{
                                it.findSuperMethods().forEach { superMethod ->
                                    return@path getPath(superMethod, parentPath)
                                }
                            }
                            return@path null
                        }
                    }
                    copyAndPrintStatus(project, fullPath.or(""))
                }
                else -> {
                    copyAndPrintStatus(project, "")
                }
            }
        }
    }

    private fun getPath(psiMethod: PsiMethod, parentPath: String): String? {
        psiMethod.annotations.forEach { an ->
            if (an.qualifiedName in REST_MAPPINGS) {
                return (PathUtil.subParentPath(parentPath) + PathUtil.getChildPath(an)).let { path -> if (path.startsWith("/")) path else "/$path" }
            }
        }
        return null
    }

    private fun copyAndPrintStatus(project: Project, data: String) {
        CopyPasteManager.getInstance().setContents(StringSelection(data))
        WindowManager.getInstance().getStatusBar(project)?.let { statusBar ->
            statusBar.info = "Endpoint path has been copied"
        }
    }
}