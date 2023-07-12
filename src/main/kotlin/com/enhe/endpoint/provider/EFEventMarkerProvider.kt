// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-04-04

package com.enhe.endpoint.provider

import com.enhe.endpoint.consts.BKG_TASK_EXECUTOR
import com.enhe.endpoint.consts.PLUGIN_NAME
import com.enhe.endpoint.consts.PUBLISHER
import com.enhe.endpoint.extend.findAdapterValue
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator
import com.intellij.icons.AllIcons
import com.intellij.ide.util.MethodCellRenderer
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.impl.java.stubs.index.JavaSuperClassNameOccurenceIndex
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.PsiNavigateUtil
import java.awt.event.MouseEvent

class EFEventMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiMethodCallExpression) {
            return null
        }
        return element.methodExpression.qualifierExpression?.type.let {
            if (it !is PsiClassReferenceType) {
                return null
            }
            when (it.reference.qualifiedName) {
                PUBLISHER -> publisherLineMarker(element)
                BKG_TASK_EXECUTOR -> taskLineMarker(element)
                else -> null
            }
        }
    }

    private fun publisherLineMarker(element: PsiMethodCallExpression): LineMarkerInfo<*>? {
        val expressTypes = element.argumentList.expressionTypes
        if (expressTypes.size != 1) {
            return null
        }
        val eventType = when (element.methodExpression.referenceName) {
            "publish" -> EventType.LOCAL
            "queue" -> EventType.QUEUE
            else -> return null
        }
        return LineMarkerInfo(element,
            element.textRange,
            AllIcons.CodeWithMe.CwmFollowMe,
            { eventType.callListener },
            { e, em -> openEventTargets(eventType, e, em.project, expressTypes[0].canonicalText) },
            GutterIconRenderer.Alignment.RIGHT,
            { PLUGIN_NAME }
        )
    }

    private fun taskLineMarker(element: PsiMethodCallExpression): LineMarkerInfo<*>? {
        val expressionTypes = element.argumentList.expressionTypes
        if (expressionTypes.size !in 3..4) {
            return null
        }
        val lastArg = expressionTypes.last()
        if (lastArg !is PsiClassReferenceType) {
            return null
        }
        val adapterName = lastArg.resolve()?.findAdapterValue() ?: return null
        return LineMarkerInfo(element,
            element.textRange,
            AllIcons.CodeWithMe.CwmFollowMe,
            { "Go to task service" },
            { _,em -> openTaskTargets(em.project, adapterName) },
            GutterIconRenderer.Alignment.RIGHT,
            { PLUGIN_NAME }
        )
    }

    private fun openEventTargets(eventType: EventType, e: MouseEvent, project: Project, eventTypeName: String) {
        val annotations = JavaAnnotationIndex.getInstance().get("EventListener", project, GlobalSearchScope.projectScope(project))
        val methods = annotations.filter { an ->
            val value = an.findAttributeValue("value") ?: return@filter false
            when (value) {
                is PsiClassObjectAccessExpression -> value.operand.type.canonicalText == eventTypeName
                else -> false
            }
        }.mapNotNull { pa ->
            PsiTreeUtil.getParentOfType(pa, PsiMethod::class.java)
        }
        if (methods.isNotEmpty()) {
            PsiElementListNavigator.openTargets(e, methods.toTypedArray(), eventType.toListenerTitle, eventType.findListenerUsagesTitle, MethodCellRenderer(true))
        }
    }

    private fun openTaskTargets(project: Project, adapterName: String) {
        val references = JavaSuperClassNameOccurenceIndex.getInstance()
            .get("AnnotationTaskService", project, GlobalSearchScope.projectScope(project))
        references.find {
            val psiClass = it.parent
            if (psiClass !is PsiClass) {
                return@find false
            }
            val adapter = psiClass.findAdapterValue() ?: return@find false
            return@find adapterName == adapter
        }?.let {
            PsiNavigateUtil.navigate(it)
        }
    }

}