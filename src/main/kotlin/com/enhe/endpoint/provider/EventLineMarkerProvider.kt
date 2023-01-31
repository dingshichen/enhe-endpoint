// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-27

package com.enhe.endpoint.provider

import com.enhe.endpoint.consts.BKG_TASK_EXECUTOR
import com.enhe.endpoint.consts.PLUGIN_NAME
import com.enhe.endpoint.extend.findAdapterValue
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.impl.java.stubs.index.JavaSuperClassNameOccurenceIndex
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PsiNavigateUtil

/**
 * 事件发布的标记
 */
class EventLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiMethodCallExpression) {
            return null
        }
        return element.methodExpression.qualifierExpression?.let {
            val psiType = it.type
            if (psiType !is PsiClassReferenceType) {
                return null
            }
            if (psiType.reference.qualifiedName != BKG_TASK_EXECUTOR) {
                return null
            }
            val eventMetadata = loadEventMetadata(element) ?: return null
            LineMarkerInfo(element,
                element.textRange,
                AllIcons.CodeWithMe.CwmFollowMe,
                { "Go to task service" },
                {_,_ -> gotoTaskService(element.project, eventMetadata) },
                GutterIconRenderer.Alignment.RIGHT,
                { PLUGIN_NAME }
            )
        }
    }

    /**
     * 获取事件元数据
     */
    private fun loadEventMetadata(methodCall: PsiMethodCallExpression): EventMetadata? {
        val expressionTypes = methodCall.argumentList.expressionTypes
        if (expressionTypes.size !in 3..4) {
            return null
        }
        val lastArg = expressionTypes.last()
        if (lastArg !is PsiClassReferenceType) {
            return null
        }
        return lastArg.resolve()?.let {
            val adapter = it.findAdapterValue() ?: return null
            return EventMetadata(adapter)
        }
    }

    /**
     * 导航到 TaskService
     */
    private fun gotoTaskService(project: Project, eventMetadata: EventMetadata) {
        val references = JavaSuperClassNameOccurenceIndex.getInstance()
            .get("AnnotationTaskService", project, GlobalSearchScope.projectScope(project))
        references.find {
            val psiClass = it.parent
            if (psiClass !is PsiClass) {
                return@find false
            }
            val adapter = psiClass.findAdapterValue() ?: return@find false
            return@find eventMetadata.isEqualsAdapter(adapter)
        }?.let {
            PsiNavigateUtil.navigate(it)
        }

    }

}