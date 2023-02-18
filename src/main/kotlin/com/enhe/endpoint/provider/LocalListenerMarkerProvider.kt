// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-02-18

package com.enhe.endpoint.provider

import com.enhe.endpoint.consts.EVENT_LISTENER
import com.enhe.endpoint.consts.PLUGIN_NAME
import com.enhe.endpoint.consts.PUBLISHER
import com.enhe.endpoint.extend.getModules
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator
import com.intellij.icons.AllIcons
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.lang.jvm.JvmMethod
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import java.awt.event.MouseEvent

@Suppress("UnstableApiUsage", "DialogTitleCapitalization")
class LocalListenerMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiMethod) {
            return null
        }
        val psiAnnotation = element.getAnnotation(EVENT_LISTENER) ?: return null
        val value = psiAnnotation.findAttributeValue("value") ?: return null
        if (value !is PsiClassObjectAccessExpression) {
            return null
        }
        val psiType = value.operand.type
        return LineMarkerInfo(psiAnnotation,
            psiAnnotation.textRange,
            AllIcons.CodeWithMe.CwmFollowMe,
            { "Go to local event publisher" },
            { e, listenerAnnotation -> openTargets(e, listenerAnnotation.project, psiType) },
            GutterIconRenderer.Alignment.RIGHT,
            { PLUGIN_NAME }
        )
    }

    private fun openTargets(e: MouseEvent, project: Project, psiType: PsiType) {
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val methods = mutableSetOf<JvmMethod>()
        project.getModules().forEach {
            val findMethods = javaPsiFacade.findClass(PUBLISHER, GlobalSearchScope.moduleWithLibrariesScope(it))
                ?.findMethodsByName("publish") ?: return@forEach
            if (findMethods.size != 1) {
                return@forEach
            }
            methods += findMethods.first()
        }
        val elements = mutableListOf<PsiMethodCallExpressionImpl>()
        methods.forEach { method ->
            if (method !is PsiMethod) {
                return@forEach
            }
            elements += ReferencesSearch.search(method.originalElement).filter {
                when (val methodCall = it.element.parent) {
                    is PsiMethodCallExpression -> {
                        val expressionTypes = methodCall.argumentList.expressionTypes
                        if (expressionTypes.size != 1) {
                            return@filter false
                        }
                        if (expressionTypes.first().canonicalText == psiType.canonicalText) {
                            return@filter true
                        }
                    }
                }
                return@filter false
            }.mapNotNull {
                PsiTreeUtil.getParentOfType(it.element, PsiMethodCallExpressionImpl::class.java)
            }
        }
        if (elements.isNotEmpty()) {
            PsiElementListNavigator.openTargets(e, elements.toTypedArray(), "Local event publisher",
                "Local event publisher", DefaultPsiElementCellRenderer())
        }
    }
}