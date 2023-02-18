// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-02-09

package com.enhe.endpoint.provider

import com.enhe.endpoint.consts.PLUGIN_NAME
import com.enhe.endpoint.consts.PUBLISHER
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator
import com.intellij.icons.AllIcons
import com.intellij.ide.util.MethodCellRenderer
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import java.awt.event.MouseEvent

/**
 * 本地事件的标记
 */
@Suppress("UNREACHABLE_CODE", "DialogTitleCapitalization")
class LocalEventLineMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiMethodCallExpression) {
            return null
        }
        return element.methodExpression.qualifierExpression?.type.let {
            if (it !is PsiClassReferenceType) {
                return null
            }
            if (it.reference.qualifiedName != PUBLISHER) {
                return null
            }
            if (element.methodExpression.referenceName != "publish") {
                return null
            }
            val expressTypes = element.argumentList.expressionTypes
            if (expressTypes.size != 1) {
                return null
            }
            val eventTypeName = expressTypes[0].canonicalText
            return LineMarkerInfo(element,
                element.textRange,
                AllIcons.CodeWithMe.CwmFollowMe,
                { "Go to local event listener" },
                { e, methodCallExpression -> openTargets(e, methodCallExpression.project, eventTypeName) },
                GutterIconRenderer.Alignment.RIGHT,
                { PLUGIN_NAME }
            )
        }
    }

    private fun openTargets(e: MouseEvent, project: Project, eventTypeName: String) {
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
            PsiElementListNavigator.openTargets(e, methods.toTypedArray(), "Local event Listeners",
                "Local event Listeners", MethodCellRenderer(true))
        }
    }

}