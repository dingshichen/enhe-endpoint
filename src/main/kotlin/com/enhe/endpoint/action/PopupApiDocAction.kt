// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-27

package com.enhe.endpoint.action

import com.enhe.endpoint.consts.WINDOW_PANE
import com.enhe.endpoint.doc.DocService
import com.enhe.endpoint.ui.ApiDocPreviewForm
import com.enhe.endpoint.window.tree.EndpointNode
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiMethod

/**
 * 弹出 API 接口文档
 */
class PopupApiDocAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        e.getData(CommonDataKeys.PSI_ELEMENT)?.let {
            when (it) {
                is PsiMethod -> {
                    // 解析、生成文档
                    it.containingClass?.let { psiClass ->
                        val api = DocService.instance(project).buildApi(project, psiClass, it)
                        ApiDocPreviewForm.getInstance(project, it.containingFile, api).popup()
                    }
                }
                else -> null
            }
        }
        e.getData(WINDOW_PANE)?.getSelected()?.let {
            when (it) {
                is EndpointNode -> {
                    val psiMethod = it.getMethod()
                    psiMethod.containingClass?.let { psiClass ->
                        val api = DocService.instance(project).buildApi(project, psiClass, psiMethod)
                        ApiDocPreviewForm.getInstance(project, psiMethod.containingFile, api).popup()
                    }
                }
                else -> null
            }
        }
    }
}